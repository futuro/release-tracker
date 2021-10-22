(ns justenough.software.release-tracker.ui.repo.tracked
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]))

(defsc TrackedRepo [this {:repo/keys [full_name id]
                          :as props}]
  {:query [:repo/full_name :repo/id]
   :ident :repo/id}
  ;; TODO add the buttons to mark a repo as tracked
  (dom/div :.ui.card
    (dom/div :.content
      (dom/div :.header full_name))))

(def tracked-repo (comp/factory TrackedRepo {:keyfn :repo/id}))

(defsc TrackedRepoList [this {repos ::tracked-repo-list
                              :as props}]
  {:query [{::tracked-repo-list (comp/get-query TrackedRepo)}]
   :initial-state (fn [_]
                    {::tracked-repo-list []})
   :ident (fn [] [:component/id ::tracked-repo-list])}
  ;; TODO: add a grid to support repo details view
  (dom/div :.ui.segment
    (dom/div :.ui.cards
      (map #(tracked-repo %) repos))))

(def repo-list (comp/factory TrackedRepoList))
