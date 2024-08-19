(ns app.polyrhythms.visualizer
  (:require [app.styles :refer [colors]]
            [clojure.string :as str]
            [re-frame.core :refer [subscribe]]
            [stylefy.core :refer [use-style]]))


(defn- get-ticks-children-style
  [idx idy span num]
  (let [column-start (-> idx
                         (* span)
                         (+ 2))
        bottom?      (= 1 idy)]
    {:grid-column  (str column-start " / span " span)
     :grid-row     (inc idy)
     :border-left  (if (zero? idx) "solid 2px" "solid 1px")
     :border-right (if (= idx (dec num)) "solid 2px" "solid 1px")
     :min-height   0
     :border-color (:0 colors)
     (if bottom? :border-bottom :border-top) "solid 1px"}))

(defn- generate-ticks
  [ticks total which]
  (let [y    (condp = which :numerator 1 :denominator 2)
        span (/ total ticks)]
    [:<>
     (doall (for [x (range 0 ticks)]
              ^{:key (str y x)}
              [:div
               (use-style (get-ticks-children-style x y span ticks)
                          {:id (str (dec y) x)})]))]))

(defn- get-minor-tick-style
  [idx verbose? mobile?]
  {:grid-column      (+ idx 2)
   :grid-row         "3"
   :color            "#777777"
   :justify-self     "start"
   :transform        "translate(-50%, -50%)"
   :display          "inline-flex"
   :flex-direction   "column"
   :align-items      "center"
   :justify-content  "center"
   ;;  :text-shadow     (str/join ", " (take 30 (repeat "0 0 4px #ffffff")))
   :background-color (:-3 colors)
   :border-radius    "4px"
   :font-size        (if mobile? "0.8rem" "1.0rem")
   :line-height      (if mobile? "1.2rem" "1.8rem")
   :font-weight      "normal"
   :height           (if verbose? "fit-content" "1.5rem")
   :width            "1.5rem"
   :overflow         "visible"
   :text-align       "center"})

(defn- generate-sequential-numbers
  [x numerator denominator]
  [:<>
   ^{:key "numerator"} [:div (inc (mod x denominator))]
   ^{:key "sequential"} [:div (inc x)]
   ^{:key "denominator"} [:div (inc (mod x numerator))]])

(defn- generate-cyclical-numbers [x numerator _] [:div (inc (mod x numerator))])

(defn- generate-minor-ticks
  [ticks numerator denominator]
  (let [verbose?   @(subscribe [:poly/verbose?])
        mobile?    @(subscribe [:layout/mobile?])
        display-fn (if verbose?
                     generate-sequential-numbers
                     generate-cyclical-numbers)]
    [:<>
     (doall (for [x (range 0 ticks)]
              ^{:key (str "minor" x)}
              [:div
               (use-style (get-minor-tick-style x verbose? mobile?))
               (display-fn x numerator denominator)]))]))

(defn- get-number-style
  [idx idy span]
  (let [column-start (-> idx
                         (* span)
                         (+ 2))]
    {:grid-column  (str column-start " / span " span)
     :grid-row     (inc idy)
     :min-height   0
     :justify-self "start"
     :align-self   "center"
     :padding      "1rem 0"
     :font-size    "1.2rem"
     :line-height  "1.2rem"
     :transform    "translateX(-50%)"}))

(defn- increment-cyclical [modulus amount x] (inc (mod (* amount x) modulus)))

(defn- generate-numbers
  [args]
  (let [{:keys [ticks total which class]} args
        y      (condp = which :numerator 0 :denominator 3)
        span   (/ total ticks)
        inc-fn (if (not @(subscribe [:poly/verbose?]))
                 (if (= :numerator which)
                   (partial increment-cyclical ticks span)
                   (constantly 1))
                 inc)]
    [:<>
     (doall (for [x (range 0 ticks)]
              ^{:key (str "number" y x)}
              [:div
               (use-style (get-number-style x y span) {:class class})
               (inc-fn x)]))]))

(defn- get-visual-beep-container-style
  [vert-pos horiz-pos divisions]
  (let [which-row    (condp = vert-pos :top 1 :bottom 4)
        which-column (condp = horiz-pos :left 1 :right (+ 2 divisions))]
    {:grid-row        which-row
     :grid-column     which-column
     :display         "flex"
     :justify-content "center"
     :align-items     "center"
     :flex            "1 0 auto"}))

(def visual-beep-style
  {:width         "0.5rem"
   :height        "0.5rem"
   :border-radius "0.5rem"
   :background-color (:0 colors)})

(defn- generate-visual-beep
  [divisions]
  [:<>
   (doall (for [vert  [:top :bottom]
                horiz [:left :right]]
            ^{:key (str "beep" vert horiz)}
            [:div
             (use-style (get-visual-beep-container-style vert horiz divisions))
             [:div
              (use-style visual-beep-style
                         {:class (str "beep " (name vert))})]]))])

(defn- get-grid-style
  [least-common-multiple mobile?]
  (let [row-template
        (if mobile?
          (str "minmax(0, 4fr) minmax(0, 3fr) minmax(0, 3fr) minmax(0, 4fr)")
          (str "minmax(0, 3fr) minmax(0, 4fr) minmax(0, 4fr) minmax(0, 3fr)"))
        beep-fr (condp <= least-common-multiple
                  60 "7fr"
                  50 "6fr"
                  40 "5fr"
                  30 "4fr"
                  "3fr")]
    {:color              (:0 colors)
     :background-color   (:-3 colors)
     :box-shadow         "inset 0 0 8px rgba(0 0 0 / 0.5)"
     :border-top         "1.5px solid transparent"
     :border-bottom      "1.5px solid transparent"
     :border-radius      "4px"
     :display            "grid"
     :grid-template-columns (str "minmax(0, "
                                 beep-fr
                                 ") repeat("
                                 least-common-multiple
                                 ", minmax(0, 3fr)) minmax(0, "
                                 beep-fr
                                 ")")
     :grid-template-rows row-template
     :margin             "0.2rem 0"
     :min-height         0
     :max-height         "500px"
     :flex               "1 1 auto"}))

(defn visualizer-grid
  []
  (let [numerator       @(subscribe [:poly/numerator-divisions])
        denominator     @(subscribe [:poly/denominator-divisions])
        total-divisions @(subscribe [:poly/lcm])
        mobile?         @(subscribe [:layout/mobile?])]
    [:div#grid
     (use-style (get-grid-style total-divisions mobile?))
     [generate-numbers {:ticks numerator
                        :total total-divisions
                        :which :numerator
                        :class "number numerator"}]
     [generate-ticks numerator total-divisions :numerator]
     [generate-ticks denominator total-divisions :denominator]
     [generate-numbers {:ticks denominator
                        :total total-divisions
                        :which :denominator
                        :class "number denominator"}]
     [generate-minor-ticks total-divisions numerator denominator]
     [generate-visual-beep total-divisions]]))