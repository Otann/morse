(ns morse.utils.test
  (:require [clojure.test :as t]
            [clojure.string :as s]

            [clj-http.client :as http]
            [clj-http.fake :refer [with-fake-routes]]
            [cheshire.core :as json]))

(defmacro capture-request
  "Returns the last request that was passed to the remote server
   during the execution of the 'body'."
  [& body]
  `(let [request# (atom nil)]
     (with-fake-routes
       {#"(.*)" (fn [data#]
                  (reset! request# data#)
                  {:status 200 :body ""})}
       ~@body
       @request#)))

(defmacro capture-request-async
  "Returns the last request that was passed to the remote server
   during the execution of the 'body'."
  [& body]
  `(let [memory# (atom nil)]
     (with-redefs [http/get (fn [url# request# on-success# on-failure#]
                              (reset! memory# request#))]
       ~@body
       @memory#)))

(defmacro with-faked-updates
  "Executes 'body', faking the response from the `/getUpdates` method."
  [result & body]
  `(with-redefs [http/get (fn [url# request# on-success# on-failure#]
                            (try
                              (let [result#   (if (t/function? ~result)
                                                (~result)
                                                ~result)
                                    response# {:status 200
                                               :body   (json/generate-string
                                                        {"result" result#})}]
                                (on-success# response#))
                              (catch Exception e#
                                (on-failure# e#))))]
     ~@body))

(defn map-subset?
  "Checks if one map ('sub') is a subset of another map ('super')."
  [sub super]
  (let [sub-keys  (keys sub)
        presented (set (keys super))]
    (and (every? presented sub-keys)
         (every? #(= (sub %) (super %)) sub-keys))))

(defn has-subset?
  "Checks if a 'collection' contains a map that contains all entries
   of the provided map ('sub')."
  [sub collection]
  (some #(map-subset? sub %) collection))

(defn extract-query-set
  "Get's query from the request and retrieves its parameters as a set
   of strings splitting them by an \"&\" char."
  [req]
  (if-let [params (:query-params req)]
    ;; raw request
    (set (for [[k v] params] (str (name k) "=" v)))
    ;; clj-http-fake
    (-> req
        :query-string
        (s/split #"&")
        (set))))
