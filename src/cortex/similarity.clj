(ns cortex.similarity
  (:import [org.apache.mahout.cf.taste.impl.similarity
            PearsonCorrelationSimilarity
            LogLikelihoodSimilarity]))

(defn likelihood-similarity
  [model]
  (LogLikelihoodSimilarity. model))

(defn rating-similarity
  [model]
  (PearsonCorrelationSimilarity. model))
