(ns vushu.repl
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [rebel-readline.main :as rebel]
            [cider.nrepl :refer [cider-nrepl-handler]]
            [nrepl.server :as server]
            [nrepl.cmdline :refer [save-port-file]]))

(defn run-repl []
  (println "running nrepl-server then rebel repl")
  (save-port-file (server/start-server :handler cider-nrepl-handler) {})
  (rebel/-main))

(defn -main []
  (run-repl))


