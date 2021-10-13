(ns vushu.views.dashboard
  (:require [hiccup.element :refer [link-to]]
            [ring.util.response :refer [get-header]]
            [vushu.views.shared :refer [nav-bar]]
            ))


(defn index [req] [:div {:class ""} (nav-bar req) ])
