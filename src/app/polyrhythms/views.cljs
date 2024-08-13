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
  {:width "6rem"
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
     :on-wheel #(-> % .-target .focus)
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
   :justify-content "space-evenly"
   :padding-top     "0.3rem"
   :margin-bottom   (if mobile? "0" "2rem")})

(defn- lcm-display-style
  [mobile?]
  {::stylefy/manual (merge (mui-override-style false) spinner-selector)})

(defn lcm-display
  [total-divisions mobile?]
  [:>
   TextField
   (use-style
    (lcm-display-style mobile?)
    {:type     "number"
     :label    "least common multiple"
     :value    total-divisions
     :variant  "outlined"
     :margin   "dense"
     :disabled true})])

(defn control-group
  [numerator denominator total-divisions mobile?]
  [:div
   (use-style (control-group-style mobile?))
   (selector :denominator denominator mobile?)
   (lcm-display total-divisions mobile?)
   (selector :numerator numerator mobile?)])

(defn- cursor-style
  [playing?]
  {:background-color (:2 colors)
   :border           "none"
   :box-shadow       "none"
   :position         "fixed"
   :opacity          (if playing? "0.65" "0")
   :margin           "0"})

;; (defn rerender-cursor
;;   [ref num-divisions cursor-width]
;;   #(when (some? ref)
;;      (let [ref         ref
;;            el-00-rec   (.getBoundingClientRect (js/document.getElementById "00"))
;;            start-x     (- (.-left el-00-rec) (/ cursor-width 2))
;;            width-x     (* (.-width el-00-rec) @num-divisions)
;;            grid-el-rec (.getBoundingClientRect (js/document.getElementById "grid"))
;;            height      (.-height grid-el-rec)
;;            start-y     (+ (.-top grid-el-rec) (.-scrollY js/window))]
;;        (js/console.log "rerender" start-x)
;;        (dispatch [:poly/update-grid-x [start-x width-x]])
;;       ;;  (swap! grid-x assoc :start start-x :width width-x)
;;        (set! (.. ref -style -left) (str start-x "px"))
;;        (set! (.. ref -style -top) (str start-y "px"))
;;        (.setAttribute ref "size" height))))

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
     :margin           "1rem auto"
     :background-color (:-2 colors)
     :box-shadow       "0 0 6px rgba(0 0 0 / 0.5)"
     :border-radius    "5px"
     :font-family      "lato-light, sans-serif"
     :box-sizing       "border-box"
     :max-height       (px 800)
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