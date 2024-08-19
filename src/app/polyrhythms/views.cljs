(ns app.polyrhythms.views
  (:require ["@mui/material/TextField$default" :as TextField]
            ["react" :as react]
            [app.polyrhythms.events :refer [CURSOR-WIDTH]]
            [app.polyrhythms.settings :refer [settings-container]]
            [app.polyrhythms.styles :refer [mui-override-style]]
            [app.polyrhythms.subs]
            [app.polyrhythms.tempo :refer [spinner-selector tempo-play-group]]
            [app.polyrhythms.visualizer :refer [visualizer-grid]]
            [app.styles :refer [colors]]
            [garden.units :refer [px]]
            [re-frame.core :refer [dispatch dispatch-sync subscribe]]
            [stylefy.core :as stylefy :refer [use-style]]))

(defn- input-style
  [mobile?]
  {:width (if mobile? "4rem" "6rem")
   :scrollbar-width "thin"
   ::stylefy/manual (mui-override-style false)})

(defn- desktop-number-input
  [type value]
  [:>
   TextField
   (use-style
    (input-style false)
    {:type     "number"
     :label    (str (name type) ":")
     :name     (name type)
     :value    value
     :variant  "outlined"
     :margin   "dense"
     :min      1
     :on-wheel #(-> %
                    .-target
                    .focus)
     :onChange #(dispatch [:poly/change-divisions {:divisions (.. % -target -value)
                                                   :which     type}])})])

(def option-style
  {:padding "0"})

(defn mobile-number-select
  [type value]
  [:>
   TextField
   (use-style
    (input-style true)
    {:select      true
     :value       value
     :label       type
     :variant     "outlined"
     :margin      "dense"
     :SelectProps #js {:native    true
                       :autoWidth true}
     :onChange    #(dispatch
                    [:poly/change-divisions {:divisions (.. % -target -value)
                                             :which     type}])})
   (doall
    (for [n (range 1 100)]
      ^{:key (str "select " n)}
      [:option (use-style option-style {:value n}) n]))])

(defn selector
  [type value mobile?]
  (if mobile?
    (mobile-number-select type value)
    (desktop-number-input type value)))

(defn- control-group-style
  [mobile?]
  {:display         "flex"
   :justify-content "center"
   :gap             "1rem"
   :align-items     "center"
   :padding         (str "0.3rem " (if mobile? "1rem" "4rem"))
   :margin-bottom   (if mobile? "0" "1rem")})

(defn- lcm-container
  [mobile?]
  {:margin "auto"})

(defn- lcm-display-style
  [mobile?]
  {:width (if mobile? "4rem" "6rem")
   ::stylefy/manual (merge (mui-override-style false) spinner-selector)})

(defn lcm-display
  [total-divisions mobile?]
  [:>
   TextField
   (use-style
    (lcm-display-style mobile?)
    {:type     "number"
     :label    (if mobile? "lcm" "least common multiple")
     :value    total-divisions
     :variant  "outlined"
     :margin   "dense"
     :disabled true})])

(defn control-group
  [numerator denominator total-divisions mobile?]
  [:div
   (use-style (control-group-style mobile?))
   [:div#left-bracket (use-style {:flex "1 1 auto"
                                  :height "4rem"
                                  :border-style "solid"
                                  :border-color (:0 colors)
                                  :border-width "1px 0 0 1px"
                                  :border-radius "4px 0 0"})]
   [:div
    (use-style {:display "flex" :flex-direction "column"})
    [:div#ratio
     (use-style {:display "flex" :justify-content "center" :gap "1rem" :align-items "center"})
     (selector :numerator numerator mobile?)
     [:span (use-style {:color (:0 colors) :font-size "2.5rem"}) ":"]
     (selector :denominator denominator mobile?)]
    [:div
     (use-style (lcm-container mobile?))
     (lcm-display total-divisions mobile?)]]
   [:div#right-bracket (use-style {:flex "1 1 auto"
                                   :height "4rem"
                                   :border-style "solid"
                                   :border-color (:0 colors)
                                   :border-width "1px 1px 0 0"
                                   :border-radius "0 4px 0 0"})]
  ])

(defn- cursor-style
  [playing?]
  {:background-color (:2 colors)
   :border           "none"
   :box-shadow       "none"
   :position         "fixed"
   :opacity          (if playing? "0.65" "0")
   :margin           "0"})

(defn cursor
  [playing?]
  [:hr
   (use-style (cursor-style playing?)
              {:id    "cursor"
               :width CURSOR-WIDTH
               :size  "800"
               :ref   #(dispatch [:poly/cursor-ref %])})])

(defn- container-style
  [mobile?]
  (let []
    {:max-width        "1200px"
     :width            "calc(100% - 2rem)"
     :margin           "1rem auto"
     :background-color (:-2 colors)
     :box-shadow       "0 0 6px rgba(0 0 0 / 0.5)"
     :border-radius    "5px"
     :font-family      "lato-light, sans-serif"
     :box-sizing       "border-box"
    ;;  :max-height       (px 800)
     :display          "flex"
     :min-height       "0"
     :flex-direction   "column"
     :position         "relative"
     :padding          "1rem"}))

(defn- metronome-group-style
  [mobile?]
  {:padding         (if mobile? "0.5rem 0" "0 3rem")
   :display         "flex"
   :flex-direction  "column"
   :overflow        "hidden"
   :flex            "1 1 auto"
   :justify-content "center"})

(stylefy/tag "body"
             {:margin        "0 !important"
              :overflow-y    "auto !important"
              :padding-right "0 !important"})


(defn polyrhythm-container
  []
  (when (not @(subscribe [:poly/storage-init?])) (dispatch-sync [:poly/fetch-local-storage]))
  (let [numerator       @(subscribe [:poly/numerator-divisions])
        denominator     @(subscribe [:poly/denominator-divisions])
        total-divisions @(subscribe [:poly/lcm])
        mobile?         @(subscribe [:layout/mobile?])
        portrait?       @(subscribe [:layout/portrait?])
        playing?        @(subscribe [:poly/playing?])
        _ (react/useLayoutEffect (fn []
                                   (dispatch [:poly/recalculate-grid-x])
                                   js/undefined)
                                 (array numerator denominator mobile? portrait?))]
    [:div
     (use-style (container-style mobile?))
     [settings-container]
     [:div
      (use-style (metronome-group-style mobile?))
      [control-group numerator denominator total-divisions mobile?]
      [visualizer-grid]
      [tempo-play-group]
      [cursor playing?]]]))