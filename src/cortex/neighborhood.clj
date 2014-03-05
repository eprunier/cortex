(ns cortex.neighborhood
  (:import [org.apache.mahout.cf.taste.impl.neighborhood
            NearestNUserNeighborhood
            ThresholdUserNeighborhood]))

(defn nearest-n-users
  "Computes a neighborhood consisting of the nearest n users to a given user.
'Nearest' is defined by the given UserSimilarity."
  [similarity model & [{:keys [users]}]]
  (NearestNUserNeighborhood. users similarity model))

(defn threshold
  "Computes a neigbhorhood consisting of all users whose similarity to the
given user meets or exceeds a certain threshold. Similarity is defined by 
the given similarity function."
  [similarity model & {:keys [threshold]}]
  (ThresholdUserNeighborhood. threshold similarity model))
