(ns app.polyrhythms.common
  (:require [cljs-bach.synthesis :as a]
            ["fraction.js" :as Fraction]))

(def worker (atom (js/Worker. "./js/worker.js")))
(def grid-x (atom {:start nil :width nil}))
(def tempo (atom (Fraction. 0)))
(def last-beat-time (atom 0))
(def numerator (atom 3))
(def numerator-microbeat (atom 0))
(def denominator (atom 2))
(def denominator-microbeat (atom 0))


(defn get-seconds-per-beat
  ([tempo] (/ 60.0 tempo))
  ([tempo divisions] (/ 60.0 tempo divisions)))

(def audio-context (a/audio-context))
(def analyser-numerator (.createAnalyser ^js audio-context))
(def analyser-denominator (.createAnalyser ^js audio-context))
(def buffer-numerator (js/Uint8Array. 256))
(def buffer-denominator (js/Uint8Array. 256))

(defn init-audio
  []
  (set! (.-fftSize analyser-numerator) 512)
  (set! (.-fftSize analyser-denominator) 512))

(defn get-context-current-time
  []
  (a/current-time audio-context))

(defn populate-analyser
  [which]
  (let [analyser (condp = which
                   :numerator analyser-numerator
                   :denominator analyser-denominator)
        buffer (condp = which
                 :numerator buffer-numerator
                 :denominator buffer-denominator)]
    (.getByteTimeDomainData analyser buffer)))

(defn gcd
  [a b]
  (if (zero? b)
    a
    (recur b (mod a b))))

(defn lcm
  [a b]
  (/ (* a b) (gcd a b)))