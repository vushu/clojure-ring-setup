(ns vushu.views.dashboard
  (:require [hiccup.element :refer [link-to]]
            [ring.util.response :refer [get-header]]))

(defn index [req]
  [:div {:class "row" :style "height: 1000px"}
   [:div {:class "column column-50"}
    [:h1 "hello dashboard"]
    [:h1 "anything" (get-header req "location")]
    [:pre {:style "height: 100%"}
     [:code {:style "word-wrap: break-word"} (:cookies req)]]]]
  )
