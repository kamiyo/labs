(ns app.routes
  (:require [re-frame.core :refer [dispatch]]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]))

(def routes
  [["/" :main]
   ["/polyrhythms" :polyrhythms]
   ["/github" :github]])

;; (defn init-router! []
;;   (accountant/configure-navigation!
;;    {:nav-handler (fn [path]
;;                    (secretary/dispatch! path))
;;     :path-exists? (fn [path]
;;                     (secretary/locate-route path))}))

;; (defn init-app-routes []
;;   (defroute "/" []
;;     (dispatch [:change-route :polyrhythms]))
;;   (defroute "/polyrhythms" []
;;     (dispatch [:change-route :polyrhythms]))
;;   (init-router!))

(defn init-app-routes! []
  (rfe/start!
   (rf/router routes)
   (fn [match] (dispatch [:change-route (-> match :data :name)]))
   {:use-fragment false}))