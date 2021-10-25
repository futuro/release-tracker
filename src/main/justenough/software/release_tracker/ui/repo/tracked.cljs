(ns justenough.software.release-tracker.ui.repo.tracked
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [com.fulcrologic.fulcro.algorithms.merge :as merge]
            [taoensso.timbre :as log]))

(declare TrackedRepo)

(defmutation track-repo
  [{:keys [ident]}]
  (action [{:keys [state]}]
    (let [repo (get-in @state ident)]
      (swap! state merge/merge-component TrackedRepo
             repo
             :append [:list/id ::repo-list :list/repos]))))

;; TODO: add a remote that can query GitHub rest API directly, and
;; then we can use that to automatically fetch releases and tags and
;; things from the URLs in the repo object map
(defsc TrackedRepo [this {:repo/keys [full_name id]
                          :as props}]
  {:query [:repo/full_name :repo/id]
   :ident :repo/id
   :initial-state (fn [_] {})}
  (dom/div :.ui.card
    (dom/div :.content
      (dom/h2 :.header full_name))))

(def tracked-repo (comp/factory TrackedRepo {:keyfn :repo/id}))

(defsc TrackedRepoList [this {repos :list/repos
                              :as props}]
  {:query [:list/id :list/label {:list/repos (comp/get-query TrackedRepo)}]
   :initial-state (fn [{:list/keys [id label]}]
                    {:list/id id
                     :list/label label
                     :list/repos []})
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
