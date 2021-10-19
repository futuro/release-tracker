(ns justenough.software.release-tracker.client
  (:require [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]))

(defonce app (app/fulcro-app))

(defsc Root [this props]
  (dom/div "TODO"))

(defn ^:export init
  "This is our shadow-cljs entry point. See `shadow-cljs.edn` for how
  this is configured"
  []
  (app/mount! app Root "app")
  (js/console.log "Loaded"))

(defn ^:export refresh
  "This is a dev helper that shadow-cljs will call on every hot-reload
  of the source code."
  []
  (app/mount! app Root "app")
  (comp/refresh-dynamic-queries! app)
  (js/console.log "Hot reload"))
