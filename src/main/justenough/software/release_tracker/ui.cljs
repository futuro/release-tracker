(ns justenough.software.release-tracker.ui
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [justenough.software.release-tracker.ui.user :as user]
            [justenough.software.release-tracker.ui.repo.search :as search]
            [justenough.software.release-tracker.ui.repo.tracked :as tracked]))

(defsc Root [this {:github/keys [client user]
                   search :component/id
                   tracked-repos :tracked-repos
                   :as props}]
  {:query [:github/client
           {:github/user (comp/get-query user/User)}
           {:component/id (comp/get-query search/SearchForm)}
           {:tracked-repos (comp/get-query tracked/TrackedRepoList)}]
   :initial-state (fn [params]
                    {:tracked-repos (comp/get-initial-state tracked/TrackedRepoList
                                                            {:list/id ::tracked/repo-list
                                                             :list/label "Tracked Repos"})
                     :github/client nil
                     :github/user {}
                     :component/id (comp/get-initial-state search/SearchForm {})})}
  (dom/div :.ui.container
    (user/factory user)
    (search/search-ui search)
    (tracked/repo-list tracked-repos)))
