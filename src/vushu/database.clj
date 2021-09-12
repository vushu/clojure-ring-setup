(ns vushu.database
  (:require [honey.sql :as sql]
            [honey.sql.helpers :as sql-helper
             :refer [select from drop-table columns values insert-into with-columns create-table where]]

            ;[honeysql-postgres.helpers :as psqlh]
            [cljc.java-time.instant :as instant]
            [buddy.sign.util :as util]
            [next.jdbc :as jdbc]
            [vushu.auth :refer [hash-password password-matches-hashed password-maches-users]]
            [next.jdbc.date-time :as sql-date]
            [heroku-database-url-to-jdbc.core :as h]
            ))

(def local-config
  {:dbtype "postgresql"
   :dbname "myapp"
   ;:host "172.17.0.1"
   :port  "5432"
   :user "postgres"
   :password "batman4ever"})

;:host "localhost"

(def database-url "jdbc:postgres://noawzrhubcobye:b9302cee8247f1a8e9ab0afc36bbbb388ceb97ab0bfd86c092114c4727d3659f@ec2-34-255-134-200.eu-west-1.compute.amazonaws.com:5432/dfk0b0u29ckfp0")

(def url "postgres://noawzrhubcobye:b9302cee8247f1a8e9ab0afc36bbbb388ceb97ab0bfd86c092114c4727d3659f@ec2-34-255-134-200.eu-west-1.compute.amazonaws.com:5432/dfk0b0u29ckfp0")

(def heroku-url (h/jdbc-connection-string url))

(def prod-config
  {:dbtype "postgresql"
   :jdbcUrl (h/jdbc-connection-string) (System/getenv "DATABASE_URL")})


(def test-config
  {:jdbcUrl heroku-url})

(def get-config
  (if (System/getenv "DATABASE_URL")
    prod-config
    local-config
    ))

(def db (jdbc/get-datasource test-config))

(def create-user-table
  (-> (create-table :users :if-not-exists)
      (sql-helper/with-columns [[:id :serial (sql/call :primary-key)]
                                [:email (sql/call :varchar 60) (sql/call :not nil) :unique]
                                [:password (sql/call :varchar 120)]])
      sql/format))


(defn create-all-table [] (jdbc/execute! db create-user-table))

(defn drop-all-tables [] (jdbc/execute! db (sql/format (drop-table :users))))

(comment
  (drop-all-tables)
  (create-all-table)
  )

(defn add-user [email password] (-> (insert-into :users)
                                    (columns :email :password)
                                    (values [[email (hash-password password)]])
                                    sql/format
                                    ))

(defn find-user-by-email-sql [email]
  (-> (select :*)
      (from :users)
      (where [:= :email email])
      sql/format))

(defn list-all-users-sql []
  (-> (select :*)
      (from :users)
      sql/format))


(defn find-user-by-email [email]
  (first (jdbc/execute! db (find-user-by-email-sql email))))

(defn find-valid-user [email password]
  (if-some [user (find-user-by-email email)]
    (when ( password-matches-hashed password (:users/password user))
      user)))



(defn remove-user-by-email [email]
  (->
    (sql-helper/delete-from :users)
    (where [:= :email email])))

(defn execute [query]
  (jdbc/execute! db query))

(comment
  (-> (list-all-users-sql)
      (execute))

  (-> (remove-user-by-email "danvu.hustle@gmail.com")
      sql/format
      (execute)))

;(let [user (find-user-by-email "danvu.hustle@gmail.com")]
;(password-matches-hashed "123123" (:users/password user)))

;(-> (add-user "danvu.hustle@gmail.com", "123123")
    ;(execute))







