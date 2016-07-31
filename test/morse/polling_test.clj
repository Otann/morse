(ns morse.polling-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer [chan go >! <!!]]
            [morse.polling :as poll]
            [morse.test-utils :as u]))

(def sample-update {:update_id 0
                    :message {:text "foo"
                              :chat {:id "bar"}}})

(defn handler-for [channel]
  (fn [upd] (go (>! channel upd))))

(deftest handler-receives-update
  (u/with-faked-updates [sample-update]
    (let [result  (chan)
          handler (handler-for result)
          running (poll/start "token" handler)]
      (is (= sample-update (<!! result)))
      (poll/stop running))))

