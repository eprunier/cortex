(ns cortex.core
  (:import [org.apache.mahout.cf.taste.eval RecommenderBuilder])
  (:require [cortex.evaluator :as eval]))

;;
;; ## Recommender builder
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
  (fn [model]
    (specs (similarity-fn model) model)))

(defn- parse-recommender
  "Parse specs and return a recommender function that takes a data model as parameter."
  [specs similarity-fn neighborhood-fn]
  (let [recommender-fn (if (vector? specs) (first specs) specs)]
    (fn [model]
      (recommender-fn (similarity-fn model) 
                      (neighborhood-fn model) 
                      model))))

(defn create-recommender-builder
  "Parse specs and create a recommender builder which can be used 
   for recommender creation or recommender evaluation.

   Exemple of specs for a user based recommender with 
   nearest n users neighborood and pearson correlation similiarity:

       {:similarity cs/pearson-correlation
        :neighborood (cn/nearest-n-users 100)
        :recommender cr/user-based-generic}"
  [{:keys [similarity neighborood recommender] :as specs} model]
  (let [similarity-fn (parse-similarity similarity)
        neighborood-fn (parse-neighborhood neighborood similarity-fn)
        recommender-fn (parse-recommender recommender similarity-fn neighborood-fn)]
    (reify RecommenderBuilder
      (buildRecommender [this model]
        (recommender-fn model)))))

(defn recommender
  "Create a recommender based on the given builder and data model."
  [recommender-builder model]
  (.buildRecommender recommender-builder 
                     model))


;;
;; ## Recommendations generation
;;

(defn recommend
  "Compute a list of recommendations."
  [recommender user nb-results]
  (map (fn [result] 
         {:item (.getItemID result) 
          :value (.getValue result)})
       (.recommend recommender user nb-results)))


;;
;; ## Recommendation evaluation
;;

(defn evaluate
  "Compute the score for a recommender defined by reco-specs and 
   an evaluator defined by eval-specs.

   Exemple of specs:

     {:model (model/load-file \"path-to-file\")
      :evaluator (evaluator/average 0.9 0.1)}

  Exemple of specs for a data model without preferences:

    {:model (model/load-boolean-file \"path-to-file\")
     :model-builder (model/boolean-model-builder)
     :evaluator (evaluator/average 0.9 0.1)}"
  [reco-specs 
   {:keys [model model-builder evaluator] :as eval-specs}]
  (evaluator (create-recommender-builder reco-specs model)
             model-builder
             model))

