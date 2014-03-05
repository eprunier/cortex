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
   [cortex.model :as cm]))

(def system
  "A Var containing an object representing the application under
  development."
  {:data {:intro "sample-data/intro.csv" 
          :sample "sample-data/ua.base"
          :boolean-sample "sample-data/user_friends.dat"}})

(defn init
  "Creates and initializes the system under development in the Var
  #'system."
  [])

(defn recommender
  [data-key]
  (let [model (cm/load-file (get-in system [:data (keyword data-key)]))
        builder (-> {:similarity cs/log-likelihood
                     :neighborood [cn/nearest-n-users :users 10]
                     :recommender cr/user-based-generic-boolean}
                    (cortex/create-recommender-builder model)) ]
    (cortex/recommender builder model)))

(defn run-score
  [data-key]
  (cortex/score {:similarity cs/log-likelihood
                 :neighborood [cn/nearest-n-users :users 10]
                 :recommender cr/user-based-generic}
                (cm/load-file (get-in system [:data (keyword data-key)]))))

(defn run-score-boolean
  [data-key]
  (cortex/score {:similarity cs/log-likelihood
                 :neighborood [cn/nearest-n-users :users 10]
                 :recommender cr/user-based-generic-boolean}
                (cm/load-boolean-file (get-in system [:data (keyword data-key)]))
                (cm/boolean-model-builder)))

(defn run-stats
  [data-key]
  (cortex/stats {:similarity cs/log-likelihood
                 :neighborood [cn/nearest-n-users :users 10]
                 :recommender cr/user-based-generic}
                (cm/load-file (get-in system [:data (keyword data-key)]))))

(defn run-stats-boolean
  [data-key]
  (cortex/stats {:similarity cs/log-likelihood
                 :neighborood [cn/nearest-n-users :users 10]
                 :recommender cr/user-based-generic-boolean}
                (cm/load-boolean-file (get-in system [:data (keyword data-key)]))
                (cm/boolean-model-builder)))

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
