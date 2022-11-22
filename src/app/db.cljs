(ns app.db
  (:require [cljs.spec.alpha :as s]
            [re-frame.core :as re-frame]
            [app.polyrhythms.common :refer [lcm]]
            [app.routes :refer [routes]]))

(s/def ::is-verbose? boolean?)
(s/def ::is-portrait? boolean?)
(s/def ::is-mobile? boolean?)
(s/def ::width number?)
(s/def ::height number?)
(s/def ::tempo number?)
(s/def ::is-playing? boolean?)
(s/def ::last-beat-time number?)
(s/def ::divisions number?)
(s/def ::input string?)
(s/def ::microbeat number?)
(s/def ::subdivision
  (s/keys :req-un
          [::divisions
           ::microbeat
           ::input]))

(s/def ::layout
  (s/keys :req-un
          [::is-portrait?
           ::is-mobile?
           ::width
           ::height]))

(s/def ::numerator ::subdivision)
(s/def ::denominator ::subdivision)
(s/def ::route (set (map #(second %) routes)))

(s/def ::tempo-input string?)

(s/def ::db
  (s/keys :req-un
          [::layout
           ::route
           ::numerator
           ::denominator
           ::lcm
           ::tempo
           ::tempo-input
           ::last-beat-time
           ::is-playing?
           ::is-verbose?]))

(def default-db
  {:layout 
   {:is-mobile? false
    :is-portrait? false
    :width 0
    :height 0}
   :route :polyrhythms
   :numerator {:divisions 3
               :microbeat 0
               :input "180.0"}
   :denominator {:divisions 2
                 :microbeat 0
                 :input "120.0"}
   :lcm (lcm 3 2)
   :last-beat-time 0
   :tempo 60
   :tempo-input "60.0"
   :is-playing? false
   :is-verbose? false})