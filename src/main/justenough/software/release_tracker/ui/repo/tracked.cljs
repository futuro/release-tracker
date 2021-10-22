(ns justenough.software.release-tracker.ui.repo.tracked
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]))

(defsc TrackedRepo [this {:repo/keys [full_name id]
                          :as props}]
  {:query [:repo/full_name :repo/id]
   :ident :repo/id
   :initial-state (fn [_] [])}
  ;; TODO add the buttons to mark a repo as tracked
  (dom/div :.ui.card
    (dom/div :.content
      (dom/div :.header full_name))))

(def tracked-repo (comp/factory TrackedRepo {:keyfn :repo/id}))

(defsc TrackedRepoList [this {repos :list/repos
                              :as props}]
  {:query [:list/id :list/label {:list/repos (comp/get-query TrackedRepo)}]
   :initial-state (fn [{:list/keys [id label]}]
                    {:list/id id
                     :list/label label
                     :list/repos (comp/get-initial-state TrackedRepo {})})
   :ident (fn [] [:list/id (:list/id props)])}
  (dom/div :.ui.segment
    (dom/div :.ui.grid
      (dom/div :.ui.four.wide.column
        (dom/div :.ui.center.aligned.header
          "Tracked Repos"
          (dom/div :.ui.cards
            (map #(tracked-repo %) repos))))
      (dom/div :.ui.twelve.wide.column
        (dom/div
          (dom/div :.ui.center.aligned.header
            "Tracked Repo Details"))))))

(def repo-list (comp/factory TrackedRepoList))
