 (ns app.nav
   (:require [reagent.core :as r]
             [re-frame.core :refer [subscribe]]
             [stylefy.core :as stylefy :refer [use-style]]
             [app.routes :refer [routes]]
             [app.styles :refer [navbar-height]]))

(def menu-ul-style
  {:list-style-type "none"
   :user-select "none"
   :text-transform "uppercase"
   :margin "0"
   :margin-right "2rem"
   :display "flex"
   :height (str navbar-height "px")})

(def menu-li-style
  {:display "inline-flex"
   :align-items "flex-end"
   :font-size "1.4rem"
   :text-align "center"
   :margin-top "1.5rem"
   ::stylefy/mode
   {:hover {:cursor "pointer"}}})

(defn menu-link-style [active?]
  (let [color (if active? "rgb(10, 66, 96)" "black")]
    {:color color
     :text-decoration "none"
     ::stylefy/mode {:active {:color color}
                     :visited {:color color}}}))

(def menu-link-text-style
  {:padding-top "0.5rem"
   :padding-bottom "0.6rem"})

(defn get-highlight-style [active?]
  {:background-color (if active? "rgb(78, 134, 164)" "none")
   :height "5px"})

(defn menu []
  [:ul
   (use-style menu-ul-style)
   (doall (for [route routes
                :let [active? (= @(subscribe [:route]) route)]]
            ^{:key route} [:li
                           (use-style menu-li-style)
                           [:a
                            (use-style
                             (menu-link-style active?)
                             {:href (str "/" (name route))})
                            [:div
                             (use-style
                              menu-link-text-style)
                             route]
                            [:div
                             (use-style
                              (get-highlight-style active?))]]]))])

(defn logo-svg []
  [:svg {:style {:display "none"}}
   [:symbol {:id "logo-template"}
    [:path {:d "M55.763 40.858L38 57.038l.352.412c2.5 3.478 4.425 6.478 5.775 8.997 1.347 2.52 2.022 4.914 2.022 7.18a10.48 10.48 0 0 1-.91 4.277 12.493 12.493 0 0 1-2.434 3.637 11.712 11.712 0 0 1-3.547 2.52 10.06 10.06 0 0 1-4.307.94c-1.603 0-3.09-.27-4.456-.82-1.37-.55-2.562-1.23-3.576-2.052-1.02-.82-1.816-1.723-2.403-2.695-.586-.98-.88-1.896-.88-2.757 0-.586.12-1.155.353-1.698.235-.55.547-1.05.938-1.498.392-.45.86-.803 1.408-1.056a4 4 0 0 1 1.7-.376c.82 0 1.563.238 2.226.73a3.594 3.594 0 0 1 1.35 1.96c.155.593.136 1.222-.06 1.908a7.998 7.998 0 0 1-.792 1.875c-.332.567-.644 1.008-.937 1.32-.293.314-.44.395-.44.235 0 1.13 1.017 1.95 3.05 2.462l.116.06c.39.116.82.175 1.29.175.74 0 1.482-.163 2.225-.495a7.204 7.204 0 0 0 1.993-1.322 6.74 6.74 0 0 0 1.435-1.932c.372-.745.56-1.526.56-2.348 0-1.604-.715-3.47-2.14-5.6-1.43-2.13-3.51-4.87-6.246-8.234l-19.05 17.412-1.23-1.47 19.05-17.35-1.817-2.286c-1.328-1.525-2.432-2.938-3.31-4.25-.88-1.313-1.593-2.543-2.14-3.692-.548-1.15-.937-2.248-1.173-3.282a14.265 14.265 0 0 1-.35-3.134c0-1.49.312-2.833.936-4.046a9.392 9.392 0 0 1 2.52-3.082c1.055-.84 2.286-1.493 3.694-1.96 1.407-.47 2.89-.7 4.455-.7 1.407 0 2.774.23 4.103.7 1.33.468 2.512 1.084 3.547 1.847 1.035.763 1.855 1.612 2.462 2.548.606.94.908 1.898.908 2.872 0 1.254-.382 2.29-1.143 3.11-.765.82-1.674 1.23-2.73 1.23-1.13 0-2.04-.333-2.725-.996-.685-.662-1.025-1.68-1.025-3.047 0 .115-.02.038-.057-.234-.04-.27-.012-.635.088-1.084.097-.45.284-.91.558-1.383.272-.467.723-.84 1.348-1.112 0-.233-.205-.485-.615-.757-.41-.277-.91-.52-1.492-.735a12.705 12.705 0 0 0-1.906-.525 9.328 9.328 0 0 0-1.905-.21c-.508 0-1.035.15-1.582.44a6.03 6.03 0 0 0-1.496 1.145 6.195 6.195 0 0 0-1.115 1.612c-.294.605-.44 1.202-.44 1.788 0 .98.314 2.018.94 3.134.624 1.116 1.444 2.318 2.46 3.606.272.315.762.907 1.465 1.784.704.883 1.878 2.242 3.518 4.074l.47.706L54.53 39.45l1.233 1.408z"}]
    [:circle {:cx 17 :cy 60 :r 3.2}]
    [:circle {:cx 50 :cy 60 :r 3.2}]
    [:path {:d "M60 0C26.863 0 0 26.863 0 60s26.863 60 60 60 60-26.863 60-60S93.137 0 60 0zm-1 114.207C29.4 113.805 5.524 89.695 5.524 60 5.524 30.304 29.4 6.196 59 5.793v108.414zm30.31-29.11c-13.066 0-22.187-11.325-22.187-25.855 0-14.102 8.51-24.145 21.58-24.145 8.61 0 16.11 4.06 16.11 10.362 0 3.736-3.04 6.517-6.384 6.517-3.648 0-7.195-2.78-7.195-6.52 0-2.455 1.216-4.913 3.75-7.37-1.317-.32-2.633-.534-3.852-.534-8.105 0-12.968 8.442-12.968 20.3 0 12.18 5.37 24.358 13.07 24.358 4.355 0 9.018-3.52 13.88-10.47-1.62 6.516-9.22 13.355-15.805 13.355z"}]]])

