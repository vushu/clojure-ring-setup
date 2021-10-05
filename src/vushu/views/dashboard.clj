(ns vushu.views.dashboard
  (:require [hiccup.element :refer [link-to]]
            [ring.util.response :refer [get-header]]
            [hiccup.element :refer [link-to]]
            ))


(defn index [req]
  [:div {:class ""}
   [:nav
    [:ul {:class "nav-bar"}
     [:li {:class "flex"}
      (link-to {:class "button bg-red"} "sign-out" "Sign out")
      ;[:pre {:style "height: 100%"}
      ;[:code {:style ""} (:cookies req)]]]]
      ]
     [:li {:class "flex"}
      (link-to {:class "button bg-green"} "/" "Create&nbspuser")
      ]]]
   ]
  )
