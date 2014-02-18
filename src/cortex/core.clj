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
            GenericBooleanPrefUserBasedRecommender
            CachingRecommender]))

;;
;; Data model builders.
;;

(defn file-data-model
  [location]
  (FileDataModel. (io/file location)))


;;
;; Neighborhood builders.
;;

(defn- default-neighborhood
  [nb-users similarity model]
  (NearestNUserNeighborhood. nb-users similarity model))


;;
;; Similarity builders
;;

(defn- likelihood-similarity
  [model]
  (LogLikelihoodSimilarity. model))

(defn- rating-similarity
  [model]
  (PearsonCorrelationSimilarity. model))


;;
;; Recommender builders
;;

(defn- likelihood-recommender
  [model neighborhood similarity]
  (GenericBooleanPrefUserBasedRecommender. model neighborhood similarity))

(defn- rating-recommender
  [model neighborhood similarity]
  (GenericUserBasedRecommender. model neighborhood similarity))


;;
;; Recommender creation
;;

(defn- create-recommender
  "Create a recommender based on a data-model and a similarity function."
  [data-model similarity-builder neighborhood-builder recommender-builder
   {:keys [neighborhood-size cache] 
    :or {neighborhood-size 10
         cache false}}]
  (let [similarity (similarity-builder data-model)
        neighborhood (neighborhood-builder neighborhood-size similarity data-model)
        recommender (recommender-builder data-model neighborhood similarity)]
    (if cache
      (CachingRecommender. recommender)
      recommender)))

(defmulti user-based-recommender 
  "User based recommender definition based on type which can be :like or :rate" 
  :type)

(defmethod user-based-recommender :like [args]
  (create-recommender 
   (file-data-model (:data-specs args))
   likelihood-similarity
   default-neighborhood
   likelihood-recommender
   (:options args)))

(defmethod user-based-recommender :rate [args]
  (create-recommender 
   (file-data-model (:data-specs args))
   rating-similarity
   default-neighborhood
   rating-recommender
   (:options args)))


;;
;; Recommendations generation
;;

(defn recommend
  [recommender user nb-results]
  (map (fn [result] 
         {:item (.getItemID result) 
          :value (.getValue result)})
       (.recommend recommender user nb-results)))
