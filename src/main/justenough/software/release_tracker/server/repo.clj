(ns justenough.software.release-tracker.server.repo
  (:require [justenough.software.release-tracker.server.database :as db]
            [asami.core :as d]))

(defn all-repos
  []
  (let [db (d/db db/connection)]
    (try
      (d/q '[:find ?repo-name
             :where [_ :repo/name ?repo-name]]
           db)
      (catch NullPointerException npe
        "No repos currently tracked.\n"))))

(defn info
  [user repo]
  (format "You asked for details on repo %s/%s\n"
          user repo))

(defn track
  [user repo]
  (format "You asked me to track repo %s/%s, but I haven't yet\n"
          user repo))

(defn seen
  [user repo]
  (format "You asked me to mark the repo %s/%s as seen\n"
          user repo))
