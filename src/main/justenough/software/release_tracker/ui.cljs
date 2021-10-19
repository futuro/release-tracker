(ns justenough.software.release-tracker.ui
  (:require [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [justenough.software.release-tracker.secrets :as secrets]
            ["octokit" :as octokit]))

(defn ghub-client
  []
  (octokit/Octokit. #js {:auth secrets/user-token}))

(defmutation create-ghub-client
  "Mutation: Create an authenticated GitHub client."
  [params]
  (action [{:keys [state] :as env}]
    (swap! state assoc :github/client (ghub-client))))

(defn get-user
  [client callback]
  (-> client
      .-rest
      .-users
      (.getAuthenticated)
      js/Promise.resolve
      (.then callback)))

(defmutation fetch-user
  "Mutation: fetch the currently auth'd user's info"
  [params]
  (action [{:keys [state] :as env}]
    ;; TODO: this is super hacky, and doesn't do any error handling.
    ;; For the sake of this exercise, I'm going to leave it as is, but
    ;; a full prod app would need to handle this kind of thing in a
    ;; more robust way.
    (js/console.log env)
    (if-let [client (:github/client @state)]
      (try
        ;; TODO: move the callback code into its own function
        (get-user client #(let [user-data (reduce-kv (fn [m k v]
                                                       (assoc m (keyword "user" k) v))
                                                     {}
                                                     (:data (js->clj % :keywordize-keys true)))]
                            (swap! state assoc :github/user user-data)
                            (js/console.log "Fetched user" user-data)))
        (catch js/Object o
          (js/console.log "failed to fetch user with error" o)))
      (js/console.log "No authenticated github client; user fetching failed."))))

(defsc User [this {:user/keys [name id] :as props}]
  {:query [:user/name :user/id]
   :ident (fn [] [:user/id (:user/id props)])}
  (dom/div
   (dom/h3 "User info")
   (if (nil? name)
     (dom/div "No user authenticated")
     (dom/div (goog.string.format "Welcome %s" name)))))

(def ui-user (comp/factory User))

(defsc Root [this {:github/keys [client user]}]
  {:query [:github/client {:github/user (comp/get-query User)}]}
  (dom/div
   (dom/div "Root div")
   (ui-user user)))
