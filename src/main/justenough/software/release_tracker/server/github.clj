(ns justenough.software.release-tracker.server.github
  (:require [justenough.software.release-tracker.config :as cfg]
            [clj-http.client :as http]
            [com.wsscode.pathom.connect.graphql2 :as pg2]
            [com.wsscode.pathom.graphql :as pg]
            [mount.core :refer [args defstate]]
            [clojure.string :as str]
            [jsonista.core :as json]))

(def graphql-uri "https://api.github.com/graphql")

(defstate github-secret
  :start (get-in cfg/config [:secrets :github/auth :user/token]))

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
