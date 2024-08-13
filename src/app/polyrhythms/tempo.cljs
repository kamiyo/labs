(ns app.polyrhythms.tempo
  (:require ["@mui/material/Radio$default" :as Radio]
            ["@mui/material/TextField$default" :as Text-Field]
            ["react" :as react]
            [app.polyrhythms.buttons :refer [pause-button play-button]]
            [app.polyrhythms.common
             :refer
             [audio-context]]
            [app.polyrhythms.sound :refer [play]]
            [app.polyrhythms.styles :refer [mui-override-style mui-radio-style]]
            [app.styles :refer [colors]]
            [garden.color :as color]
            [re-frame.core :refer [dispatch subscribe]]
            [stylefy.core :as stylefy :refer [use-style]]))

(def slider-height 50)
(def slider-width 300)

(defn update-canvas
  [angle ref]
  (let [canvas (.-current ref)]
    (if (some? canvas)
      (let [context (.getContext canvas "2d")
            scale   (/ js/Math.PI 3)
            gradient (.createLinearGradient context 0 0 300 0)]
        (.clearRect context 0 0 (.-width canvas) (.-height canvas))
        (dorun
         (for [arr (range -5 5.5 0.5)
               :let [curve (-> arr
                               (* 2)
                               (/ 10)
                               (* scale)
                               (js/Math.sin)
                               (/ (/ (js/Math.sqrt 3) 2))
                               (+ 1)
                               (/ 2))
                     color (-> arr
                               (js/Math.abs)
                               (* 2)
                               (/ 10)
                               (* -0.35)
                               (+ 0.42)
                               (* 255))]]
           (.addColorStop ^js gradient curve (str "rgb(" color ", " (* 1.1 color) ", " (* 1.2 color) ")"))))
        (set! (.-strokeStyle context) "none")
        (set! (.-fillStyle context) gradient)
        (.beginPath context)
        (.rect context 0 0 300 50)
        (.fill context)
        (set! (.-fillStyle context) "none")
        (dorun
         (for [arr  (range -5 6 0.5)
               :let [
                     val   (-> arr
                               (* 2)
                               (+ angle)
                               (/ 10)
                               (* scale))
                     left  (-> arr
                               (- 0.05)
                               (* 2)
                               (+ angle) ; [0 1)
                               (/ 10) ; results in [-1 1]
                               (* scale) ; results in [-pi/3 pi/3]
                               (js/Math.sin) ; results in [-sqrt(3)/2
                               ; sqrt(3)/2]
                               (/ (/ (js/Math.sqrt 3) 2))) ; results in [-1 1]
                     right (-> arr
                               (+ 0.05)
                               (* 2)
                               (+ angle)
                               (/ 10)
                               (* scale)
                               (js/Math.sin)
                               (/ (/ (js/Math.sqrt 3) 2)))
                     width (* 100 (- right left))
                     pos   (-> val
                               (js/Math.sin)
                               (/ (/ (js/Math.sqrt 3) 2))
                               (* (/ slider-width 2))
                               (+ (/ slider-width 2)))
                     ]]
           (do (set!
                (.-strokeStyle context)
                ;; (str "rgba(0, 0, 0, " (- 1 (js/Math.abs (js/Math.sin val))) ")")
                "rgba(0 0 0 / 0.75)"
                )

               (set! (.-lineWidth context) width)
               (.beginPath context)
               (.moveTo context pos 0)
               (.lineTo context pos slider-height)
               (.stroke context)))) )
      nil)))

(def canvas-style {:margin "0 2rem"
                   :border-radius "4px"
                   :border-top "1px solid rgba(255 255 255 / 0.2)"
                   :border-bottom "1px solid rgba(0 0 0 / 0.4)"})

