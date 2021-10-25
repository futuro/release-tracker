(ns justenough.software.release-tracker.config
  (:require [mount.core :refer [args defstate]]
            [com.fulcrologic.fulcro.server.config :as fsc]
            [taoensso.timbre :as log]))

(defn configure-logging! [{::log/keys [logging-config]}]
  (log/info "Configuring Timbre with " logging-config)
  (log/merge-config! logging-config))

(defstate config
  :start (let [{:keys [config]
                :or {config "config/dev.edn"}} (args)
               configuration (fsc/load-config! {:config-path config})]
           (log/info "Loaded config" config)
           (configure-logging!)
           configuration))
