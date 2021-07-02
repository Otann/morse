(ns morse.updates.polling-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :as a :refer [<!!]]

            [morse.updates.polling :as m-u-poll]
            [morse.utils.test :as m-u-t]))

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
    (m-u-t/with-faked-updates
      sample-updates

      (let [result  (a/chan)
            handler (handler-for result)]
        (m-u-poll/start! token handler {})
        (is (= (first sample-updates)
               (<!!? result 1000)))
        (m-u-poll/stop!)))))

(deftest stopping-the-long-polling-process
  (testing "when an exception happens"
    (m-u-t/with-faked-updates
      #(throw (ex-info "error" {}))

      (m-u-poll/start! token identity {})
      (<!! (a/timeout 1000))
      (is (= true (m-u-poll/has-stopped?)))))

  (testing "when reaching a global timeout"
    (m-u-poll/start! token identity {:timeout 0.001})
    (<!! (a/timeout 1000))
    (is (= true (m-u-poll/has-stopped?)))))
