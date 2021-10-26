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
            [ring.middleware.defaults :refer [wrap-defaults]]
            [ring.util.response :refer [response file-response resource-response]]
            [ring.util.response :as resp]))

(defroutes routes
  ;; N.B. this route is intended, though not functional, to handle
  ;; Fulcro requests from the frontend
  (PUT "/experiment/api" {:keys [transit-params]}
    (fsa/handle-api-request transit-params ))

  ;; TODO
  (GET "/repo" []
    (resp/response "All tracked repos\n"))

  (context "/repo/:user/:repo" [user repo]
    (GET "/" []
      (resp/response
       (format "You asked for details on repo %s/%s\n"
               user repo)))
    (POST "/track" []
      (resp/response
       (format "You asked me to track repo %s/%s, but I haven't yet\n"
               user repo)))
    (POST "/seen" []
      (resp/response
       (format "You asked me to mark the repo %s/%s as seen\n"
               user repo))))

  (route/not-found "Nothing to see here."))

(defstate middleware
  :start
  (let [default-config (:ring.middleware/defaults-config cfg/config)]
    (-> routes
        (wrap-defaults default-config))))
