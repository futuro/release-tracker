(ns justenough.software.release-tracker.server.database
  (:require [asami.core :as d]
            [mount.core :as mount :refer [defstate]]
            [justenough.software.release-tracker.config :as cfg]))

(defn db-uri
  [{:keys [name]}]
  (format "asami:local://%s" name))

(defn create-db
  [dbcfg]
  (d/create-database (db-uri dbcfg)))

(defn connect
  [dbcfg]
  (d/connect (db-uri dbcfg)))

(defstate connection
  :start
  (let [{dbcfg :db} cfg/config]
    (create-db dbcfg)
    (connect dbcfg))
  :stop (d/release connection))
