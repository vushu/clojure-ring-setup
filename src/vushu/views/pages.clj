(ns vushu.views.pages
  [:require [hiccup.element :refer [link-to]]])

(defn index [req]
  [:div "welcome to vushu" ])

(defn api [req]
  [:div "api" ])
