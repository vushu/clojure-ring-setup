(ns vushu.views.login
  (:require [hiccup.page :refer [html5]]
            [ring.util.response :refer [response]]
            [vushu.views.layout :refer [main-layout]]
            [ring.middleware.session :refer [wrap-session]]
            [hiccup.form :as form]
            [ring.util.response :refer [redirect]]
            [reitit.ring.coercion :as rrc]
            [clojure.string :refer [blank?]]
            [reitit.coercion.spec]
            ))


(defn login-view "doc-string" [params]
  [:div {:style "padding-top: 11.6rem"}
   [:div {:class "row" :style "top:300px"}
    [:div.column.column-50.column-offset-25
     (form/form-to [:post "/login"]
                   (when-not (blank? (:username params))
                     (form/label "lbl" "No such user exists! try again"))
                   (form/text-field "username"  (:username params))
                   (form/password-field "password" (:password params))
                   (form/submit-button {:class "column column-33 column-offset-33 button"} "Sign-in"))
     ;(println "user -> " (:username params))
     ]]])

(defn index [req]
  (main-layout
    login-view req))

(defn handler-test [{:keys [parameters]}]
  (let [username (-> parameters :form :username)]
    {:status 200
     :body {:username username}}
    ))

(def user {:username "bob" :password "secret"})

(defn user-exists [inputs]
  (= [(:username user) (:password user)] inputs))

(def no-user {:status 400
              :body {:message "User not found"}})

(def user-found {:status 200
                 :body {:message "User exists!"}})

(def fill-all
  {:status 403
   :body {:message "Please fill formular"}}
  )

(defn show-login-view [username password]
  (println "re-render login")
  (main-layout login-view {:username username :password password}))

(defn set-user [session username password]
  (assoc (redirect "/users")
         :session (assoc session :identity user)))

(def post {
           :coercion reitit.coercion.spec/coercion
           :parameters {:form {:username string? :password string?}}
           :session {:identity string?}
           :responses {
                       ;200 { :body {:message string?}}
                       ;400 {:body {:message string?}}
                       }
           :handler (fn [req]
                      (let [username (-> req :parameters :form :username)
                            password (-> req :parameters :form :password)
                            session (-> req :session)]


                        (println "POST ::: session" (:session req))
                        (println "usersname:" username)
                        (println "password" password)
                        (if (every? not-empty [username password])
                          (if (user-exists [username password])
                            (assoc (redirect "/")
                                   :session (assoc session :identity (keyword username)))
                            (show-login-view username password) )
                          (show-login-view username password)
                          )
                        ))
           })


