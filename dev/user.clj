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
   [cortex.data-model :as data-model]
   [cortex.neighborhood :as neighborhood]
   [cortex.similarity :as similarity]))

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
                      :ratings (-> system :ratings-path data-model/file-data-model)
                      :likes (-> system :likes-path data-model/likes-file-data-model)))))

(defn start
  "Starts the system running, updates the Var #'system."
  [])

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
  (start)
  :ready)

(defn reset
  "Stops the system, reloads modified source files, and restarts it."
  []
  (stop)
  (refresh :after 'user/go))
