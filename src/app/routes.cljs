(ns app.routes
  (:require [re-frame.core :refer [dispatch]]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]))

(def routes
  [["/" :main]
   ["/metronome" :metronome]
   ["/polyrhythms" :polyrhythms]
   ["/github" :github]])

(defn init-app-routes! []
  (rfe/start!
   (rf/router routes)
   (fn [match] (dispatch [:router/change-route (-> match :data :name)]))
   {:use-fragment false}))