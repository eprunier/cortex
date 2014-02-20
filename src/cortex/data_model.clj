(ns cortex.data-model
  (:require [clojure.java.io :as io])
  (:import [org.apache.mahout.cf.taste.impl.model.file FileDataModel]))

(defn file-data-model
  [location]
  (FileDataModel. (io/file location)))
