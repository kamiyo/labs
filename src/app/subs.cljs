(ns app.subs
 (:require [re-frame.core :refer [reg-sub subscribe]]
           [reagent.ratom :refer [reaction]]))

(reg-sub
 :numerator-divisions
 (fn [db _]
   (get-in db [:numerator :divisions])))

(reg-sub
 :denominator-divisions
 (fn [db _]
   (get-in db [:denominator :divisions])))

(reg-sub
 :tempo
 (fn [db _]
   (:tempo db)))

(reg-sub
 :is-playing?
 (fn [db _]
   (:is-playing? db)))

(reg-sub
 :last-beat-time
 (fn [db _]
   (:last-beat-time db)))

(reg-sub
 :numerator-microbeat
 (fn [db _]
   (get-in db [:numerator :microbeat])))

(reg-sub
 :denominator-microbeat
 (fn [db _]
   (get-in db [:denominator :microbeat])))

(reg-sub
 :route
 (fn [db _]
   (:route db)))

(reg-sub
 :is-mobile?
 (fn [db _]
   (get-in db [:layout :is-mobile?])))

(reg-sub
 :is-portrait?
 (fn [db _]
   (get-in db [:layout :is-portrait?])))

(reg-sub
 :viewport-width
 (fn [db _]
   (get-in db [:layout :width])))

(reg-sub
 :is-verbose?
 (fn [db _]
   (:is-verbose? db)))

(reg-sub
 :lcm
 (fn [db _]
   (:lcm db)))

(reg-sub
 :input-value
 (fn [db [_ type]]
   (if (nil? type)
     (:tempo-input db)
     (get-in db [type :input]))))
