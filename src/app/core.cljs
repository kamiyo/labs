(ns app.core
  (:require ["mobile-detect" :as mobile-detect]
            [app.events]
            [app.polyrhythms.animation]
            [app.polyrhythms.common :refer [init-audio worker]]
            [app.polyrhythms.sound :refer [lookahead scheduler]]
            [app.routes :refer [init-app-routes!]]
            [app.styles :refer [colors]]
            [app.subs]
            [app.views :refer [app]]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [reagent.dom :as rdom]
            [stylefy.core :as stylefy]
            [stylefy.reagent :as stylefy-reagent]))

(defn listen-worker
  [^js e]
  (when (= (.-data e) "tick")
    (scheduler)))

(defn listen-browser
  [^js e]
  (let [mobile? (-> (mobile-detect. js/window.navigator.userAgent) .mobile some?)
        innerWidth (.-innerWidth js/window)
        innerHeight (.-innerHeight js/window)
        ratio (/ innerWidth innerHeight)]
    (dispatch [:layout/update
               {:mobile? mobile?
                :width innerWidth
                :height innerHeight
                :portrait? (<= ratio 1)}])))

(defn start []
  (init-audio)
  (init-app-routes!)
  (.addEventListener ^js @worker "message" listen-worker)
  (.addEventListener ^js js/window "resize" listen-browser)
  (.postMessage ^js @worker (clj->js {:interval lookahead}))
  (listen-browser nil)
  (rdom/render [app] (.getElementById js/document "app")))

(stylefy/init
 {:dom (stylefy-reagent/init)
  :global-vendor-prefixed
  {::stylefy/vendors      ["webkit" "moz" "o"]
   ::stylefy/auto-prefix #{:border-radius}}})

(stylefy/tag "html" {:background-color (:-3 colors)})

(dispatch-sync [:main/initialise-db])

(defn stop []
  (.postMessage @worker "stop")
  (.removeEventListener js/window "resize" listen-browser)
  (.removeEventListener @worker "tick" listen-worker))

(defn ^:export main []
  (start))