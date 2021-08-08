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
             [buddy.auth.middleware :refer [wrap-authentication
                                            wrap-authorization]]
             ))
(def request
  {:headers
   {"content-type" "application/edn"
    "accept" "application/transit+json"}
   :body "{:kikka 42}"})

(defn authenticate-user [req]
  (if-not  (authenticated? req)
    (throw-unauthorized)))

(defn ping "pinging" [req]
  {:status 200, :body {:name "dan"}})

(defn ping "pinging" [req]
  {:status 200, :body "<h1>MAMA</h1>"})

(def paths

  [
   ["/"

    ["" (partial main-layout (fn [req] [:h1 "hej med dig"])) ]

    ["login" {:get login/index
              :post login/post
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

(def backend (backends/session))
;(def backend (session-backend))
;(def backend (backends/session))

(comment
  (def pubkey (keys/public-key "pubkey.pem"))
  (def privkey (keys/private-key "privkey.pem"))
  (def backend
    (backends/jwe {:secret "privkey.pem"
                   :options {:alg :rsa-oaep
                             :enc :a128cbc-hs256}})))
(defn on-error
  [request value]
  {:status 403
   :headers {}
   :body "You are not authorized!"})


(defn any-access [_] true)

(def rules [{:pattern #"^/login$"
             :handler any-access}
            {:pattern #"^/.*"
             :handler (fn [req] (do (println "HANDLING SESSIon" (:session req)) (authenticated? req)))
             :on-error (fn [req _]
                         (when-not ( authenticated? req)
                           (redirect "login")))}])


(comment
  params/wrap-params
  muuntaja/format-middleware)
(def options
  {:data {
          :muuntaja m/instance
          :middleware [
                       [wrap-authentication backend]
                       [wrap-authorization backend]
                       [ wrap-access-rules {:rules rules :on-error on-error}]

                       params/wrap-params
                       muuntaja/format-middleware

                       rcc/coerce-exceptions-middleware
                       rcc/coerce-request-middleware
                       rcc/coerce-response-middleware
                       ]}})


(def router
  (ring/router paths options))
