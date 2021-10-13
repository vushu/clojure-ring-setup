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
  [:div {:class "sign-in-grid"}
   [:div {:class "top-bar flex flex-center-both"} "Vu Portal"]
   [:div {:class "middle" }
    ;[:div {:class "header bg-danger" :style "height: 10em"} "sdfdsa"]
    (form/form-to [:post "/login"]
                  (when-not (blank? (:email params))
                    (form/label "" "No such user exists! try again"))
                  [:div {:class "flex flex-center"}
                   (form/email-field {:class "text-field" :placeholder "Email" :type "email"} "email" (:email params))]
                  [:div {:class "flex flex-center"}
                   (form/password-field {:class "text-field" :placeholder "Password"} "password" (:password params))]
                  [:div {:class "flex flex-center"}
                   (form/submit-button {:class "button bg-green mt-3 "} "Log in")])]
   [:div {:class "bot-bar" } ""]]
  ;[:div {:class "footer bg-danger" :style "height: 10em"} "hsss"]
  )


;(defn login-view "doc-string" [params]
;[:div {:class "vf vf-col vf-center"};
;[:div {:class "top-bar"}
;"Vushu"]
;[:div {:class "middle" }
;;[:div {:class "header bg-danger" :style "height: 10em"} "sdfdsa"]
;(form/form-to [:post "/login"]
;[:fieldset
;(when-not (blank? (:email params))
;(form/label "" "No such user exists! try again"))
;[:div {:class "vf vf-center vf-col"}
;(form/text-field {:class "mb-3" :placeholder "Email" :type "email"} "email" (:email params))
;(form/password-field {:class "mb-3" :placeholder "Password"} "password" (:password params))]
;[:div {:class "vf vf-center"}
;(form/submit-button {:class "w-50"} "Sign-in")]])]

;[:div {:class "bot-bar" }]]
;;[:div {:class "footer bg-danger" :style "height: 10em"} "hsss"]
;)


(defn index [req]
  (main-layout
    login-view req :style "sign-in.css"))

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

(defn show-login-view [email password]
  (println "re-render login")
  (main-layout login-view {:email email :password password}))

(defn set-user [session username password]
  (assoc (redirect "/users")
         :session (assoc session :identity user)))

(def post {
           :coercion reitit.coercion.spec/coercion
           :parameters {:form {:email string? :password string?}}
           :session {:identity string?}
           :responses {
                       ;200 { :body {:message string?}}
                       ;400 {:body {:message string?}}
                       }
           :handler (fn [req]
                      (let [username (-> req :parameters :form :email)
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


