(ns morse.api-test
  (:require [clojure.test :refer :all]
            [morse.api :as api]

            [morse.test-utils :refer [capture-request]]))

(deftest send-text-test
  (testing "Sending plain text message"
    (is (= "chat_id=239&text=message"
           (-> (api/send-text 239 "message")
               (capture-request)
               :query-string))))

  (testing "Sending message with additional parameters"
    (is (= "chat_id=239&text=message&parse_mode=Markdown"
           (-> (api/send-text 239 {:parse_mode "Markdown"} "message")
               (capture-request)
               :query-string)))))