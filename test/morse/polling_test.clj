(ns morse.polling-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer [chan go >! <!!]]
            [morse.polling :as poll]
            [morse.test-utils :as u]
            [clojure.core.async :as a]))

(def sample-update [{:update_id 0
                     :message {:text "foo"
                               :chat {:id "bar"}}}])

(defn <!!? [port timeout]
  (let [[result _] (a/alts!! [port (a/timeout timeout)])]
    result))

(defn handler-for [channel]
  (fn [upd]
    (a/put! channel upd)))

(deftest handler-receives-update
  (testing "passing the response from updates function correctly"
    (u/with-faked-updates sample-update
      (let [result  (chan)
            handler (handler-for result)
            running (poll/start "token" handler {})]
        (is (= (first sample-update)
               (<!!? result 1000)))
        (poll/stop running))))

  (testing "stopping polling process when exception happens"
    (u/with-faked-updates #(throw (ex-info "error" {}))
      (let [running (poll/start "token" identity {})]
        (is (nil? (<!!? running 1000))))))

  (testing "stopping polling process when reaching global timeout"
    (let [running (poll/start "token" identity {:timeout 0.001})]
      (is (nil? (<!!? running 1000))))))
