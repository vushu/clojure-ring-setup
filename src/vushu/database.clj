(ns vushu.database
  (:require [honey.sql :as sql]
            [honey.sql.helpers :as sql-helper
             :refer [select from drop-table columns values insert-into with-columns create-table where]]
            [cljc.java-time.instant :as instant]
            [buddy.sign.util :as util]
            [next.jdbc :as jdbc]
            [vushu.auth :refer [hash-password password-matches-hashed password-maches-users]]
            [next.jdbc.date-time :as sql-date]
            [heroku-database-url-to-jdbc.core :as h]))

(def local-config
  {:dbtype "postgresql"
   :dbname "myapp"
   ;:host "172.17.0.1"
   :port  "5432"
   :user "postgres"
   :password "batman4ever"})

(defn prod-config []
  {:jdbcUrl (h/jdbc-connection-string (System/getenv "DATABASE_URL"))})

(def get-config
  (if (System/getenv "DATABASE_URL")
    (prod-config)
    local-config))

(def db (jdbc/get-datasource local-config))

(def create-user-table
  (-> (create-table :users :if-not-exists)
      (sql-helper/with-columns [[:id :serial (sql/call :primary-key)]
                                [:email (sql/call :varchar 60) (sql/call :not nil) :unique]
                                [:password (sql/call :varchar 120)]
                                [:role [:varchar :15] [:not nil]]])
      sql/format))


(def create-roles-table
  (-> (create-table :roles :if-not-exists)
      (sql-helper/with-columns [[:id :serial (sql/call :primary-key)]
                                [:user_id :int [:references :users :id]]
                                [:name [:varchar :60] (sql/call :not nil) :unique]])
      sql/format))


(defn create-all-table []
  (jdbc/execute! db create-user-table)
  ;(jdbc/execute! db create-roles-table)
  )


(defn drop-all-tables [tables]
  (map (fn [table]
         (->> (drop-table table)
              sql/format
              (jdbc/execute! db))) tables))


(defn add-user [email password role] (-> (insert-into :users)
                                         (columns :email :password :role)
                                         (values [[email (hash-password password) role]])
                                         sql/format
                                         ))

(defn add-role [role] (-> (insert-into :roles)
                          (columns :name)
                          (values [[role]])
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

(defn seed []
  (-> (add-user "danvu.hustle@gmail.com", "batman4ever", "admin")
      (execute))

  (-> (add-user "user@vushu.com", "batman4ever", "user")
      (execute)))


(comment
  (drop-all-tables [:users])
  (create-all-table)
  (seed)
  (use 'clojure.stacktrace)
  (print-stack-trace *e 5)
  )








