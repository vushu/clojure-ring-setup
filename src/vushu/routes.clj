(ns vushu.routes
  ( :require [reitit.ring :as ring]
             [vushu.views.home :as home]
             [vushu.views.layout :refer [main-layout]]
             [vushu.views.users :as users]
             [vushu.views.pages :as pages]
             [vushu.views.login :as login]
             [ring.middleware.params :as params]
             [ring.middleware.session :refer [wrap-session]]
             [muuntaja.core :as m]
             [reitit.ring.middleware.muuntaja :as muuntaja]
             [ring.util.response :refer [response redirect]]
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
             [buddy.auth.middleware :refer [wrap-authentication
                                            wrap-authorization]]
             ))
(def request
  {:headers
   {"content-type" "application/edn"
    "accept" "application/transit+json"}
   :body "{:kikka 42}"})

(type #inst "2020-05-11")
(prn-str #inst "2020-05-11")

(def exp (util/to-timestamp (ins/plus-seconds (ins/now) 3600)))

;(lt/now)
(def timer (lt/plus-seconds (lt/now) 3600))
(lt/now)

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

(defn login-handler [req]
  (let [claims {:user "DAN"
                :exp  exp}
        token (jwt/encrypt claims pubkey {:alg :rsa-oaep :enc :a128cbc-hs256})]

    ;{:status 200
    ;:body (m/encode "application/transit+json" token)
    ;}
    ( response {:token token})
    ))

(def paths

  [["/"

    ["" (partial main-layout (fn [req] [:h1 "Are you authenticated? " (authenticated? req) (:identity req)])) ]

    ["login" {:get login/index
              :post login-handler
              }]
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
;(def backend (backends/session))
;(def backend (session-backend))
;(def backend (backends/session))
(defn on-error
  [request value]
  {:status 403
   :headers {}
   :body "You are not authorized!"})


(defn any-access [_] true)

(def rules [{:pattern #"^/login$"
             :handler any-access}
            {:pattern #"^/.*"
             :handler (fn [req] (do (println "You may pass?" (:identity req) ) (authenticated? req)))
             :on-error (fn [req _]
                         (when-not ( authenticated? req)
                           (redirect "login")))}])


(def options
  {:data {
          :muuntaja m/instance
          :middleware [

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
