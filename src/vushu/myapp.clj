(ns vushu.myapp
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.params :refer [wrap-params]]
            [vushu.repl :refer [run-repl]]
            [reitit.ring.coercion :as coersion]
            [reitit.ring :as ring]
            [vushu.routes :refer [router]]
            [muuntaja.middleware :as middleware]
            [muuntaja.core :as m]
            [buddy.auth.accessrules :refer [wrap-access-rules]]
            [buddy.auth :refer [authenticated?]]
            [buddy.auth.middleware :refer [wrap-authentication
                                           wrap-authorization]]

            [ring.util.response :refer [response redirect]]
            [buddy.auth.backends.session :refer [session-backend]]
            [reitit.ring.coercion :as rcc]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            )
  (:gen-class))

(defn any-access [_] true)

(def user {:id "bob" :pass "secret"})

(def rules [{:pattern #"^/login$"
             :handler any-access}
            {:pattern #"^/.*"
             :handler (fn [req] (do (println "HANDLING SESSIon" (:session req)) (authenticated? req)))
             :on-error (fn [req _]
                         (when-not ( authenticated? req)
                           (redirect "login")))}])


(comment
  (def rules
    [{:uris ["/" "/api*"]
      :handler authenticated?}]))

(defn my-unauthorized-handler
  [request metadata]
  (-> (response "Unauthorized request")
      (assoc :status 403)))

(defn my-authfn
  [request authdata]
  (let [username (:username authdata)
        password (:password authdata)]
    username))

(comment
  (def backend (backends/basic {:realm "API"
                                :authfn my-authfn
                                :unauthorized-handler my-unauthorized-handler})))

(def backend (session-backend))

(defn my-authfn
  [request authdata]
  (let [username (:username authdata)
        password (:password authdata)]
    username))

(defn on-error
  [request value]
  {:status 403
   :headers {}
   :body "You are not authorized!"})

(def app
  (-> (ring/ring-handler router ring/default-options-handler {:middleware [wrap-session]})
      ;(wrap-access-rules {:rules rules :on-error on-error})
      ))

(def app-with-reload
  (wrap-reload #'app))

(defn start-server "Starting the server" []
  jetty/run-jetty)


(defn -main "dev mode"
  [& args]
  ;(run-repl)
  (jetty/run-jetty #'app-with-reload {:port 8080 :join? false}))
