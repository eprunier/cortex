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
   [cortex.data :as cd]))

(def system
  "A Var containing an object representing the application under
  development."
  {:ratings-path "sample-data/ua.base"
   :likes-path "sample-data/user_friends.dat"})

(defn init
  "Creates and initializes the system under development in the Var
  #'system."
  []
  (alter-var-root #'system
                  (fn [system]
                    (assoc system
                      :ratings (-> system :ratings-path cd/load-ratings-file)
                      :likes (-> system :likes-path cd/load-likes-file)))))

(defn score-ubl-recommender
  []
  (let [recommender-builder (cortex/user-based-recommender-builder :likes)
        data-model (-> system
                       :likes-path
                       cd/load-likes-file)]
    (cortex/score recommender-builder
                  data-model)))

(defn stats-ubl-recommender
  []
  (let [recommender-builder (cortex/user-based-recommender-builder :likes)
        data-model (-> system
                       :likes-path
                       cd/load-likes-file)]
    (cortex/stats recommender-builder
                  data-model)))

(defn score-ubr-recommender
  []
  (let [recommender-builder (cortex/user-based-recommender-builder :ratings)
        data-model (-> system
                       :ratings-path
                       cd/load-ratings-file)]
    (cortex/score recommender-builder
                  data-model)))

(defn stats-ubr-recommender
  []
  (let [recommender-builder (cortex/user-based-recommender-builder :ratings)
        data-model (-> system
                       :ratings-path
                       cd/load-ratings-file)]
    (cortex/stats recommender-builder
                  data-model)))

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
