(ns morse.updates.polling-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :as a :refer [<!!]]

            [morse.updates.polling :as updates.polling]
            [morse.utils.test :as utils.test]))

;; test data

(def token "fake-token")
(def sample-updates [{:update_id 0
                      :message   {:text "foo"
                                  :chat {:id "bar"}}}])


;; aux fns

(defn- <!!?
  [port timeout]
  (let [[result _] (a/alts!! [port (a/timeout timeout)])]
    result))

(defn- handler-for
  [channel]
  (fn [upd]
    (a/put! channel upd)))


;; test cases

(deftest handler-receives-update
  (testing "handler fn receives an update from the poller"
    (utils.test/with-faked-updates
      sample-updates

      (let [result  (a/chan)
            handler (handler-for result)
            running (updates.polling/start! token handler {})]
        (is (= (first sample-updates)
               (<!!? result 1000)))
        (updates.polling/stop! running)))))

(deftest stopping-the-long-polling-process
  (testing "when an exception happens"
    (utils.test/with-faked-updates
      #(throw (ex-info "error" {}))

      (let [running (updates.polling/start! token identity {})]
        (is (nil? (<!!? running 1000))))))

  (testing "when reaching a global timeout"
    (let [running (updates.polling/start! token identity {:timeout 0.001})]
      (is (nil? (<!!? running 1000))))))
