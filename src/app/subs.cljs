(ns app.subs
 (:require [re-frame.core :refer [reg-sub]]))


(reg-sub
 :router/route
 (fn [db _]
   (:route db)))

(reg-sub
 :layout/mobile?
 (fn [db _]
   (get-in db [:layout :mobile?])))

(reg-sub
 :layout/portrait?
 (fn [db _]
   (get-in db [:layout :portrait?])))

(reg-sub
 :layout/viewport-width
 (fn [db _]
   (get-in db [:layout :width])))

(reg-sub
 :mouse/down?
 (fn [db _]
   (get-in db [:mouse :down?])))