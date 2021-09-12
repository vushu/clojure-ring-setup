(ns vushu.auth
    (:require [ring.util.response :refer  [response]]
              [buddy.hashers :as hasher]))

(defn hash-password [password] (hasher/derive password {:alg :argon2id}))

(defn password-matches-hashed [password hashed-password]
    (:valid (hasher/verify password hashed-password)))

(defn password-maches-users [incomming-password user-password]
    (password-matches-hashed incomming-password user-password))




