(ns justenough.software.release-tracker.server.repo
  (:require [justenough.software.release-tracker.server.database :as db]
            [justenough.software.release-tracker.server.github :as ghub]
            [asami.core :as d]
            [clojure.string :as str]
            [taoensso.timbre :as log]))

(defn all-repos
  []
  (let [db (d/db db/connection)]
    (try
      ;; TODO: probably this should have some different formatting.
      (str/join " "
             (d/q '[:find [?repo-name ...]
                    :where [_ :github.repo/name ?repo-name]]
                  db))
      (catch NullPointerException npe
        ;; N.B. if you attempt to query against an attribute that
        ;; doesn't exist in the DB Asami will throw an NPE. There are
        ;; probably other reasons this could happen, but to keep
        ;; within a reasonable timeframe for this exercise I'm simply
        ;; going to assume all NPEs associated with this query are
        ;; caused in this way.
        ;;
        ;; I could also check the DB contents, or the connection
        ;; `next-tx`, and make some assumptions, but I'm not going to
        ;; because that's not a great solution for a production
        ;; DB/this should be reported to the Asami maintainers to see
        ;; if it's a bug or not/this is meant to be a small project.
        "No repos currently tracked.\n"))))

(defn info
  [{:keys [user repo]}]
  (format "You asked for details on repo %s/%s\n"
          user repo))

(defn track
  [{:keys [user repo] :as opts}]
  (try
    (let [repo-data (ghub/fetch-repo opts)
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
