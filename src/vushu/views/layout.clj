(ns vushu.views.layout
  (:require [hiccup.page :refer  [html5]]
            [ring.util.response :refer [response]]))


(defn main-layout [page req]
  (response
    (html5
      [:head [:link {:rel "stylesheet" :href "https://cdnjs.cloudflare.com/ajax/libs/bulma/0.9.3/css/bulma.min.css" }]]
      [:body
       [:section {:class "section"}
        [:div {:class "container"}
         (page req)]]
       ])))


