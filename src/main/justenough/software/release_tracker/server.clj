(ns justenough.software.release-tracker.server
  (:require [justenough.software.release-tracker.config :as cfg]
            [mount.core :as mount :refer [defstate]]))

(defstate http-server
  :start
  (let [cfg (::http-kit/config cfg/config)]
    ))
