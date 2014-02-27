(ns cortex.recommender
  (:import [org.apache.mahout.cf.taste.impl.recommender 
            GenericUserBasedRecommender
            GenericBooleanPrefUserBasedRecommender]
           [org.apache.mahout.cf.taste.eval RecommenderBuilder]))


;;
;; Recommender creation
;;

(defn likes-recommender
  [similarity neighborhood data-model]
  (GenericBooleanPrefUserBasedRecommender. data-model neighborhood similarity))

(defn ratings-recommender
  [similarity neighborhood data-model]
  (GenericUserBasedRecommender. data-model neighborhood similarity))


;;
;; Recommender builder
;;

(defn recommender-builder
  [similarity-fn neighborhood-fn recommender-fn]
  (proxy [RecommenderBuilder] []
    (buildRecommender [data-model]
      (let [similarity (similarity-fn data-model)
            neighborhood (neighborhood-fn 10 similarity data-model)]
        (recommender-fn similarity neighborhood data-model)))))

(defn build-recommender
  [recommender-builder data-model]
  (.buildRecommender recommender-builder 
                     data-model))
