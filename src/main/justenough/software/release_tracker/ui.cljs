(ns justenough.software.release-tracker.ui
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [justenough.software.release-tracker.ui.user :as user]))

(defsc Root [this {:github/keys [client user]}]
  {:query [:github/client
           {:github/user (comp/get-query user/User)}]}
  (dom/div
   (dom/div "Root div")
   (user/factory user)))
