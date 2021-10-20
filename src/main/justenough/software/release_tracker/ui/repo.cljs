(ns justenough.software.release-tracker.ui.repo
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.dom.events :as evt]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [com.fulcrologic.fulcro.algorithms.merge :as merge]
            [com.fulcrologic.fulcro.algorithms.form-state :as fs]))

(defn field [{:keys [label valid? error-message] :as props}]
  (let [input-props (-> props
                        (assoc :name label)
                        (dissoc :label :valid? :error-message))]
    (dom/div :.ui.field
             (dom/label {:htmlFor label} label)
             (dom/input input-props)
             (dom/div :.ui.error.message {:classes [(when valid? "hidden")]}
                      error-message))))

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
  (dom/li
   (dom/h5 full_name)))

(def search-result (comp/factory SearchResult {:keyfn :repo/id}))

(defsc SearchResultsList [this {:repo/keys [list] :as props}]
  {:query [{:repo/list (comp/get-query SearchResult)}]
   :initial-state (fn [_]
                    {:repo/list []})
   :ident (fn [] [:component/id :repo.search/results])}
  (dom/div
   (dom/h4 "Search Results")
   (if (not-empty list)
     (dom/ul
      (map #(search-result %) list))
     (dom/div "No results yet"))))

(def search-result-list (comp/factory SearchResultsList))

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
  (action [{:keys [state]}]
    (let [client (:github/client @state)]
      (try
        (-> client
            .-rest
            .-search
            (.repos #js {:q name})
            js/Promise.resolve
            (.then #(swap! state merge/merge-component SearchResultsList (parse-search-results %))))
        (catch js/Object o
          (js/console.log "Search failed with error" o))))))

(declare SearchForm)

(def repo-search-ident
  [:component/id :repo.search/form])

(defn configure-search-form*
  [state-map]
  (-> state-map
      (assoc-in repo-search-ident
                {:repo/name ""})
      (fs/add-form-config* SearchForm repo-search-ident)))

(defmutation configure-search-form [_]
  (action [{:keys [state]}]
          (swap! state configure-search-form*)))

(defsc SearchForm [this {{:repo/keys [name]} :repo.search/form
                         list       :repo.search/results
                         :as        props}]
  {:query             [{:repo.search/form [:repo/name]}
                       fs/form-config-join
                       {:repo.search/results (comp/get-query SearchResultsList)}]
   :form-fields       #{:repo/name}
   :ident             (fn [] repo-search-ident)
   :componentDidMount (fn [this]
                        (comp/transact! this [(configure-search-form)]))}
  (let [submit!  (fn [evt]
                   (when (or (identical? true evt) (evt/enter-key? evt))
                     (comp/transact! this [(search-repos! {:repo/name name})])))
        checked? (fs/checked? props)]
    (dom/div
     (dom/h3 "Search")
     (dom/div :.ui.form {:classes [(when checked? "error")]}
              (dom/input {:label        "Repo name"
                          :value        (or name "")
                          :autoComplete "off"
                          :onKeyDown    submit!
                          :onChange     #(m/set-string! this :repo/name :event %)}))
     (search-result-list list))))

(def search-ui (comp/factory SearchForm))
