(ns vushu.views.layout
  (:require [hiccup.page :refer  [html5]]
            [ring.util.response :refer [response]]))


(defn main-layout
  [page req & {:keys [style] :or {style "/app.css"}}]
  (response
    (html5
      [:head
       [:link {:rel "stylesheet" :href style}]
       [:link {:rel "stylesheet" :href "/utils.css"}]
       ;[:link {:rel "stylesheet" :href "https://unpkg.com/awsm.css/dist/awsm_theme_big-stone.min.css"}]
       ;[:link {:rel "stylesheet" :href "https://unpkg.com/awsm.css/dist/awsm_theme_black.min.css"}]
       ;ss

       ;[:link {:href "https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/css/bootstrap.min.css" :rel "stylesheet" :integrity "sha384-F3w7mX95PdgyTmZZMECAngseQB83DfGTowi0iMjiWaeVhAn4FJkqJByhZMI3AhiU" :crossorigin "anonymous" } ]
       ;[:script {:src "https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/js/bootstrap.bundle.min.js" :integrity "sha384-/bQdsTh/da6pkI1MST/rWKFNjaCP5gBSY4sEBT38Q/9RBh9AH40zEOg7Hlq2THRZ" :crossorigin "anonymous"}]
       [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]]

      [:body
       (page req)]
      )))


