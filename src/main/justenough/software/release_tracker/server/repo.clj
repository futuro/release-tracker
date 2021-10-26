(ns justenough.software.release-tracker.server.repo
  (:require [justenough.software.release-tracker.server.database :as db]
            [justenough.software.release-tracker.server.github :as ghub]
            [asami.core :as d]))

(defn all-repos
  []
  (let [db (d/db db/connection)]
    (try
      (d/q '[:find ?repo-name
             :where [_ :repo/name ?repo-name]]
           db)
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
  [user repo]
  (format "You asked for details on repo %s/%s\n"
          user repo))

(defn track
  [user repo]
  ;; TODO  sort out the rest of what this requires to work
  (d/transact db/connection
              {:tx-data [(ghub/fetch-repo {:user user :repo repo})]}))

(defn seen
  [user repo]
  (format "You asked me to mark the repo %s/%s as seen\n"
          user repo))
