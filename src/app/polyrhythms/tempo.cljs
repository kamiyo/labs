(ns app.polyrhythms.tempo
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [stylefy.core :as stylefy :refer [use-style]]
            [fork.re-frame :as fork]
            [app.mui :as mui]
            [app.polyrhythms.buttons :refer [play-button pause-button]]
            [app.polyrhythms.sound :refer [play]]
            [app.polyrhythms.styles :refer [mui-override-style]]
            [app.polyrhythms.common :refer [context]]))

(defonce touch-init (r/atom 0))
(defonce tempo-on-touch (r/atom 0))
(defonce slider-angle (r/atom 0))
(defonce slider-height 50)
(defonce slider-width 300)

(defn update-canvas []
  (let [angle @slider-angle
        canvas (js/document.getElementById "slider-canvas")
        context (.getContext canvas "2d")
        scale (/ js/Math.PI 3)]
    (.clearRect context 0 0 (.-width canvas) (.-height canvas))
    (dorun
     (for [arr (range -5 6 0.5)
           :let [val (-> arr (* 2) (+ angle) (/ 10) (* scale))
                 left (-> arr
                          (- 0.05)
                          (* 2)
                          (+ angle) ; [0 1)
                          (/ 10) ; results in [-1 1]
                          (* scale) ; results in [-pi/3 pi/3]
                          (js/Math.sin) ; results in [-sqrt(3)/2 sqrt(3)/2]
                          (/ (/ (js/Math.sqrt 3) 2))) ; results in [-1 1]
                 right (-> arr
                           (+ 0.05)
                           (* 2)
                           (+ angle)
                           (/ 10)
                           (* scale)
                           (js/Math.sin)
                           (/ (/ (js/Math.sqrt 3) 2)))
                 width (* 100 (- right left))
                 pos (-> val
                         (js/Math.sin)
                         (/ (/ (js/Math.sqrt 3) 2))
                         (* (/ slider-width 2))
                         (+ (/ slider-width 2)))]]
       (do
         (set! (.-strokeStyle context) (str "rgba(0, 0, 0, " (- 1 (js/Math.abs (js/Math.sin val))) ")"))
         (set! (.-lineWidth context) width)
         (.beginPath context)
         (.moveTo context pos 0)
         (.lineTo context pos slider-height)
         (.stroke context))))))

(defn on-touch-move [e]
  (if (undefined? (.. e -touches))
    nil
    (let [touch (.. e -touches (item 0) -clientX)
          diff (- touch @touch-init)]
      (reset! slider-angle (mod (/ diff 20) 1))
      (dispatch [:change-tempo (+ @tempo-on-touch (/ (js/Math.round (* (/ diff 20) 10)) 10))]))))

(def canvas-style
  {:margin "0 2rem"})

(defn slider-canvas [angle]
  (r/create-class
   {:component-did-mount #(update-canvas)
    :component-did-update #(update-canvas)
    :display-name "slider-canvas"
    :reagent-render (fn [angle]
                      [:canvas
                       (use-style
                        canvas-style
                        {:id "slider-canvas"
                         :width slider-width
                         :height slider-height
                         :on-touch-start (fn [e]
                                           (let [touch (.. e -touches (item 0) -clientX)]
                                             (reset! touch-init touch)
                                             (reset! tempo-on-touch @(subscribe [:tempo]))))
                         :on-touch-move on-touch-move
                         :on-mouse-move on-touch-move})])}))

(def no-spinner {:appearance "none"
                 :margin 0
                 :display "none"})

(def spinner-selector [["& input::-webkit-outer-spin-button, & input::-webkit-inner-spin-button" no-spinner]
                       ["input[type=number]" {:appearance "textfield"}]])

(defn- tempo-field-style [is-mobile?]
  {:padding (if is-mobile? "0" "2rem 0")
   ::stylefy/manual (merge
                     (mui-override-style false "4rem")
                     spinner-selector)})

(defn- num-den-field-style [is-mobile?]
  {:padding (if is-mobile? "0" "2rem 0")
   ::stylefy/manual (merge
                     (mui-override-style false "4rem")
                     spinner-selector)})

(def tempo-input-style
  {:margin "0 1rem"})

