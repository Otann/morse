(ns morse.test-utils
  (:require [clj-http.fake :refer [with-fake-routes]]))

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
