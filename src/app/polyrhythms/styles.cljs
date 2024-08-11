(ns app.polyrhythms.styles
  (:require [app.styles :refer [light-blue]]))

(defn mui-override-style
  ([mobile? width]
   [[:label.Mui-focused {:color light-blue}]
    [:.MuiOutlinedInput-root.Mui-focused
     [:.MuiOutlinedInput-notchedOutline {:border-color light-blue
                                         :color light-blue}]]
    [:.MuiOutlinedInput-inputMarginDense (merge
                                          (if (some? width) {:width width} nil))]])
  ([mobile?] (mui-override-style mobile? nil)))