(ns cortex.neighborhood
  (:import [org.apache.mahout.cf.taste.impl.neighborhood
            NearestNUserNeighborhood]))

(defn default-neighborhood
  [nb-users similarity model]
  (NearestNUserNeighborhood. nb-users similarity model))
