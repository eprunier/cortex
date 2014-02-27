(ns cortex.core
  (:import [org.apache.mahout.cf.taste.eval RecommenderBuilder]
           [org.apache.mahout.cf.taste.impl.eval 
            GenericRecommenderIRStatsEvaluator
            AverageAbsoluteDifferenceRecommenderEvaluator])
  (:require [cortex.similarity :as cs]
            [cortex.neighborhood :as cn]
            [cortex.recommender :as cr]))


;;
;; Recommender builder
;;

(defmulti user-based-recommender-builder 
  "User based recommender definition based on type which can be :like or :rate.
Exemple: (user-based-recommender {:type :like :data-specs \"/tmp/data.csv\"})" 
  keyword)

(defmethod user-based-recommender-builder :likes [args]
  (cr/recommender-builder cs/likes-similarity
                          cn/default-neighborhood
                          cr/likes-recommender))

(defmethod user-based-recommender-builder :ratings [args]
  (cr/recommender-builder cs/ratings-similarity
                          cn/default-neighborhood
                          cr/ratings-recommender))


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

(defn score
  "Compute the score for a recommender and a data model."
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
