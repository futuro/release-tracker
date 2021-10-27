(ns justenough.software.release-tracker.server.github
  (:require [justenough.software.release-tracker.config :as cfg]
            [justenough.software.release-tracker.util :as util]
            [clj-http.client :as http]
            [com.wsscode.pathom.connect.graphql2 :as pg2]
            [com.wsscode.pathom.graphql :as pg]
            [mount.core :refer [args defstate]]
            [clojure.string :as str]
            [jsonista.core :as json]))

(defstate github-secret
  :start (get-in cfg/config [:secrets :github/auth :user/token]))

;;; REST API

(def base-rest-uri "https://api.github.com")

(def base-req-options
  {:oauth-token github-secret
   :accept :application/vnd.github.v3+json})

(defn fetch-repo
  [{:keys [user repo] :as cfg}]
  (let [rest-uri (format "%s/repos/%s/%s" base-rest-uri user repo)
        response (http/get rest-uri base-req-options)]
    ;; TODO: handle http failures, like asking for a non-existent repo
    (-> response
        :body
        (json/read-value json/keyword-keys-object-mapper)
        (util/namespace-keys "github.repo"))))

;; TODO I'm seeing a lot of patterns for abstraction here
(defn fetch-releases
  "Fetch the releases associated with the given :user and :repo. Doesn't
  handle exceptions from the HTTP layer; this is left up to the
  calling functions to do whatever is reasonable."
  [{:keys [user repo] :as cfg}]
  (let [rest-uri (format "%s/repos/%s/%s/releases" base-rest-uri user repo)
        {:keys [body]} (http/get rest-uri base-req-options)]
    (map #(util/namespace-keys % "github.release")
         (json/read-value body json/keyword-keys-object-mapper))))

(comment
  (fetch-releases {:user "facebook" :repo "react"})

  (fetch-releases {:user "facebook" :repo "dflksjdflk"})
  )

;;; GraphQL Experiments

(def graphql-uri "https://api.github.com/graphql")

(defn eql->jsonquery
  [eql]
  (format "{\"query\":%s}"
          (json/write-value-as-string
           (str/replace (pg/query->graphql eql)
                        #"\n(\s+)?" " "))))

(defn query-graphql
  [eql]
  (http/post graphql-uri
             {:oauth-token github-secret
              :body (eql->jsonquery eql)
              :content-type :json
              :accept :json}))
