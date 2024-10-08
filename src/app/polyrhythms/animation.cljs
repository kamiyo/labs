(ns app.polyrhythms.animation
  (:require [app.polyrhythms.common
             :as
             common
             :refer
             [buffer-denominator
              buffer-numerator
              get-context-current-time
              get-seconds-per-beat
              populate-analyser]]
            [app.styles :refer [colors]]
            [clojure.string :as s]
            [reagent.core :as r]
            [garden.color :as color]
            [garden.compiler :as compiler]))

(defonce raf-id (r/atom nil))

(defn set-el-highlight
  [el color]
  (set! (.. el -style -fontWeight) "bold")
  (set! (.. el -style -fontSize) "1.5rem")
  (set! (.. el -style -textShadow) color)
  (set! (.. el -style -color) "#ffffff"))

(defn unset-el-highlight
  [el]
  (set! (.. el -style -fontWeight) "normal")
  (set! (.. el -style -fontSize) "1.2rem")
  (set! (.. el -style -textShadow) "none")
  (set! (.. el -style -color) (:0 colors)))

(def fade-buffer
  (atom {:top    []
         :bottom []}))

(defn do-beep
  [which]
  (let [[buffer color]    (condp = which
                            :top    [buffer-numerator "#2CA4B8"]
                            :bottom [buffer-denominator "#e6825c"])
        normalized-buffer (reduce (fn [acc val]
                                    (+ acc (Math/abs (- val 128))))
                                  0
                                  (array-seq buffer))
        divisions         (case which
                            :top    @common/numerator
                            :bottom @common/denominator)
        tempo             @common/tempo
        bpm-ratio         (-> ^js tempo
                              (.mul divisions)
                              (.div 60)
                              (.valueOf)
                              (js/Math.sqrt))
        window-size       (-> 10
                              (/ bpm-ratio)
                              (js/Math.floor))
        fade-vec          (if (pos? normalized-buffer)
                            (into [] (take window-size (repeat 1)))
                            (-> (which @fade-buffer)
                                rest
                                (vec)
                                (conj 0)))
        fade-avg          (/ (reduce + fade-vec) (count fade-vec))
        grid-el           (js/document.getElementById "grid")]
    (swap! fade-buffer assoc-in [which] fade-vec)
    (if (pos? fade-avg)
      (if (= :top which)
        (set! (.. grid-el -style -borderTop)
              (str "1.5px solid " color (.toString (* 255 fade-avg) 16)))
        (set! (.. grid-el -style -borderBottom)
              (str "1.5px solid " color (.toString (* 255 fade-avg) 16))))
      (if (= :top which)
        (set! (.. grid-el -style -borderTop) (str "1.5px solid transparent"))
        (set! (.. grid-el -style -borderBottom) (str "1.5px solid transparent")))
    )
    (doseq
      [el (array-seq (js/document.getElementsByClassName (str "beep " (name which))))]
      (if (pos? fade-avg)
        (do
          (set! (.. el -style -backgroundColor) color)
          (set! (.. el -style -transform) (str "scale(" (+ 1 fade-avg) ")")))
        (do
          (set! (.. el -style -backgroundColor) (:0 colors))
          (set! (.. el -style -transform) nil))))))

(defn animate
  []
  (reset! raf-id (js/window.requestAnimationFrame animate))
  (let [{:keys [start width]} @common/grid-x
        tempo             (.valueOf @common/tempo)
        last-beat-time    @common/last-beat-time
        seconds-per-beat  (get-seconds-per-beat tempo)
        time-since-last-beat (- (get-context-current-time) last-beat-time)
        progress          (/ time-since-last-beat seconds-per-beat)
        progress-adjusted (mod (+ 1 progress) 1)
        cursor            (js/document.getElementById "cursor")
        cursor-bounding   (.getBoundingClientRect cursor)
        cursor-left       (.-left cursor-bounding)
        cursor-width      (.-width cursor-bounding)
        cursor-midpoint   (+ cursor-left (/ cursor-width 2))
        new-x             (+ start (* progress-adjusted width))]
    (set! (.. cursor -style -left) (str new-x "px"))
    (doseq [el   (array-seq (js/document.getElementsByClassName "number"))
            :let [el-bounding         (.getBoundingClientRect el)
                  el-left             (.-left el-bounding)
                  el-width            (.-width el-bounding)
                  el-midpoint         (+ el-left (/ el-width 2))
                  midpoint-difference (- cursor-midpoint el-midpoint)
                  max-thresh          (- width 20)
                  color               (if
                                        (-> el
                                            .-classList
                                            (.contains "denominator"))
                                        (s/join
                                         ", "
                                         (take 10
                                               (repeat (str "0 0 3px "
                                                            (-> "#e6825c"
                                                                (color/transparentize 0.2)
                                                                compiler/render-css)))))
                                        (s/join
                                         ", "
                                         (take 10
                                               (repeat (str "0 0 3px "
                                                            (-> "#2CA4B8"
                                                                (color/transparentize
                                                                 0.2)
                                                                compiler/render-css
                                                            ))))))]]
      (if (or (and (> midpoint-difference (/ tempo -3))
                   (< midpoint-difference (/ tempo 2)))
              (> midpoint-difference max-thresh))
        (set-el-highlight el color)
        (unset-el-highlight el)))
    (populate-analyser :numerator)
    (populate-analyser :denominator)
    (do-beep :top)
    (do-beep :bottom)))

(defn stop-animation
  []
  (js/window.cancelAnimationFrame @raf-id)
  (doseq [el (array-seq (js/document.querySelectorAll ".number,.beep,#grid"))]
    (.removeAttribute el "style"))
  (let [{:keys [start _width]} @common/grid-x
        cursor (js/document.getElementById "cursor")]
    (set! (.. cursor -style -left) (str start "px")))
  (reset! raf-id nil))