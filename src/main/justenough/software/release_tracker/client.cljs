(ns justenough.software.release-tracker.client
  (:require [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.components :as comp]
            [justenough.software.release-tracker.application :refer [tracker-app]]
            [justenough.software.release-tracker.github :as github]
            [justenough.software.release-tracker.ui :as ui]
            [justenough.software.release-tracker.ui.user :as user]))

(defn ^:export init
  "This is our shadow-cljs entry point. See `shadow-cljs.edn` for how
  this is configured"
  []
  (app/mount! tracker-app ui/Root "app")
  (comp/transact! tracker-app [(github/create-ghub-client nil)])
  (comp/transact! tracker-app [(user/fetch-user nil)])
  (js/console.log "Loaded"))

(defn ^:export refresh
  "This is a dev helper that shadow-cljs will call on every hot-reload
  of the source code."
  []
  ;; re-mounting will cause forced UI refresh
  (app/mount! tracker-app ui/Root "app")
  ;; 3.3.0+ Make sure dynamic queries are refreshed
  (comp/refresh-dynamic-queries! tracker-app)
  (js/console.log "Hot reload"))
