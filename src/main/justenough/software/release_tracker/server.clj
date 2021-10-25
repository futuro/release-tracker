(ns justenough.software.release-tracker.server
  (:require [justenough.software.release-tracker.config :as cfg]
            [mount.core :as mount :refer [defstate]]
            [org.httpkit.server :as http-kit]))

(defstate http-server
  :start
  (let [cfg (::http-kit/config cfg/config)]
    ))
