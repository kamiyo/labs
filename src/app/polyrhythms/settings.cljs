(ns app.polyrhythms.settings
  (:require ["@mui/material/FormControlLabel$default" :as Form-Control-Label]
            ["@mui/material/FormGroup$default" :as Form-Group]
            ["@mui/material/Menu$default" :as Menu]
            ["@mui/material/MenuItem$default" :as Menu-Item]
            ["@mui/material/Switch$default" :as Switch]
            [app.styles :refer [colors]]
            [app.svgs.gear :refer [gear-svg]]
            [garden.color :as color]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r]
            [stylefy.core :as stylefy :refer [use-style]]))

(defn- settings-container-style
  [mobile?]
  (merge (if mobile?
           {:position "absolute" :right "0"}
           {:display "flex" :justify-content "flex-end" :flex "0 0 0"})
         {:margin-bottom (if mobile? "0" "3rem")
          :height        (if mobile? "0.5rem" "100%")}))

(defn- get-gear-style
  [is-open?]
  {:margin        "0.5rem"
   :height        "1.5rem"
   :width         "1.5rem"
   :fill          (:1 colors)
   :transition    "transform 0.8s"
   :transform     (if is-open? "rotate(360deg)" "rotate(0)")
   ::stylefy/mode {:hover {:cursor "pointer"}}})

(def mui-switch-override-style
  {::stylefy/manual [[:.MuiSwitch-colorPrimary
                      [[:& {:color      "rgba(255 255 255 / 0.1)"
                            :transition "all 0.25s"}]
                       [:&:hover {:background-color (color/transparentize (:1 colors) 0.8)}]]
                     ]
                     [:.MuiSwitch-colorPrimary.Mui-checked
                      [[:& {:color      (:1 colors)
                            :transition "all 0.25s"}]
                       [:&:hover {:background-color (color/transparentize (:0 colors) 0.5)}]]]
                     [".MuiSwitch-colorPrimary.Mui-checked + .MuiSwitch-track"
                      {:background-color (:1 colors)}]]})

(def settings-style
  {::stylefy/manual [[:.MuiMenu-paper
                      [:& {:background-color (color/transparentize (:-3 colors) 0.05)
                           :color (:1 colors)}]]]})

(defn verbose-toggler
  []
  [:>
   Menu-Item
   [:>
    Form-Group
    {:row true}
    [:>
     Form-Control-Label
     (use-style mui-switch-override-style
                {:control (r/create-element
                           Switch
                           (clj->js {:checked  @(subscribe [:poly/verbose?])
                                     :onChange #(dispatch
                                                 [:poly/toggle-verbose?])
                                     :value    "is-verbose?"}))
                 :label   "Verbose UI"})]]])

(defn settings-container
  []
  (let [anchor-el    (r/atom nil)
        handle-open  #(reset! anchor-el (.-currentTarget %))
        handle-close #(reset! anchor-el nil)]
    (fn []
      (let [is-open? (some? @anchor-el)]
        [:div
         (use-style (settings-container-style @(subscribe
                                                [:layout/mobile?])))
         [gear-svg
          (use-style (get-gear-style is-open?) {:on-click handle-open})]
         [:>
          Menu
          (use-style settings-style
                     {:open             is-open?
                      :anchor-el        @anchor-el
                      :keep-mounted     true
                      :anchor-origin    #js {:vertical "bottom" :horizontal "right"}
                      :transform-origin #js {:vertical "top" :horizontal "right"}
                      :on-close         handle-close})
          (verbose-toggler)]]))))