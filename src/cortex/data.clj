(ns cortex.data
  (:refer-clojure :exclude [load-file])
  (:require [clojure.java.io :as io])
  (:import [org.apache.mahout.cf.taste.eval DataModelBuilder]
           [org.apache.mahout.cf.taste.impl.model GenericBooleanPrefDataModel]
           [org.apache.mahout.cf.taste.impl.model.file FileDataModel]))


;;
;; File data models
;;

(defn- load-file
  [location]
  (-> location
      io/file
      (FileDataModel.)))

(defn load-ratings-file
  [location]
  (load-file location))

(defn load-likes-file
  [location]
  (-> location
      load-file
      (GenericBooleanPrefDataModel/toDataMap)
      (GenericBooleanPrefDataModel.)))


;;
;; Data model builders
;;

(defn likes-data-builder
  []
  (proxy [DataModelBuilder] []
    (buildDataModel [trainingData]
      (-> trainingData
          (GenericBooleanPrefDataModel/toDataMap)
          (GenericBooleanPrefDataModel.)))))