(defn slider-canvas
  [current-tempo mouse-down? control-select]
  (let [[touch-init set-touch-init] (react/useState 0)
        [tempo-on-touch set-tempo-on-touch] (react/useState current-tempo)
        [slider-angle set-slider-angle] (react/useState 0)
        ref (react/useRef nil)
        on-move
        (react/useCallback
         (fn [e]
           (if mouse-down?
             (let [touch?        (some? (.. e -touches))
                   touch         (if touch? (.. e -touches (item 0) -clientX) (* 1 (.. e -clientX)))
                   diff          (- touch-init touch)
                   dispatch-name (case control-select
                                   :main        :poly/set-tempo
                                   :numerator   :poly/set-numerator-tempo
                                   :denominator :poly/set-denominator-tempo)]
               (set-slider-angle (mod (/ diff -20) 1))
               (dispatch [dispatch-name
                          (.add tempo-on-touch (js/Math.round (/ diff 20)))]))
             nil))
         (array touch-init tempo-on-touch mouse-down? set-slider-angle))
        on-start (react/useCallback
                  (fn [e]
                    (let [touch? (some? (.. e -touches))
                          touch  (if touch? (.. e -touches (item 0) -clientX) (.. e -clientX))]
                      (dispatch [:mouse/set-down true])
                      (set-touch-init touch)
                      (set-tempo-on-touch current-tempo)))
                  (array current-tempo mouse-down?))
        _ (react/useEffect (fn [] (update-canvas slider-angle ref) js/undefined)
                           (array slider-angle))]
    [:canvas
     (use-style canvas-style
                {:id            "slider-canvas"
                 :ref           #(set! (.-current ref) %)
                 :width         slider-width
                 :height        slider-height
                 :on-touch-start on-start
                 :on-mouse-down on-start
                 :on-touch-move on-move
                 :on-mouse-move on-move
                 :on-mouse-up   #(dispatch [:mouse/set-down true])})]))

(def no-spinner {:appearance "none" :margin 0 :display "none"})

(def spinner-selector
  [["& input::-webkit-outer-spin-button, & input::-webkit-inner-spin-button"
    no-spinner]
   ["input[type=number]" {:appearance "textfield"}]])

(defn- tempo-field-style
  [mobile? control?]
  {:padding         (if mobile? "0" "2rem 0 1rem")
   ::stylefy/manual (merge (mui-override-style control? "4rem") spinner-selector)})

(def tempo-input-style {:margin "0 1rem"})


(defn tempo-control
  [mobile? control-select]
  (let [input    @(subscribe [:poly/input-value])
        control? (= :main control-select)
        changeFn #(dispatch [:poly/set-tempo %])
        inputFn  #(dispatch [:poly/update-input [%]])
        wheelFn  (fn [ev]
                   (-> ev .-target .focus)
                   (let [in-delta  (.-deltaY ev)
                         out-delta (if (pos? in-delta) -1 1)]
                     (dispatch [:poly/update-tempo out-delta])))]
    [:div
     (use-style (tempo-field-style mobile? control?))
     [:>
      Text-Field
      (use-style tempo-input-style
                 {:key         "tempo"
                  :variant     "outlined"
                  :margin      "dense"
                  :label       "group-bpm"
                  :name        "tempo"
                  :value       input
                  :on-focus    #(dispatch [:poly/set-control-select :main])
                  :on-wheel    wheelFn
                  :on-change   #(inputFn (.. % -target -value))
                  :on-blur     #(changeFn input)
                  :on-submit   #(changeFn input)
                  :on-key-down #(if (= (.-key %) "Enter")
                                  (changeFn input)
                                  nil)})]]))

(defn num-tempo
  [mobile? control-select]
  (let [input    @(subscribe [:poly/input-value :numerator])
        control? (= :numerator control-select)
        changeFn #(dispatch [:poly/set-numerator-tempo %])
        inputFn  #(dispatch [:poly/update-input [% :numerator]])
        wheelFn  (fn [ev]
                   (-> ev .-target .focus)
                   (let [in-delta  (.-deltaY ev)
                         out-delta (if (pos? in-delta) -1 1)]
                     (dispatch [:poly/update-numerator-tempo out-delta])))]
    [:div
     (use-style (tempo-field-style mobile? control?))
     [:>
      Text-Field
      (use-style tempo-input-style
                 {:key         "num-tempo"
                  :variant     "outlined"
                  :margin      "dense"
                  :label       "num-bpm"
                  :name        "numerator-tempo"
                  :value       input
                  :on-focus    #(dispatch [:poly/set-control-select :numerator])
                  :on-wheel    wheelFn
                  :on-change   #(inputFn (.. % -target -value))
                  :on-blur     #(changeFn input)
                  :on-submit   #(changeFn input)
                  :on-key-down #(if (= (.-key %) "Enter")
                                  (changeFn input)
                                  nil)})]]))

(defn den-tempo
  [mobile? control-select]
  (let [input    @(subscribe [:poly/input-value :denominator])
        control? (= :denominator control-select)
        changeFn #(dispatch [:poly/set-denominator-tempo %])
        inputFn  #(dispatch [:poly/update-input [% :denominator]])
        wheelFn  (fn [ev]
                   (-> ev .-target .focus)
                   (let [in-delta  (.-deltaY ev)
                         out-delta (if (pos? in-delta) -1 1)]
                     (dispatch [:poly/update-denominator-tempo out-delta])))]
    [:div
     (use-style (tempo-field-style mobile? control?))
     [:>
      Text-Field
      (use-style tempo-input-style
                 {:key         "den-tempo"
                  :variant     "outlined"
                  :margin      "dense"
                  :label       "den-bpm"
                  :name        "denominator-tempo"
                  :value       input
                  :on-focus    #(dispatch [:poly/set-control-select :denominator])
                  :on-wheel    wheelFn
                  :on-change   #(inputFn (.. % -target -value))
                  :on-blur     #(changeFn input)
                  :on-submit   #(changeFn input)
                  :on-key-down #(if (= (.-key %) "Enter")
                                  (changeFn input)
                                  nil)})]]))

(def tempo-play-style
  {:text-align      "center"
   :display         "flex"
   :flex-direction  "row"
   :flex            "0 0 auto"
   :justify-content "space-around"
   :gap             "1rem"
   :align-items     "center"})

(def radio-style {:position "relative"
                  :color (color/as-hex (:1 colors))
                  ::stylefy/manual (mui-radio-style)})

(defn radio-group
  [control-select]
  (let [on-change (fn [e]
                    (let [val (keyword (.. e -target -value))]
                      (dispatch [:poly/set-control-select val])))]
    [:div
     (use-style tempo-play-style)

     [:>
      Radio
      (use-style radio-style
                 {:checked   (= control-select :denominator)
                  :value     "denominator"
                  :on-change on-change})]
     [:>
      Radio
      (use-style radio-style
                 {:checked   (= control-select :main)
                  :value     "main"
                  :on-change on-change})]
     [:>
      Radio
      (use-style radio-style
                 {:checked   (= control-select :numerator)
                  :value     "numerator"
                  :on-change on-change})]]))


(defn handle-play-click
  [_]
  (if (= (.-state audio-context) "suspended")
    (-> (.resume audio-context)
        (.then (play)))
    (play)))

(defn tempo-play-group
  []
  (let [mobile?        @(subscribe [:layout/mobile?])
        playing?       @(subscribe [:poly/playing?])
        control-select @(subscribe [:poly/control-select])
        current-tempo  (case control-select
                         :main        @(subscribe [:poly/tempo])
                         :numerator   @(subscribe [:poly/tempo :numerator])
                         :denominator @(subscribe [:poly/tempo :denominator]))
        mouse-down?    @(subscribe [:mouse/down?])
        ;; tempo         @(subscribe [:poly/input-value])
        ;; den           @(subscribe [:poly/input-value :denominator])
        ;; num           @(subscribe [:poly/input-value :numerator])
        button-height  (if mobile? 60 120)
        button-width   (if mobile? 95 120)]
    [:div
     (use-style nil)
     [:div
      (use-style tempo-play-style)
      [den-tempo mobile? control-select]
      [tempo-control mobile? control-select]
      [num-tempo mobile? control-select]]
     [radio-group control-select]
     [:div
      (use-style {:text-align "center" :margin "1rem auto"}
                 {:on-mouse-up #(dispatch [:mouse/set-down false])})
      [:f> slider-canvas current-tempo mouse-down? control-select]]
     [:div
      (use-style {:text-align "center"})
      (if playing?
        [pause-button {:on-click handle-play-click
                       :width    button-width
                       :height   button-height}]
        [play-button {:on-click handle-play-click
                      :width    button-width
                      :height   button-height}])]]))