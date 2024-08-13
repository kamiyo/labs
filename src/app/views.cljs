(ns app.views
  (:require [app.polyrhythms.views :refer [polyrhythm-container]]
            [app.styles :refer [colors]]
            [app.svgs.logo :refer [logo-instance logo-svg]]
            [re-frame.core :refer [subscribe]]
            [stylefy.core :as stylefy :refer [use-style]]
            [garden.color :as color]))

(def menu-container-style
  {:width    "100%"
   :position "fixed"
   :bottom   "0"
   :height   "100px"
   :z-index  "500"})

(def menu-style
  {:max-width        "1400px"
   :margin           "0 auto"
   :height           "100px"
   :width            "100%"
   :display          "flex"
   :align-items      "center"
   :justify-content  "center"
   ;;  :border-top (str "2px solid" (color/as-hex (:1 colors)))
   :box-shadow       "0 -2px 16px rgba(0 0 0 / 0.8)"
   :background-color (:-2 colors)
   ;;  :overflow "hidden"
   :bottom           "0"
   :border-radius "100px 100px 0 0"
   })

(def logo-container-style
  {:border-radius    "120px"
   :width            "fit-content"
   :padding          "5px"
   :display          "flex"
   :background-color (:1 colors)})

(def logo-instance-style
  {::stylefy/manual [:svg {:height "200px"
                           :width  "200px"
                           :fill   (:-2 colors)
                           :stroke (:-2 colors)}]})

(defmulti current-page (fn [_] @(subscribe [:router/route])))
(defmethod current-page :polyrhythms
  []
  [:f> polyrhythm-container])
(defmethod current-page :metronome
  []
  [:f> polyrhythm-container])
(defmethod current-page :default
  []
  [:f> polyrhythm-container])

(defn menu
  []
  [:div
   (use-style menu-container-style)
   [:div
    (use-style menu-style)
    [:div
     (use-style logo-container-style)
     [logo-instance (use-style logo-instance-style)]]]])

(defn app
  []
  [:<>
   [logo-svg]
   [:div
    [current-page]]
   [menu]])