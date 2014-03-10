(ns cortex.similarity
  (:import [org.apache.mahout.cf.taste.impl.similarity
            PearsonCorrelationSimilarity
            LogLikelihoodSimilarity
            EuclideanDistanceSimilarity]))

(defn log-likelihood
  [model]
  (LogLikelihoodSimilarity. model))

(defn pearson-correlation
  "Pearson correlation requires preference values."
  [model]
  (PearsonCorrelationSimilarity. model))

(defn euclidean-distance
  "Euclidean distance requires preference values."
  [model]
  (EuclideanDistanceSimilarity. model))
