(ns vushu.routes
  ( :require [reitit.ring :as ring]
             [vushu.views.home :as home]
             [vushu.views.layout :refer [main-layout]]
             [vushu.views.users :as users]
             [vushu.views.pages :as pages]
             [vushu.views.login :as login]
             [vushu.views.dashboard :as dashboard]
             [ring.middleware.params :as params]
             [ring.middleware.session :refer [wrap-session]]
             [ring.middleware.cookies :refer [wrap-cookies]]
             [ring.middleware.stacktrace :refer [wrap-stacktrace]]
             [muuntaja.core :as m]
             [reitit.ring.middleware.muuntaja :as muuntaja]
             [ring.util.response :refer [response redirect update-header header set-cookie]]
             [buddy.auth :refer [authenticated? throw-unauthorized]]
             [reitit.ring.coercion :as rcc]
             [reitit.coercion.spec]
             [buddy.auth.backends :as backends]
             [buddy.auth.backends.session :refer [session-backend]]
             [buddy.auth.accessrules :refer [wrap-access-rules]]
             [cljc.java-time.local-date :as ld]
             [buddy.sign.jwt :as jwt]
             [cljc.java-time.local-time :as lt]
             [cljc.java-time.instant :as ins]
             [buddy.core.keys :as keys]
             [buddy.sign.util :as util]
             [buddy.core.nonce :as nonce]
             [vushu.database :as db]
             [buddy.auth.middleware :refer [wrap-authentication
                                            wrap-authorization]]
             [buddy.hashers :as hasher]
             ))
(def request
  {:headers
   {"content-type" "application/edn"
    "accept" "application/transit+json"}
   :body "{:kikka 42}"})

(type #inst "2020-05-11")
(prn-str #inst "2020-05-11")

(defn hash-password [password] (hasher/derive password {:alg :argon2id}))

;;Must be a function or it wont renew
(defn exp [] (util/to-timestamp (ins/plus-seconds (ins/now) 3600)))


(defn authenticate-user [req]
  (if-not  (authenticated? req)
    (throw-unauthorized)))

(defn ping "pinging" [req]
  {:status 200, :body {:name "dan"}})

(defn ping "pinging" [req]
  {:status 200, :body "<h1>MAMA</h1>"})


;(def pubkey (keys/public-key "resources/pubkey.pem"))
;(def privkey (keys/private-key "resources/privkey.pem", "batman4ever"))

(def pubkey (keys/public-key "resources/test/pubkey.pem"))
(def privkey (keys/private-key "resources/test/privkey.pem"))

(def backend
  (backends/jwe {:secret privkey
                 :options {:alg :rsa-oaep
                           :enc :a128cbc-hs256}}))
(defn store-token-in-session [session token]
  (assoc (redirect "/")
         :session (assoc session :token token)))

(comment
  (defn update-header [req token]
    (let [headers (:headers req)
          updated (conj headers  {"Authorization: Token" token})]

      (println "updated stuffs" updated)
      (assoc req :headers updated))))

(defn password-matches-hashed [password hashed-password] (:valid (hasher/verify password hashed-password)))

(defn create-token [user]
  (let [claims {:email (:users/email user)
                :password (:users/password user)
                :role (:users/role user)
                :exp  (exp)}

        token (jwt/encrypt claims pubkey {:alg :rsa-oaep :enc :a128cbc-hs256})]
    token))

(defn login-handler [req]
  (if-some  [user (db/find-valid-user
                    (get-in req [:parameters :form :email])
                    (get-in req [:parameters :form :password]))]

    (set-cookie (redirect "/") "token" (create-token user) {:http-only true :same-site :lax})
    ))

(def paths

  [["/"

    ["" (fn [req] (main-layout dashboard/index  req :style "dashboard.css")) ]
    ;["" (partial main-layout (fn [req] [:h1 "Are you authenticated? " (authenticated? req) " Role: " (get-in req [:identity :role])])) ]
    ["dashboard" (partial main-layout dashboard/index)]
    ["login" {:get login/index
              :post {
                     :coercion reitit.coercion.spec/coercion
                     :parameters {:form {:email string? :password string?}}
                     :handler login-handler
                     }
              }]
    ["sign-out" (fn [req]
                  (set-cookie (redirect "/login") "token" "" {:max-age 0}))]
    ["users"
     ["" (partial main-layout users/index)]
     ["/list" (partial main-layout users/hej)]]

    ["api"
     ["" (partial main-layout pages/api)]
     ["/list" {:get ping}]
     ]]])

(defn echo [request]
  {:status 200
   :body (:body-params request)})

(defn on-error
  [request value]
  {:status 403
   :headers {}
   :body "You are not authorized!"})


(defn any-access [_] true)

(defn is-admin [req]
  (= (get-in req [:identity :role]) "admin"))

(def rules [
            {:pattern #"^/login$"
             :handler any-access}

            ;{:pattern #"^/dashboard"
            ;:handler (fn [req] (do (prn (:cookies req)) (some? (:cookies req))))}
            {:pattern #"^/users"
             :handler (fn [req] (is-admin req))
             }

            {:pattern #"^/.*"
             :handler (fn [req]
                        (println "is admin?" (is-admin req))
                        (and (authenticated? req) (is-admin req)))
             :on-error (fn [req _]
                         ;(dissoc :cookies)
                         (redirect "login"))}])

(defn get-token [request]
  (:value (get (:cookies request) "token")))

(defn cookie-token-to-header [handler] (fn [request]
                                         ;(println "==========================\r\n" (get-token request))
                                         ;(if-some [token (get-token request)])
                                         (let [req (header request "Authorization" (str "Token " (get-token request) ))]
                                           (handler req))))


(def options
  {:data {
          :muuntaja m/instance
          :middleware [

                       [wrap-stacktrace]
                       [wrap-cookies {:http-only true :same-site :lax}]
                       [cookie-token-to-header]
                       [wrap-authorization backend]
                       [wrap-authentication backend]

                       [wrap-access-rules {:rules rules :on-error on-error}]
                       params/wrap-params

                       muuntaja/format-middleware

                       rcc/coerce-exceptions-middleware
                       rcc/coerce-request-middleware
                       rcc/coerce-response-middleware
                       ]}})


(def router
  (ring/router paths options))

