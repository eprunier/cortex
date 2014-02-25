(ns cortex.core
  (:import [org.apache.mahout.cf.taste.impl.recommender 
            GenericUserBasedRecommender
            GenericBooleanPrefUserBasedRecommender]
           [org.apache.mahout.cf.taste.eval RecommenderBuilder]
           [org.apache.mahout.cf.taste.impl.eval 
            GenericRecommenderIRStatsEvaluator
            AverageAbsoluteDifferenceRecommenderEvaluator])
  (:require [clojure.java.io :as io]
            [cortex.neighborhood :as cn]
            [cortex.similarity :as cs]))

;;
;; Recommender creation
;;

(defn- likelihood-recommender
  [data-model neighborhood similarity]
  (GenericBooleanPrefUserBasedRecommender. data-model neighborhood similarity))

(defn- rating-recommender
  [data-model neighborhood similarity]
  (GenericUserBasedRecommender. data-model neighborhood similarity))

(defn recommender
  [recommender-builder data-model]
  (.buildRecommender recommender-builder 
                     data-model))


;;
;; Recommender builder
;;

(defn- recommender-builder
  [neighborhood-fn similarity-fn recommender-fn]
  (proxy [RecommenderBuilder] []
    (buildRecommender [data-model]
      (let [similarity (similarity-fn data-model)
            neighborhood (neighborhood-fn 10 similarity data-model)]
        (recommender-fn data-model neighborhood similarity)))))

(defmulti user-based-recommender-builder 
  "User based recommender definition based on type which can be :like or :rate.
Exemple: (user-based-recommender {:type :like :data-specs \"/tmp/data.csv\"})" 
  keyword)

(defmethod user-based-recommender-builder :like [args]
  (recommender-builder cn/default-neighborhood
                       cs/likelihood-similarity
                       likelihood-recommender))

(defmethod user-based-recommender-builder :rate [args]
  (recommender-builder cn/default-neighborhood
                       cs/rating-similarity
                       rating-recommender))


;;
;; Recommendations generation
;;

(defn recommend
  [recommender user nb-results]
  (map (fn [result] 
         {:item (.getItemID result) 
          :value (.getValue result)})
       (.recommend recommender user nb-results)))


;;
;; Recommendation evaluation
;;

(defn evaluate
  "Evaluate a recommender for the given data model."
  [recommender-builder data-model]
  (-> (AverageAbsoluteDifferenceRecommenderEvaluator.)
      (.evaluate recommender-builder
                 nil
                 data-model
                 0.7
                 1.0)))

(defn- parse-stats
  "Extract stats from recommender evaluation."
  [result]
  {:precision (.getPrecision result)
   :recall (.getRecall result)
   :fallOut (.getFallOut result)
   :reach (.getReach result)
   :ndcg (.getNormalizedDiscountedCumulativeGain result)
   :f1 (.getF1Measure result)})

(defn stats
  "Compute stats for a recommender and a data model."
  [recommender-builder data-model]
  (-> (GenericRecommenderIRStatsEvaluator.)
      (.evaluate recommender-builder
                 nil
                 data-model
                 nil
                 2
                 GenericRecommenderIRStatsEvaluator/CHOOSE_THRESHOLD
                 1.0)
      parse-stats))
