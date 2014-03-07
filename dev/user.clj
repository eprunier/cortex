(ns user
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
  (:require
   [clojure.java.io :as io]
   [clojure.java.javadoc :refer (javadoc)]
   [clojure.pprint :refer (pprint)]
   [clojure.reflect :refer (reflect)]
   [clojure.repl :refer (apropos dir doc find-doc pst source)]
   [clojure.set :as set]
   [clojure.string :as str]
   [clojure.test :as test]
   [clojure.tools.namespace.repl :refer (refresh refresh-all)]
   [cortex.core :as cortex]
   [cortex.similarity :as cs]   
   [cortex.neighborhood :as cn]
   [cortex.recommender :as cr]
   [cortex.model :as cm]
   [cortex.evaluator :as ce]))

(def system
  "A Var containing an object representing the application under
  development."
  {:data {:ratings-small "sample-data/intro.csv" 
          :ratings-100k "sample-data/ratings-100k.csv.gz"
          :ratings-1M "sample-data/ratings-1M.csv.gz"
          :ratings-10M "sample-data/ratings-10M.csv.gz"
          :boolean-sample "sample-data/user_friends.dat"}})

(defn init
  "Creates and initializes the system under development in the Var
  #'system."
  [])

(defn recommender
  [data-key]
  (let [model (cm/load-file (get-in system [:data (keyword data-key)]))
        builder (-> {:similarity cs/log-likelihood
                     :neighborood (cn/nearest-n-users 10)
                     :recommender cr/user-based-generic-boolean}
                    (cortex/create-recommender-builder model)) ]
    (cortex/recommender builder model)))

(defn evaluate-score
  [data-key ratio percentage]
  (cortex/evaluate {:similarity cs/pearson-correlation
                    :neighborood (cn/nearest-n-users 100)
                    :recommender cr/user-based-generic}
                   {:model (cm/load-file (get-in system [:data (keyword data-key)]))
                    :evaluator (ce/average ratio percentage)}))

(defn evaluate-score-boolean
  [data-key ratio percentage]
  (cortex/evaluate {:similarity cs/log-likelihood
                    :neighborood (cn/nearest-n-users 10)
                    :recommender cr/user-based-generic-boolean}
                   {:model (cm/load-boolean-file (get-in system [:data (keyword data-key)]))
                    :model-builder (cm/boolean-model-builder)
                    :evaluator (ce/average ratio percentage)}))

(defn evaluate-stats
  [data-key nb-reco percentage]
  (cortex/evaluate {:similarity cs/log-likelihood
                    :neighborood (cn/nearest-n-users 10)
                    :recommender cr/user-based-generic}
                   {:model (cm/load-file (get-in system [:data (keyword data-key)]))
                    :evaluator (ce/stats nb-reco ce/stats-choose-threshold percentage)}))

(defn evaluate-stats-boolean
  [data-key nb-reco percentage]
  (cortex/evaluate {:similarity cs/log-likelihood
                    :neighborood (cn/nearest-n-users 10)
                    :recommender cr/user-based-generic-boolean}
                   {:model (cm/load-file (get-in system [:data (keyword data-key)]))
                    :model-builder (cm/boolean-model-builder)
                    :evaluator (ce/stats nb-reco ce/stats-choose-threshold percentage)}))

(defn stop
  "Stops the system if it is currently running, updates the Var
  #'system."
  []
  (alter-var-root #'system
                  (fn [system]
                    (dissoc system :ratings :likes))))

(defn go
  "Initializes and starts the system running."
  []
  (init)
  :ready)

(defn reset
  "Stops the system, reloads modified source files, and restarts it."
  []
  (stop)
  (refresh :after 'user/go))
