(ns app.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx reg-fx after debug]]
            [app.db :refer [default-db]]
            [app.polyrhythms.common :refer [get-context-current-time get-seconds-per-beat lcm]]
            [cljs.spec.alpha :as s]))

(defn check-and-throw
  "Throws an exception if `db` doesn't match the Spec `a-spec`."
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

;; now we create an interceptor using `after`
(def check-spec-interceptor (after (partial check-and-throw :app.db/db)))

(def debug? ^boolean goog.DEBUG)
(def standard-interceptors [(when debug? debug)
                            (when debug? check-spec-interceptor)])

(reg-event-db
 :main/initialise-db
 (fn [_ _]
   default-db))

(reg-event-db
 :router/change-route
 [check-spec-interceptor]
 (fn [db [_ route]]
   (assoc db :route route)))

(reg-event-db
 :layout/update
 [check-spec-interceptor]
 (fn [db [_ values-map]]
   (update-in db [:layout] merge values-map)))

(reg-event-db
 :mouse/set-down
 [check-spec-interceptor]
 (fn [db [_ down?]]
   (assoc-in db [:mouse :down?] down?)))