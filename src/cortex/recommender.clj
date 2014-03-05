(ns cortex.recommender
  (:import [org.apache.mahout.cf.taste.impl.recommender 
            GenericUserBasedRecommender
            GenericBooleanPrefUserBasedRecommender]
           [org.apache.mahout.cf.taste.eval RecommenderBuilder]))


(defn user-based-generic-boolean
  [similarity neighborhood model]
  (GenericBooleanPrefUserBasedRecommender. model neighborhood similarity))

(defn user-based-generic
  [similarity neighborhood model]
  (GenericUserBasedRecommender. model neighborhood similarity))
