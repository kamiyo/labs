(ns app.styles
  (:require [stylefy.core :refer [font-face]]
            [garden.color :as color]))

(def light-blue "rgb(78, 134, 164)")
(def light-blue-transparent "rgb(78, 134, 164, 0.08)")
(def dark-blue "rgb(10, 66, 96)")

(def colors
  {:-3 "#151617"
   :-2 (color/lighten "#151617" 5)
   :-1 "#24434D"
   :0  (color/lighten "#79a0ae" 7)
   :1  (color/lighten "#c1c8c2" 7)
   :2  (color/lighten "#faeab4" 5)})

(font-face {:font-family "lato-light"
            :src "url('/fonts/lato-light.woff2') format('woff2')"
            :font-weight "normal"
            :font-style "normal"})