(ns cortex.core
  (:import [org.apache.mahout.cf.taste.eval RecommenderBuilder]
           [org.apache.mahout.cf.taste.impl.eval 
            GenericRecommenderIRStatsEvaluator
            AverageAbsoluteDifferenceRecommenderEvaluator]))

;;
;; Recommender builder
;;

(defn- parse-similarity
  "Parse specs and return a similarity function."
  [specs]
  (if (vector? specs)
    (first specs)
    specs))

(defn- parse-neighborhood
  "Parse specs and return a neighborood function that takes a data model as parameter."
  [specs similarity-fn]
  (if (vector? specs)
    (let [neighborood-fn (first specs)
          options (rest specs)]
      (fn [model]
        (neighborood-fn (similarity-fn model) model options)))
    (fn [model]
      (specs (similarity-fn model) model))))

(defn- parse-recommender
  "Parse specs and return a recommender function that takes a data model as parameter."
  [specs similarity-fn neighborhood-fn]
  (let [recommender-fn (if (vector? specs) (first specs) specs)]
    (fn [model]
      (recommender-fn (similarity-fn model) 
                      (neighborhood-fn model) 
                      model))))

(defn create-recommender-builder
  "Parse specs and create a recommender."
  [{:keys [similarity neighborood recommender]} model]
  (let [similarity-fn (parse-similarity similarity)
        neighborood-fn (parse-neighborhood neighborood similarity-fn)
        recommender-fn (parse-recommender recommender similarity-fn neighborood-fn)]
    (proxy [RecommenderBuilder] []
      (buildRecommender [model]
        (recommender-fn model)))))

(defn recommender
  "Create a recommender based on the given builder and data model."
  [recommender-builder model]
  (.buildRecommender recommender-builder 
                     model))


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
  [specs model & [model-builder]]
  (-> (AverageAbsoluteDifferenceRecommenderEvaluator.)
      (.evaluate (create-recommender-builder specs model)
                 model-builder
                 model
                 0.9
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
  [specs model & [model-builder]]
  (-> (GenericRecommenderIRStatsEvaluator.)
      (.evaluate (create-recommender-builder specs model)
                 model-builder
                 model
                 nil
                 10
                 GenericRecommenderIRStatsEvaluator/CHOOSE_THRESHOLD
                 1.0)
      parse-stats))
