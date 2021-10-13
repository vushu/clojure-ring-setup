(ns vushu.views.shared
  (:require [hiccup.element :refer [link-to]]))

(defn nav-bar [ { {:keys [role]} :identity}]
  [:nav {:class ""}
   [:ul {:class "nav-bar"}
    (if (= role "admin")
      [:li {:class "flex"}
       (link-to {:class "button bg-green"} "/users/new" "Create&nbspuser")
       ])
    [:li {:class "flex"}
     (link-to {:class "button bg-green"} "/" "Dashboard")
     ]
    ;[:li {:class ""}
     ;(link-to {:class "button bg-blue"} "/sign-out" "Sign out again")
     ;]
    [:li {:class "flex"}
     (link-to {:class "button bg-red"} "/sign-out" "Sign out")
     ]
    ]]
  )

