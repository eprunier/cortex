(ns cortex.data-model
  (:require [clojure.java.io :as io])
  (:import [org.apache.mahout.cf.taste.impl.model GenericBooleanPrefDataModel]
           [org.apache.mahout.cf.taste.impl.model.file FileDataModel]))

(defn file-data-model
  [location]
  (FileDataModel. (io/file location)))

(defn likes-file-data-model
  [location]
  (-> location
      file-data-model
      (GenericBooleanPrefDataModel/toDataMap)
      (GenericBooleanPrefDataModel.)))
