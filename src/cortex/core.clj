(ns cortex.core
  (:require [clojure.java.io :as io])
  (:import [org.apache.mahout.cf.taste.impl.model.file FileDataModel]
           [org.apache.mahout.cf.taste.impl.similarity
            PearsonCorrelationSimilarity
            LogLikelihoodSimilarity]
           [org.apache.mahout.cf.taste.impl.neighborhood
            NearestNUserNeighborhood]
           [org.apache.mahout.cf.taste.impl.recommender 
            GenericUserBasedRecommender
            CachingRecommender]))

(defn- file-data-model
  [location]
  (FileDataModel. (io/file location)))

(defn- user-neighborhood
  [nb-users similarity model]
  (NearestNUserNeighborhood. nb-users similarity model))

;;
;; Similarity functions
;;

(defn likelihood-similarity
  [model]
  (LogLikelihoodSimilarity. model))

(defn rating-similarity
  [model]
  (PearsonCorrelationSimilarity. model))


;;
;; Recommender creation
;;

(defn user-based-recommender
  [data-location similarity-fn & {:keys [neighborhood-size cache]
                    :or {neighborhood-size 10
                         cache false}}]
  (let [model (file-data-model data-location)
        similarity (similarity-fn model)
        neighborhood (user-neighborhood neighborhood-size similarity model)
        recommender (GenericUserBasedRecommender. model neighborhood similarity)]
    (if cache
      (CachingRecommender. recommender)
      recommender)))

;;
;; Recommendations generation
;;

(defn recommend
  [recommender user nb-results]
  (.recommend recommender user nb-results))
