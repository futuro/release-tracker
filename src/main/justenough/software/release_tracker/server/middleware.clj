(ns justenough.software.release-tracker.server.middleware
  (:require [compojure.core :refer [context defroutes GET POST PUT]]
            [compojure.route :as route]
            [com.fulcrologic.fulcro.server.api-middleware :as fsa]
            [com.wsscode.pathom.connect :as pc]
            [com.wsscode.pathom.core :as p]
            [com.wsscode.pathom.graphql :as pg]
            [com.wsscode.pathom.connect.graphql2 :as pg2]
            [mount.core :refer [defstate]]
            [justenough.software.release-tracker.config :as cfg]
            [justenough.software.release-tracker.server.repo :as repo]
            [ring.middleware.defaults :refer [wrap-defaults]]
            [ring.util.response :refer [response file-response resource-response]]
            [ring.util.response :as resp]))

(defroutes routes
  (GET "/repo" []
    (resp/response
     (repo/all-repos)))

  (context "/repo/:user/:repo" [user repo]
    (GET "/" []
      (resp/response
       (repo/info {:user user :repo repo})))
    (POST "/track" []
      (resp/response
       (repo/track {:user user :repo repo})))
    (POST "/update" []
      (let [res (repo/add-repo-releases {:user user :repo repo})]
        (if (string? res)
          (resp/bad-request res)
          (resp/response "Successfully updated repo releases.\n"))))
    (POST "/seen" []
      (resp/response
       (repo/seen {:user user :repo repo}))))

  (route/not-found "Nothing to see here."))

(defstate middleware
  :start
  (let [default-config (:ring.middleware/defaults-config cfg/config)]
    (-> routes
        (wrap-defaults default-config))))
