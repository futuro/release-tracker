(ns justenough.software.release-tracker.server.repo
  (:require [justenough.software.release-tracker.server.database :as db]
            [justenough.software.release-tracker.server.github :as ghub]
            [datascript.core :as d]
            [clojure.string :as str]
            [taoensso.timbre :as log]))

(defn drop-nil-vals
  "Drops all k/v pairs from `m` where the value is `nil`."
  [m]
  (into {}
        (remove #(nil? (second %)))
        m))

(defn full-name
  [{:keys [user repo]}]
  (format "%s/%s" user repo))

(defn all-repos
  []
  (->> @db/connection
       (d/q '[:find [?repo-name ...]
              :where [_ :github.repo/full_name ?repo-name]])
       (str/join " ")))

(defn repo-exists?
  [{:keys [user repo]}]
  (let [full-name (format "%s/%s" user repo)]
    (d/q '[:find ?eid .
           :in $ ?full-name
           :where [?eid :github.repo/full_name ?full-name]]
         @db/connection full-name)))

(defn info
  [{:keys [user repo] :as opts}]
  (try
    (let [db (d/db db/connection)
          repo-id (d/q '[:find ?eid .
                         :in $ ?user ?repo
                         :where [?eid :github.repo/name ?repo]
                         [?eid :github.repo/owner ?oid]
                         [?oid :login ?user]]
                       db user repo)]
      (d/entity db repo-id))
    (catch Throwable t
      (log/warn "Trying to get info for " opts " threw the following error" t)
      (format "No info available for repo %s/%s" user repo))))

(defn track
  [{:keys [user repo] :as opts}]
  (if (repo-exists? opts)
    "Repo already being tracked.\n"
    (try
      (let [tx-result (->> opts
                           ghub/fetch-repo
                           drop-nil-vals
                           vector
                           (d/transact! db/connection))]
        "Repo tracked.\n")
      (catch Exception e
        (log/warnf "Caught exception while trying to track repo %s/%s, %s"
                   user repo e)
        (format "Something went wrong while trying to track repo \"%s/%s\", double check this repo exists, and check the server logs for details.\n"
                user repo)))))

(defn seen
  [{:keys [user repo]}]
  (format "You asked me to mark the repo %s/%s as seen\n"
          user repo))

(comment
  (def react-repo
    (let [user "facebook"
          repo "react"]
      (->> {:user user :repo repo}
           ghub/fetch-repo
           (into {} (remove #(nil? (second %)))))))

  (d/transact! db/connection [react-repo])
  )
