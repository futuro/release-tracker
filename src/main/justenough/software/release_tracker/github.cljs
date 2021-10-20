(ns justenough.software.release-tracker.github
  (:require [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
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

