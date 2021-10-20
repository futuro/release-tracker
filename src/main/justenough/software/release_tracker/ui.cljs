(ns justenough.software.release-tracker.ui
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [justenough.software.release-tracker.ui.user :as user]
            [justenough.software.release-tracker.ui.repo :as repo]))

(defsc Root [this {:github/keys [client user]
                   search :component/id
                   :as props}]
  {:query [:github/client
           {:github/user (comp/get-query user/User)}
           {:component/id (comp/get-query repo/SearchForm)}]}
  (dom/div
   (dom/div "Root div")
   (user/factory user)
   (repo/search-ui search)))
