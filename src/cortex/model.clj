(ns cortex.model
  (:refer-clojure :exclude [load-file])
  (:require [clojure.java.io :as io])
  (:import [org.apache.mahout.cf.taste.eval DataModelBuilder]
           [org.apache.mahout.cf.taste.impl.model GenericBooleanPrefDataModel]
           [org.apache.mahout.cf.taste.impl.model.file FileDataModel]))

;;
;; ## File data models
;;

(defn load-file
  [location]
  (-> location
      io/file
      (FileDataModel.)))

(defn load-boolean-file
  [location]
  (-> location
      load-file
      (GenericBooleanPrefDataModel/toDataMap)
      (GenericBooleanPrefDataModel.)))


;;
;; ## Data model builders
;;

(defn boolean-model-builder
  []
  (reify DataModelBuilder
    (buildDataModel [this trainingData]
      (-> trainingData
          (GenericBooleanPrefDataModel/toDataMap)
          (GenericBooleanPrefDataModel.)))))
