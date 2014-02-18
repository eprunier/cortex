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

(defn- likelihood-similarity
  [model]
  (LogLikelihoodSimilarity. model))

(defn- rating-similarity
  [model]
  (PearsonCorrelationSimilarity. model))


;;
;; Recommender creation
;;

(defn- user-based-recommender-impl
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

(defmulti user-based-recommender :type)
(defmethod user-based-recommender :like [args]
  (user-based-recommender-impl (:data args) likelihood-similarity))
(defmethod user-based-recommender :rate [args]
  (user-based-recommender-impl (:data args) rating-similarity))


;;
;; Recommendations generation
;;

(defn recommend
  [recommender user nb-results]
  (map (fn [result] 
         [(.getItemID result) (.getValue result)])
       (.recommend recommender user nb-results)))
