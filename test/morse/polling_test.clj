(ns morse.polling-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer [chan go >! <!!]]
            [morse.polling :as poll]
            [morse.test-utils :as u]))

(def sample-update [{:update_id 0
                     :message {:text "foo"
                               :chat {:id "bar"}}}])

(defn handler-for [channel]
  (fn [upd]
    (go (>! channel upd))))

(deftest handler-receives-update
  (testing "passing the response from updates function correctly"
    (let [result  (chan)
          handler (handler-for result)
          running (poll/start "token" handler
                              {:get-updates-fn
                               (fn [_ resp-handler _ _]
                                 (go (resp-handler sample-update)))})]
      (is (= (first sample-update) (<!! result)))
      (poll/stop running)))

  (testing "stopping polling process when exception happens"
    (let [running (poll/start "token" identity
                              {:get-updates-fn
                               (fn [_ _ err-handler _]
                                 (go (err-handler (ex-info "should raise" {}))))})]
      (is (nil? (<!! running)))))

  (testing "stopping polling process when reaching global timeout"
    (let [running (poll/start "token" identity
                              {:wait-timeout 1
                               :get-updates-fn
                               (fn [_ _ _ _] :pass)})]
      (is (nil? (<!! running))))))
