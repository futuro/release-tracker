(ns justenough.software.release-tracker.server
  (:require [justenough.software.release-tracker.config :as cfg]
            [justenough.software.release-tracker.server.middleware :as middleware]
            [mount.core :as mount :refer [defstate]]
            [org.httpkit.server :as http-kit]
            [taoensso.timbre :as log]))

(defstate http-server
  :start
  (let [cfg (::http-kit/config cfg/config)]
    (log/info "Starting HTTP server with config " cfg)
    (http-kit/run-server middleware/middleware cfg))
  :stop (http-server))
