(ns justenough.software.release-tracker.server.database
  (:require [datascript.core :as d]
            [mount.core :as mount :refer [defstate]]))

(defstate connection
  :start
  (d/create-conn))
