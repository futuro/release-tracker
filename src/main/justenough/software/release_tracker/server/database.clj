(ns justenough.software.release-tracker.server.database
  (:require [datascript.core :as d]
            [mount.core :as mount :refer [defstate]]))

(def schema
  {:github.repo/releases {:db/valueType :db.type/ref
                          :db/cardinality :db.cardinality/many}
   :github.repo/full_name {:db/unique :db.unique/identity}
   :github.release/id {:db/unique :db.unique/identity}})

(defstate connection
  :start
  (d/create-conn schema))
