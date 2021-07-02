(ns morse.updates.json
  "Internal JSON parsing utilities"
  (:require [cheshire.core :as json]))

(defn parse-json-str
  [str]
  (json/parse-string str true))
