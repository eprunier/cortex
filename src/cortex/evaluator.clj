(ns cortex.evaluator
  (:import [org.apache.mahout.cf.taste.impl.eval 
            GenericRecommenderIRStatsEvaluator
            AverageAbsoluteDifferenceRecommenderEvaluator]))

(defn average
  [training-ratio model-percentage]
  (fn [recommender-builder model-builder model]
    (-> (AverageAbsoluteDifferenceRecommenderEvaluator.)
        (.evaluate recommender-builder
                   model-builder
                   model
                   training-ratio
                   model-percentage))))

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
  [nb-reco threshold percentage]
  (fn [recommender-builder model-builder model]
    (-> (GenericRecommenderIRStatsEvaluator.)
        (.evaluate recommender-builder
                   model-builder
                   model
                   nil
                   nb-reco
                   threshold
                   percentage)
        parse-stats)))

(def stats-choose-threshold GenericRecommenderIRStatsEvaluator/CHOOSE_THRESHOLD)