(defn tempo-control [is-mobile? tempo]
  (let [value (r/atom tempo)
        input (subscribe [:input-value])]
    (fn [is-mobile?]
      (let [changeFn #(do
                        (reset! value (.toFixed % 1))
                        (dispatch [:change-tempo %]))
            inputFn #(do
                       (reset! value %)
                       (dispatch [:update-input [%]]))]
        (reset! value @input)
        [:div
         (use-style (tempo-field-style is-mobile?))
         [mui/text-field
          (use-style
           tempo-input-style
           {:key       "tempo"
            :type      "number"
            :variant   "outlined"
            :margin    "dense"
            :label     "group-bpm"
            :name      "tempo"
            :value     @value
            :on-wheel  #(let [del (.-deltaY %)
                              v   (js/parseFloat @value)]
                          (cond
                            (pos? del) (let [tempo (- v 0.1)]
                                         (changeFn tempo))
                            (neg? del) (let [tempo (+ v 0.1)]
                                         (changeFn tempo)))
                          (.preventDefault %))
            :on-change #(inputFn (.. % -target -value))
            :on-blur   #(let [tempo (js/parseFloat @value)]
                          (changeFn tempo))
            :on-submit #(let [tempo (js/parseFloat @value)]
                          (changeFn tempo))})]]))))

(defn num-tempo [is-mobile? default]
  (let [value (r/atom default)
        input (subscribe [:input-value :numerator])]
    (fn [is-mobile?]
      (let [changeFn #(do
                        (reset! value (.toFixed % 1))
                        (dispatch [:change-related-tempo [% :numerator]]))
            inputFn #(do
                       (reset! value %)
                       (dispatch [:update-input [% :numerator]]))]
        (reset! value @input)
        [:div
         (use-style (num-den-field-style is-mobile?))
         [mui/text-field
          (use-style
           tempo-input-style
           {:key       "num-tempo"
            :type      "number"
            :variant   "outlined"
            :margin    "dense"
            :label     "num-bpm"
            :name      "numerator-tempo"
            :value     @value
            :on-wheel  #(let [del (.-deltaY %)
                              v   (js/parseFloat @value)]
                          (cond
                            (pos? del) (let [tempo (- v 0.1)]
                                         (changeFn tempo))
                            (neg? del) (let [tempo (+ v 0.1)]
                                         (changeFn tempo)))
                          (.preventDefault %))
            :on-change #(inputFn (.. % -target -value))
            :on-blur   #(let [tempo (js/parseFloat @value)]
                          (changeFn tempo))
            :on-submit #(let [tempo (js/parseFloat @value)]
                          (changeFn tempo))})]]))))

(defn den-tempo [is-mobile? default]
  (let [value (r/atom default)
        input (subscribe [:input-value :denominator])]
    (fn [is-mobile?]
      (let [changeFn #(do
                        (reset! value (.toFixed % 1))
                        (dispatch [:change-related-tempo [% :denominator]]))
            inputFn #(do
                       (reset! value %)
                       (dispatch [:update-input [% :denominator]]))]
        (reset! value @input)
        [:div
         (use-style (num-den-field-style is-mobile?))
         [mui/text-field
          (use-style
           tempo-input-style
           {:key       "den-tempo"
            :type      "number"
            :variant   "outlined"
            :margin    "dense"
            :label     "den-bpm"
            :name      "denominator-tempo"
            :value     @value
            :on-wheel  #(let [del (.-deltaY %)
                              v   (js/parseFloat @value)]
                          (cond
                            (pos? del) (let [tempo (- v 0.1)]
                                         (changeFn tempo))
                            (neg? del) (let [tempo (+ v 0.1)]
                                         (changeFn tempo)))
                          (.preventDefault %))
            :on-change #(inputFn (.. % -target -value))
            :on-blur   #(let [tempo (js/parseFloat @value)]
                          (changeFn tempo))
            :on-submit #(let [tempo (js/parseFloat @value)]
                          (changeFn tempo))})]]))))

(defn tempo-play-style [is-mobile?]
  {:text-align "center"
   :display "flex"
   :flex-direction "row-reverse"
   :flex "0 0 auto"
   :justify-content "center"
   :align-items "center"})

(defn handle-play-click
  [event]
  (if (= (.-state context) "suspended")
    (-> (.resume context) (.then (play)))
    (play)))

(defn tempo-play-group []
  (let [is-mobile?    @(subscribe [:is-mobile?])
        is-playing?   @(subscribe [:is-playing?])
        tempo         @(subscribe [:input-value])
        den           @(subscribe [:input-value :denominator])
        num           @(subscribe [:input-value :numerator])
        button-height (if is-mobile? 60 120)
        button-width  (if is-mobile? 95 120)]
    [:div
     (use-style nil)
     [:div
      (use-style (tempo-play-style is-mobile?))
      [den-tempo is-mobile? den]
      [tempo-control is-mobile? tempo]
      [num-tempo is-mobile? num]]
     (when is-mobile? [:div
                       (use-style {:text-align "center"
                                   :margin "1rem auto"})
                       [slider-canvas 
                        @slider-angle]]) 
     [:div
      (use-style {:text-align "center"})
      (if is-playing?
        [pause-button {:on-click handle-play-click
                       :width button-width
                       :height button-height}]
        [play-button  {:on-click handle-play-click
                       :width button-width
                       :height button-height}])]]))