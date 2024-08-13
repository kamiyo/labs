(ns app.polyrhythms.styles
  (:require [app.styles :refer [colors light-blue]]))

(defn mui-override-style
  ([control? width]
   [[:label.MuiFormLabel-root (if control? {:color (:1 colors)} {:color (:0 colors)})]
    [:label.Mui-focused {:color (:1 colors)}]
    [:.MuiOutlinedInput-input
     (merge
      {:text-align "center"
       :max-width  "5rem"}
      (if control? {:color (:1 colors)} {:color (:0 colors)}))]
    [:.MuiOutlinedInput-input.Mui-disabled {:-webkit-text-fill-color (:0 colors)}]
    [:.MuiOutlinedInput-input.Mui-disabled::placeholder {:color (:0 colors)}]
    [:.MuiOutlinedInput-root
     [:.MuiOutlinedInput-notchedOutline
      (if control?
        {:transition   "all 0.2s"
         :border-color (:1 colors)
         :color        (:1 colors)}
        {:transition   "all 0.2s"
         :border-color (:0 colors)
         :color        (:0 colors)}
      )]]
    [:.MuiOutlinedInput-root.Mui-disabled
     [:.MuiOutlinedInput-notchedOutline {:transition   "all 0.2s"
                                         :border-color (:0 colors)
                                         :color        (:0 colors)}]]
    [".MuiOutlinedInput-root:not(.Mui-disabled):hover"
     [:.MuiOutlinedInput-notchedOutline {:transition   "all 0.2s"
                                         :border-color (:1 colors)
                                         :color        (:1 colors)}]]
    [:.MuiOutlinedInput-root.Mui-focused
     [:.MuiOutlinedInput-notchedOutline {:transition   "all 0.2s"
                                         :border-color (:blue colors)
                                         :color        (:blue colors)}]]
    [:.MuiOutlinedInput-inputMarginDense
     (merge
      (if (some? width) {:width width} nil))]])
  ([mobile?] (mui-override-style mobile? nil)))

(defn mui-radio-style
  []
  [[:&.MuiRadio-root {:color (:0 colors)}]
   [:&.MuiRadio-root.Mui-checked {:color (:1 colors)}]])