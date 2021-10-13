(ns vushu.views.users
  (:require [hiccup.core :refer [html]]
            [vushu.views.shared :refer [nav-bar]]
            [hiccup.form :as form]))

(defn index [req]
  [:div [:h1 "users INdex"]])

(defn hej [req]
  [:div [:h1 "hej med hej"]])

(defn new-user [req]
  [:div
   [:div {:class ""} (nav-bar req)]
   [:div {:class "flex flex-center-both vh90"}
    (form/form-to [:post "/users/create"]
                  ;(form/label {:class "flex "} "email" "email:")
                  (form/email-field {:class "text-field"} "email" )
                  (form/password-field {:class "text-field"} "password" )
                  [:div {:class "flex flex-center"}
                   (form/submit-button {:class "button bg-green mt-3"} "Create user")]
                  )

    ]])


