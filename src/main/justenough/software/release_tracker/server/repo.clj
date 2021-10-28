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
  [opts]
  (d/q '[:find ?eid .
         :in $ ?full-name
         :where [?eid :github.repo/full_name ?full-name]]
       @db/connection (full-name opts)))

(defn info
  [opts]
  (try
    (d/q '[:find (pull ?eid [:github.repo/full_name
                             :github.repo/description
                             {:github.repo/latest-seen-release
                              [:github.release/published_at]}]) .
           :in $ ?full-name
           :where [?eid :github.repo/full_name ?full-name]]
         @db/connection (full-name opts))
    (catch Throwable t
      (log/warn "Trying to get info for " opts " threw the following error" t)
      (format "No info available for repo %s" (full-name opts)))))

(defn assoc-backtracking-key
  [opts m]
  (assoc m :github.repo/_releases [:github.repo/full_name (full-name opts)]))

(defn add-repo-releases
  [opts]
  (if-not (repo-exists? opts)
    "Track the repo before adding releases.\n"
    (try
      (let [tx-result (->> opts
                           ghub/fetch-releases
                           (map drop-nil-vals)
                           (map #(assoc-backtracking-key opts %))
                           (d/transact! db/connection))]
        tx-result)
      (catch Throwable t
        (log/error "Error while adding repo releases" t)
        (format "Something went wrong while trying to add releases for repo %s"
                (full-name opts))))))

(defn track
  [{:keys [user repo] :as opts}]
  (if (repo-exists? opts)
    "Repo already being tracked.\n"
    (try
      (let [tx-result (->> opts
                           ghub/fetch-repo
                           drop-nil-vals
                           vector
                           (d/transact! db/connection))

            release-tx-result (add-repo-releases opts)]
        "Repo tracked.\n")
      (catch Exception e
        (log/warnf "Caught exception while trying to track repo %s/%s, %s"
                   user repo e)
        (format "Something went wrong while trying to track repo \"%s/%s\", double check this repo exists, and check the server logs for details.\n"
                user repo)))))

(defn latest-release
  [opts]
  (let [rid (d/q '[:find (max ?id) .
                   :in $ ?full-name
                   :where
                   [?repo :github.repo/full_name ?full-name]
                   [?repo :github.repo/releases ?release]
                   [?release :github.release/id ?id]]
                 @db/connection (full-name opts))]
    (d/pull @db/connection '[*] [:github.release/id rid])))

(defn seen
  [opts]
  (d/transact! db/connection
               [{:github.repo/full_name (full-name opts)
                 :github.repo/latest-seen-release (latest-release opts)}])
  (format "The latest release for %s has been marked as seen" (full-name opts)))

(comment
  (def opts
    {:user "facebook"
     :repo "react"})

  (def react-repo
    (->> opts
         ghub/fetch-repo
         drop-nil-vals))

  (d/transact! db/connection [react-repo])

  (d/datoms @db/connection :eavt)

  (def releases
    (add-repo-releases opts))

  (-> releases
      :db-after
      (d/pull [:github.repo/releases] [:github.repo/full_name (full-name opts)]))

  (d/q '[:find ?name ?id
         :where [ ?id]
         [?e1 :github.release/id ?id]
         [?e1 :github.release/name ?name]]
       @db/connection)

  (def ttt
    (d/with @db/connection releases))

  (d/pull
   (d/db-with @db/connection
              [{:github.repo/full_name (full-name opts)
                :github.repo/latest-release (latest-release opts)}])
   '[:github.repo/latest-release :github.repo/full_name] [:github.repo/full_name (full-name opts)])

  (d/datoms
   (d/db-with @db/connection
              [{:github.repo/full_name (full-name opts)
                :github.repo/latest-release (latest-release opts)}])
   :eavt)

  (d/pull 
   (:db-after
    (d/with @db/connection
            (map #(assoc % :github.repo/_releases [:github.repo/full_name "facebook/react"])
                 releases)))
   '[:github.repo/releases] [:github.repo/full_name "facebook/react"])

  (d/pull @db/connection '[*] 2))
