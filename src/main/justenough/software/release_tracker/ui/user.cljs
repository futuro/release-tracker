(ns justenough.software.release-tracker.ui.user
  (:require [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [com.fulcrologic.fulcro.algorithms.merge :as merge]
            [taoensso.timbre :as log]))

(defn get-user
  [client callback]
  (-> client
      .-rest
      .-users
      (.getAuthenticated)
      js/Promise.resolve
      (.then callback)))

(declare User)

(defmutation fetch-user
  "Mutation: fetch the currently auth'd user's info"
  [params]
  ;; TODO: long-term, remote network calls should be done in a
  ;; configured remote. For the sake of this interview app, I'm going
  ;; to keep it here for simplicity's sake.
  (action [{:keys [app state] :as env}]
    ;; TODO: this is super hacky, and doesn't do any error handling.
    ;; For the sake of this exercise, I'm going to leave it as is, but
    ;; a full prod app would need to handle this kind of thing in a
    ;; more robust way.
    (if-let [client (:github/client @state)]
      (try
        ;; TODO: move the callback code into its own function
        (get-user client #(let [user-data (reduce-kv (fn [m k v]
                                                       (assoc m (keyword "user" k) v))
                                                     {}
                                                     (:data (js->clj % :keywordize-keys true)))]
                            (merge/merge-component! app User user-data
                                   :replace [:github/user])
                            (log/info "Fetched user")))
        (catch js/Object o
          (log/error "failed to fetch user with error" o)))
      (log/info "No authenticated github client; user fetching failed."))))

(defsc User [this {:user/keys [name id] :as props}]
  {:query [:user/name :user/id]
   :ident (fn [] [:user/id (:user/id props)])
   :initial-state (fn [_] {})}
  (dom/div
   (dom/h3 "User info")
   (if (nil? name)
     (dom/div "No user authenticated")
     (dom/div (goog.string.format "Welcome %s" name)))))

(def factory (comp/factory User))
