(ns app.polyrhythms.db
  (:require [app.polyrhythms.common :refer [lcm]]
            [cljs.spec.alpha :as s]
            ["fraction.js" :as Fraction]))

(s/def ::verbose? boolean?)
(s/def ::tempo #(instance? Fraction %))
(s/def ::playing? boolean?)
(s/def ::last-beat-time number?)
(s/def ::divisions number?)
(s/def ::input string?)
(s/def ::microbeat number?)
(s/def ::cursor-ref #(or (nil? %) (instance? js/HTMLElement %)))
(s/def ::subdivision
  (s/keys :req-un
          [::divisions
           ::microbeat
           ::input]))

(s/def ::numerator ::subdivision)
(s/def ::denominator ::subdivision)

(s/def ::tempo-input string?)

(s/def ::start #(or (nil? %) (number? %)))
(s/def ::width #(or (nil? %) (number? %)))

(s/def ::grid-x
  (s/keys :req-un
          [::start
           ::width]))

(s/def ::control-select #(contains? #{:main :numerator :denominator} %))

(s/def ::init? boolean?)

(s/def ::polyrhythms
  (s/keys :req-un
          [::init?
           ::numerator
           ::denominator
           ::lcm
           ::tempo
           ::tempo-input
           ::last-beat-time
           ::control-select
           ::playing?
           ::verbose?
           ::grid-x
           ::cursor-ref]))


(def db {:init? false
         :numerator {:divisions 3
                     :microbeat 0
                     :input "180"}
         :denominator {:divisions 2
                       :microbeat 0
                       :input "120"}
         :lcm (lcm 3 2)
         :last-beat-time 0
         :tempo (Fraction. 60)
         :tempo-input "60"
         :control-select :main
         :playing? false
         :verbose? false
         :cursor-ref nil
         :grid-x {:start nil
                  :width nil}})