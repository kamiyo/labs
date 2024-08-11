(ns app.polyrhythms.subs
  (:require [re-frame.core :refer [reg-sub]]))

(defn- get-in-polyrhythms [db keys]
  (get-in db (into [:polyrhythms] keys)))

(reg-sub
 :poly/numerator-divisions
 (fn [db _]
   (get-in-polyrhythms db [:numerator :divisions])))

(reg-sub
 :poly/denominator-divisions
 (fn [db _]
   (get-in-polyrhythms db [:denominator :divisions])))

(reg-sub
 :poly/tempo
 (fn [db [_ type]]
   (let [main-tempo (get-in-polyrhythms db [:tempo])]
     (if (nil? type)
       main-tempo
       (let [divisions (get-in-polyrhythms db [type :divisions])]
         (.mul ^js main-tempo divisions))))))

(reg-sub
 :poly/playing?
 (fn [db _]
   (get-in-polyrhythms db [:playing?])))

(reg-sub
 :poly/last-beat-time
 (fn [db _]
   (get-in-polyrhythms db [:last-beat-time])))

(reg-sub
 :poly/numerator-microbeat
 (fn [db _]
   (get-in-polyrhythms db [:numerator :microbeat])))

(reg-sub
 :poly/denominator-microbeat
 (fn [db _]
   (get-in-polyrhythms db [:denominator :microbeat])))


(reg-sub
 :poly/verbose?
 (fn [db _]
   (get-in-polyrhythms db [:verbose?])))

(reg-sub
 :poly/lcm
 (fn [db _]
   (get-in-polyrhythms db [:lcm])))

(reg-sub
 :poly/input-value
 (fn [db [_ type]]
   (if (nil? type)
     (get-in-polyrhythms db [:tempo-input])
     (get-in-polyrhythms db [type :input]))))

(reg-sub
 :poly/grid-x
 (fn [db _]
   (get-in-polyrhythms db [:grid-x])))

(reg-sub
 :poly/control-select
 (fn [db _]
   (get-in-polyrhythms db [:control-select])))