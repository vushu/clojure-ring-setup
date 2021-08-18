(ns vushu.database
  (:require [honey.sql :as sql]
            [cljc.java-time.instant :as instant]
            [buddy.sign.util :as util]
            [next.jdbc :as jdbc]
            [next.jdbc.date-time :as sql-date]))

(def db-config
  {:dbtype "postgresql"
   :dbname "postgres"
   :host "172.17.0.1"
   :user "postgres"
   :password "batman4ever"})



(def db (jdbc/get-datasource db-config))

(jdbc/execute! db (sql/format {:select [:*] :from [:rooms]}))



