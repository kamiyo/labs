(ns app.views
  (:require [app.polyrhythms.views :refer [polyrhythm-container]]
            [app.styles :refer [colors]]
            [app.svgs.logo :refer [logo-instance logo-svg]]
            [re-frame.core :refer [subscribe]]
            [stylefy.core :as stylefy :refer [use-style]]
            [garden.color :as color]))

(defn menu-container-style [mobile?]
  {:width    "100%"
   :position "fixed"
   :bottom   "0"
   :height   (if mobile? "3.2rem" "100px")
   :z-index  "500"})

(defn menu-style [mobile?]
  {:max-width        "1400px"
   :margin           "0 auto"
   :height           (if mobile? "3.2rem" "100px")
   :width            "100%"
   :display          "flex"
   :align-items      "center"
   :justify-content  "center"
   :border (str "2px solid" (color/as-hex (:1 colors)))
   :box-shadow       "0 -2px 16px rgba(0 0 0 / 0.8)"
   :background-color (:-2 colors)
   ;;  :overflow "hidden"
   :bottom           "0"
   :border-radius    "100px 100px 0 0"
  })

(defn logo-container-style [mobile?]
  {:border-radius    "50%"
   :width            "fit-content"
   :padding          "5px"
   :display          "flex"
   :background-color (:1 colors)})

(defn logo-instance-style
  [mobile?]
  {::stylefy/manual [:svg {:height (if mobile? "6rem" "200px")
                           :width  (if mobile? "6rem" "200px")
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
  (let [mobile? @(subscribe [:layout/mobile?])]
    [:div
     (use-style (menu-container-style mobile?))
     [:div
      (use-style (menu-style mobile?))
      [:div
       (use-style (logo-container-style mobile?))
       [logo-instance (use-style (logo-instance-style mobile?))]]]]))

(defn app
  []
  [:<>
   [logo-svg]
   [:div
    [current-page]]
   [menu]])