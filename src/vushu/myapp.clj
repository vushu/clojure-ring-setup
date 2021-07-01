(ns vushu.myapp
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [rebel-readline.main :as rebel]
            [cider.nrepl :refer [cider-nrepl-handler]]
            [nrepl.server :as server]
            [nrepl.cmdline :refer [save-port-file]]
            )
  (:gen-class))

(defn test []
  (println "hejd med"))

(defn greet
  "Callable entry point to the application."
  [data]
  (println (str "Hello, " (or (:name data) "World") "!")))

(defn start-server "Starting the server" []
  jetty/run-jetty)

(defn welcome
  "A ring handler to process all requests sent to the webapp"
  [request]
  {:status  200
   :headers {}
   :body    "<h1>Hello, Clojure World</h1>
            <p>Welcome to first Clojure app.
            This message is returned regardless of the request, sorry<p>"})


(defn run-repl []
  (println "running repl on port 7888")
  (save-port-file (server/start-server :handler cider-nrepl-handler) {})
  (rebel/-main))

(defn -main "dev mode"
  [& args]
  (run-repl)
  (jetty/run-jetty
    (wrap-reload #'welcome) {:port (Integer. "8080")
                             :join? false}))

( comment
  (defn -main
    [& args]
    (jetty/run-jetty
      welcome
      {:port (Integer. "8080")})))


