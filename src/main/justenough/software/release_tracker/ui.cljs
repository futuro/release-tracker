(ns justenough.software.release-tracker.ui
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [justenough.software.release-tracker.ui.user :as user]
            [justenough.software.release-tracker.ui.repo.search :as search]
            [justenough.software.release-tracker.ui.repo.tracked :as tracked]))

(defsc Root [this {:github/keys [client user]
                   search :component/id
                   :as props}]
  {:query [:github/client
           {:github/user (comp/get-query user/User)}
           {:component/id (conj
                           (comp/get-query search/SearchForm)
                           (comp/get-query tracked/TrackedRepoList))}]}
  (dom/div :.ui.container
    (user/factory user)
    (search/search-ui search)
    (tracked/repo-list search)))
