(ns justenough.software.release-tracker.util)

(defn namespace-keys
  "Rewrite all of the top-level keys in the map `m` to have the
  namespace `ns`, regardless of whatever previous namespace they had."
  [m ns]
  (reduce-kv (fn [acc k v]
               (assoc acc (keyword ns (name k)) v))
             {}
             m))
