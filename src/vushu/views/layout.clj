(ns vushu.views.layout
  (:require [hiccup.page :refer  [html5]]
            [ring.util.response :refer [response]]))


(defn main-layout [page req]
  (response
    (html5
      [:head [:link {:rel "stylesheet" :href "https://cdnjs.cloudflare.com/ajax/libs/milligram/1.4.1/milligram.css" }]]
      [:body
       [:div {:class "container"}
        (page req)]
       ])))


