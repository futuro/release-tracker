(ns justenough.software.release-tracker.ui
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [justenough.software.release-tracker.ui.user :as user]
            [justenough.software.release-tracker.ui.repo.search :as search]))

(defsc Root [this {:github/keys [client user]
                   search :component/id
                   :as props}]
  {:query [:github/client
           {:github/user (comp/get-query user/User)}
           {:component/id (comp/get-query search/SearchForm)}]}
  (dom/div :.ui.container
   (user/factory user)
   (search/search-ui search)))
