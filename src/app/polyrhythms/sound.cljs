(ns app.polyrhythms.sound
  (:require [cljs-bach.synthesis :as a]
            [re-frame.core
             :refer
             [dispatch
              dispatch-sync]]
            [app.polyrhythms.common
             :as
             common
             :refer
             [worker
              get-seconds-per-beat
              audio-context
              analyser-denominator
              analyser-numerator]]
            [app.polyrhythms.animation :refer [raf-id animate stop-animation]]))

(defonce schedule-ahead-time 0.1) ; s
(defonce lookahead 25.0) ; ms
(defonce beat-frequencies {:numerator 3520 :denominator 2640})

(defonce audio-filter
  (a/connect->
   (a/percussive 0.01 0.05)
   (a/gain 0.2)))

(defn analyser-numerator-subgraph
  []
  (a/subgraph analyser-numerator))

(defn analyser-denominator-subgraph
  []
  (a/subgraph analyser-denominator))

;; (defn blip [freq]
;;   (a/connect->
;;    (a/sine freq)
;;    audio-filter))

(def blip-numerator
  (a/connect->
   (a/sine (:numerator beat-frequencies))
   audio-filter
   analyser-numerator-subgraph
   a/destination))

(def blip-denominator
  (a/connect->
   (a/sine (:denominator beat-frequencies))
   audio-filter
   analyser-denominator-subgraph
   a/destination))

(defn play-once
  [time which]
  (let [synth (condp = which
                :numerator   blip-numerator
                :denominator blip-denominator)]
    (a/run-with synth audio-context time 0.1)))

;; (defn next-note
;;   [which]
;;   (let [tempo            @common/tempo
;;         divisions        (case which
;;                            :numerator @common/numerator
;;                            :denominator @common/denominator)
;;         seconds-per-beat (get-seconds-per-beat tempo divisions)
;;         next-note-time   @(keyword (str "poly/" (name which) "-next-note-time"))]
;;     (dispatch-sync [:poly/change-next-note-time {:next-note-time (+ next-note-time seconds-per-beat)
;;                                                  :which which}])))

(defn schedule-note
  [time which]
  (play-once time which))

(defn scheduler-loop
  [which seconds-per last-beat-time limit]
  (let [which-microbeat    (condp = which
                             :numerator   common/numerator-microbeat
                             :denominator common/denominator-microbeat)
        get-next-note-time (fn []
                             (-> @which-microbeat
                                 (* seconds-per)
                                 (+ last-beat-time)))]
    (while (< (get-next-note-time) limit)
      (schedule-note (get-next-note-time) which)
      (dispatch-sync [:poly/inc-microbeat which]))))

(defn scheduler
  []
  (let [tempo          @common/tempo
        last-beat-time @common/last-beat-time
        numerator      @common/numerator
        denominator    @common/denominator
        seconds-per-numerator (get-seconds-per-beat tempo numerator)
        seconds-per-denominator (get-seconds-per-beat tempo denominator)
        limit          (+ schedule-ahead-time (a/current-time audio-context))]
    (scheduler-loop :numerator seconds-per-numerator last-beat-time limit)
    (scheduler-loop :denominator seconds-per-denominator last-beat-time limit)
    (if (and
         (> @common/numerator-microbeat numerator)
         (> @common/denominator-microbeat denominator))
      (dispatch-sync [:poly/normalize-microbeats])
      nil)))

(defn play
  [play?]
  (if play?
    (do
      (dispatch [:poly/toggle-playing])
      (reset! raf-id (js/window.requestAnimationFrame animate))
      (.postMessage ^js @worker "start")
      (dispatch-sync [:poly/change-last-beat-time (+ 0.06 (a/current-time audio-context))])
      (dispatch-sync [:poly/reset-microbeats]))
    (do
      (dispatch [:poly/toggle-playing])
      (dispatch-sync [:poly/change-last-beat-time (a/current-time audio-context)])
      (dispatch-sync [:poly/reset-microbeats])
      (stop-animation)
      (.postMessage ^js @worker "stop"))))