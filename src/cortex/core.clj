(ns cortex.core
  (:require [clojure.java.io :as io])
  (:import [org.apache.mahout.cf.taste.model DataModel]
           [org.apache.mahout.cf.taste.impl.model.file FileDataModel]
           [org.apache.mahout.cf.taste.neighborhood UserNeighborhood]
           [org.apache.mahout.cf.taste.impl.neighborhood NearestNUserNeighborhood]
           [org.apache.mahout.cf.taste.recommender Recommender RecommendedItem]
           [org.apache.mahout.cf.taste.impl.recommender
            GenericUserBasedRecommender
            CachingRecommender]
           [org.apache.mahout.cf.taste.similarity UserSimilarity]
           [org.apache.mahout.cf.taste.impl.similarity
            AveragingPreferenceInferrer
            PearsonCorrelationSimilarity]))

(defn file-data-model
  [location]
  (FileDataModel. (io/file location)))

(defn user-similarity
  [model]
  (PearsonCorrelationSimilarity. model))

(defn user-neighborhood
  [nb-users similarity model]
  (NearestNUserNeighborhood. nb-users similarity model))

(defn user-based-recommender
  [data-location & {:keys [neighborhood-size cache]
                    :or {:neighborhood-size 10
                         :cache false}}]
  (let [model (file-data-model data-location)
        similarity (user-similarity model)
        neighborhood (user-neighborhood neighborhood-size similarity model)
        recommender (GenericUserBasedRecommender. model neighborhood similarity)]
    (if cache
      (CachingRecommender. recommender)
      recommender)))

(defn recommend
  [recommender user nb-results]
  (.recommend recommender user nb-results))
