(ns justenough.software.release-tracker.application
  (:require [com.fulcrologic.fulcro.application :as app]))

(defonce tracker-app
  (app/fulcro-app))
