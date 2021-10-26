(ns justenough.software.release-tracker.config
  (:require [mount.core :refer [args defstate]]
            [aero.core :as aero]
            [taoensso.timbre :as log]
            [clojure.java.io :as io]))

(defn configure-logging! [{::log/keys [logging-config]}]
  (log/info "Configuring Timbre with " logging-config)
  (log/merge-config! logging-config))

(defstate config
  :start (let [{:keys [env]
                :or   {env :dev}} (args)
               configuration    (-> "config/default.edn"
                                    io/resource
                                    (aero/read-config
                                     {:profile env}))]
           (log/info "Loaded config")
           (configure-logging! configuration)
           configuration))
