(ns morse.test-utils
  (:require [clojure.test :as t]
            [clj-http.fake :refer [with-fake-routes]]
            [clojure.string :as s]
            [cheshire.core :as json]
            [clj-http.client :as http]))


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


(defmacro capture-request-async
  "Returns last request that was passed
   to remote server during execution of the body"
  [& body]
  `(let [memory# (atom nil)]
     (with-redefs [http/get (fn [url# request# on-success# on-failure#]
                              (reset! memory# request#))]
       ~@body
       @memory#)))


(defmacro with-faked-updates
  "Executes body, faking response from Telegram's
   /getUpdates method"
  [result & body]
  `(with-redefs [http/get (fn [url# request# on-success# on-failure#]
                            (try
                              (let [result#   (if (t/function? ~result)
                                                (~result)
                                                ~result)
                                    response# {:status 200
                                               :body   (json/generate-string {"result" result#})}]
                                (on-success# response#))
                              (catch Exception e#
                                (on-failure# e#))))]
     ~@body))


(defn map-subset?
  "Checks if one map is subset of another"
  [sub super]
  (let [sub-keys  (keys sub)
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
  (if-let [params (:query-params req)]
    ;; raw request
    (set (for [[k v] params] (str (name k) "=" v)))
    ;; clj-http-fake
    (-> req
        :query-string
        (s/split #"&")
        (set))))

