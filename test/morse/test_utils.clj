(ns morse.test-utils
  (:require [clojure.test :as t]
            [clj-http.fake :refer [with-fake-routes]]
            [clojure.string :as s]
            [cheshire.core :as json]))

(defmacro capture-request
  "Returns last request that was passed
   to remote server during execution of the body"
  [& body]
  `(let [request# (atom nil)]
     (with-fake-routes
       {#"(.*)" (fn [data#]
                  (reset! request# data#)
                  {:status 200 :body ""})}
       ~@body
       @request#)))

(defmacro with-faked-updates
  "Executes body, faking response from Telegram's
   /getUpdates method"
  [result & body]
  `(let [result# (if (t/function? ~result)
                   (~result)
                   ~result)
         response# {:status 200
                    :body (json/generate-string {:result result#})}]
     (with-fake-routes {#"(.*)/getUpdates(.*)" (fn [_#] response#)}
      ~@body)))

(defn log-request
  "Logs request to console and passes further on
   To be used in threading macro"
  [req]
  (println "request:" req)
  req)

(defn in?
  "Checks if element is presented in the collection"
  [element collection]
  (some #(= element %) collection))

(defn map-subset?
  "Checks if one map is subset of another"
  [sub super]
  (let [sub-keys (keys sub)
        presented (set (keys super))]
    (and (every? presented sub-keys)
         (every? #(= (sub %) (super %)) sub-keys))))

(defn has-subset?
  "Checks if collection contains map that has
   all the content of the provided map"
  [sub collection]
  (some #(map-subset? sub %) collection))

(defn extract-query-set
  "Get's query from request and splits parameters
   into strings by & sign"
  [req]
  (-> req
      :query-string
      (s/split #"&")
      (set)))

