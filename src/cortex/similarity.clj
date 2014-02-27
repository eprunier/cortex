(ns cortex.similarity
  (:import [org.apache.mahout.cf.taste.impl.similarity
            PearsonCorrelationSimilarity
            LogLikelihoodSimilarity]))

(defn likes-similarity
  [model]
  (LogLikelihoodSimilarity. model))

(defn ratings-similarity
  [model]
  (PearsonCorrelationSimilarity. model))
