(ns justenough.software.release-tracker.ui.repo.search
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.dom.events :as evt]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [com.fulcrologic.fulcro.algorithms.merge :as merge]
            [justenough.software.release-tracker.ui.repo.tracked :as tracked]
            [taoensso.timbre :as log]))

;; Utils

(defn prepend-keys
  [m prefix]
  (reduce-kv (fn [m k v]
               (assoc m (keyword prefix k) v))
             {}
             m))

;; Search Results

(defsc SearchResult [this {:repo/keys [full_name id]
                           :as props}]
  {:query [:repo/full_name :repo/id]
   :ident (fn [] [:repo/id (:repo/id props)])}
  (dom/div :.ui.card
    (dom/div :.content
      (dom/div :.header full_name))
    (dom/div :.extra.content
      (dom/button :.fluid.ui.positive.basic.button
                  {:onClick #(comp/transact! this [(tracked/track-repo {:id id})])}
                  "Track"))))

(def search-result (comp/factory SearchResult {:keyfn :repo/id}))

(defsc SearchResultsList [this {:repo/keys [list] :as props}]
  {:query [{:repo/list (comp/get-query SearchResult)}]
   :initial-state (fn [_]
                    {:repo/list []})
   :ident (fn [] [:component/id :repo.search/results])}
  (if (not-empty list)
    (dom/div :.ui.cards
      (map #(search-result %) (take 4 list)))
    (dom/div "No results yet")))

(def search-result-list (comp/factory SearchResultsList))

;; Search Form

(defn parse-search-results
  "Given the results of a search using the Octokit library, this returns
  the results in the same shape as the query for `SearchResultsList`,
  thus enabling auto-normalization by Fulcro."
  [results]
  (let [repos (-> results
                 (js->clj :keywordize-keys true)
                 :data
                 :items)]
    {:repo/list
     (->> repos
          ;; This must return a vector to get free normalization from
          ;; `merge/merge-component`
          (mapv #(prepend-keys % "repo")))}))

(defmutation search-repos! [{:repo/keys [name]}]
  (action [{:keys [state app]}]
    (let [client (:github/client @state)]
      (try
        (-> client
            .-rest
            .-search
            (.repos #js {:q name})
            js/Promise.resolve
            ;; TODO this will grow the list unbounded, and we should
            ;; probably figure out how to clear about the list of
            ;; search results with every new search.
            (.then #(merge/merge-component! app SearchResultsList (parse-search-results %))))
        (catch js/Object o
          (log/error "Search failed with error" o))))))

(def repo-search-ident
  [:component/id :repo.search/form])

;; TODO turn this into a parent Search component, which contains SearchForm and SearchResultsList
(defsc SearchForm [this {{:repo/keys [name]} :repo.search/form
                         list       :repo.search/results
                         :as        props}]
  {:query             [{:repo.search/form [:repo/name]}
                       {:repo.search/results (comp/get-query SearchResultsList)}]
   :ident             (fn [] repo-search-ident)
   :initial-state (fn [_]
                    {:repo/name ""
                     :repo.search/results (comp/get-initial-state SearchResultsList {})})}
  (let [submit!  (fn [evt]
                   (when (or (identical? true evt) (evt/enter-key? evt))
                     (comp/transact! this [(search-repos! {:repo/name name})])))]
    (dom/div :.ui.segment
      (dom/div :.ui.center.aligned.header "Search for repos")
      (dom/div :.ui.grid
        (dom/div :.ui.row
          (dom/div :.ui.center.aligned.column
            (dom/div :.ui.input
              (dom/input {:placeholder "Repo name"
                          :value        (or name "")
                          :autoComplete "off"
                          :onKeyDown    submit!
                          :onChange     #(m/set-string! this :repo/name :event %)}))))
        (dom/div :.ui.row
          (dom/div :.ui.column
            (search-result-list list)))))))

(def search-ui (comp/factory SearchForm))