(defn logo-instance [props]
  [:svg (merge
         props
         {:xmlns "https://www.w3.org/2000/svg"
          :viewBox "0 0 120 120"})
   [:use {:xlinkHref "#logo-template"}]])

(def navbar-style
  {:height (str navbar-height "px")
   :box-shadow "rgba(0, 0, 0, 0.3) 0px 0px 6px 1px;"
   :position "fixed"
   :top "0"
   :left "0"
   :width "100%"
   :background-color "white"
   :color "rgb(10, 66, 96)"
   :display "flex"
   :align-items "flex-start"
   :justify-content "space-between"
   :font-family "lato-light, sans-serif"
   :letter-spacing "0.05rem"})

(def navbar-logo-style
  {:width "150px"
   :height "150px"
   :float "left"
   :fill "rgb(10, 66, 96)"
   :-webkit-tap-highlight-color "transparent"})

(def logo-group-style
  {:display "inline-flex"
   :flex "1 1 auto"
   :height "100%"
   :align-items "center"
   :overflow "hidden"})

(defn logo-group [& children]
  [:div (use-style logo-group-style)
   children])

(def logo-text-style
  (let [height (str app.styles/navbar-height "px")]
    {:display "inline-block"
     :font-size "2.5rem"
     :margin-left "1.5rem"
     :vertical-align "middle"
     :line-height height
     :height height
     :text-transform "uppercase"}))

(def sub-text-style
  (let [height (str navbar-height "px")]
    {:display "inline-block"
     :vertical-align "middle"
     :font-size "1.8rem"
     :text-transform "uppercase"
     :line-height height
     :height height}))

(def flask-style
  {:height "60px"
   :margin "0.8rem"
   :margin-top "0"})

(defn navbar []
  [:div
   (use-style navbar-style)
   [logo-group
    ^{:key "logo"} [logo-instance (use-style navbar-logo-style)]
    ^{:key "text"} [:div (use-style logo-text-style)
                    [:span {:style {:vertical-align "middle"}} "SEAN CHEN"]]
    ^{:key "flask"} [:img (use-style flask-style {:src "/images/flask.svg"})]
    ^{:key "sub"} [:div (use-style sub-text-style)
                   [:span {:style {:vertical-align "middle"}} "labs"]]]
   [menu]])