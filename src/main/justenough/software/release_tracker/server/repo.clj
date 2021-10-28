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
                         :where [[?eid :github.repo/name ?repo]
                                 [?eid :github.repo/owner ?user]]]
                       db user repo)]
      ( repo-id)
      (d/entity db repo-id))
    (catch Throwable t
      (log/warn "Trying to get info for " opts " threw the following error" t)
      (format "No info available for repo %s/%s" user repo))))

(defn track
  [{:keys [user repo] :as opts}]
  (try
    (let [repo-data (ghub/fetch-repo opts)
          ;; TODO introduce either :db/ident or a plain :id key to
          ;; allow for upserts
          ;;
          ;; TODO update the keys to have the replacement annotation
          ;; -- a single quote mark at the end -- to enable
          ;; replacement of existing data, which, while brittle,
          ;; should be sufficient for this exercise. Alternatively,
          ;; query the DB for the repo before fetching and tracking it
          tx-result (d/transact db/connection
                                {:tx-data [repo-data]})]
      "Repo tracked.\n")
    (catch Exception e
      (log/warnf "Caught exception while trying to track repo %s/%s, %s"
                 user repo e)
      (format "Something went wrong while trying to track repo \"%s/%s\", double check this repo exists, and check the server logs.\n"
              user repo))))

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
