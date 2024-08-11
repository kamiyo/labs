(ns app.db
  (:require [cljs.spec.alpha :as s]
            [app.polyrhythms.db :as poly]
            [app.routes :refer [routes]]))

(s/def ::portrait? boolean?)
(s/def ::mobile? boolean?)
(s/def ::width number?)
(s/def ::height number?)

(s/def ::layout
  (s/keys :req-un
          [::portrait?
           ::mobile?
           ::width
           ::height]))

(s/def ::route (fn [v] (contains? (set (map #(second %) routes)) v)))

(s/def ::down? boolean?)

(s/def ::mouse (s/keys :req-un [::down?]))

(s/def ::db
  (s/keys :req-un
          [::layout
           ::mouse
           ::route
           ::poly/polyrhythms]))

(def default-db
  {:layout
   {:mobile?   false
    :portrait? false
    :width     0
    :height    0}
   :mouse
   {:down? false}
   :route :polyrhythms
   :polyrhythms poly/db})