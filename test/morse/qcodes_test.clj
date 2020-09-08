(ns morse.api-test
  (:require [cheshire.core :as json]
            [clojure.test :refer :all]
            [morse.handlers :as h]
            [morse.qcodes :as q]
            [morse.test-utils :as u]))

(defn command-message [command]
  {:text (str "/" command)
   :chat {:id "fake-chat-id"}})

(deftest direct-reply-handler
  (let [handler (q/direct-reply "fake-token" (h/command-fn "start" (constantly "change")))
        start-resp (handler {:message (command-message "start")})
        other-resp (handler {:message (command-message "stop")})
        direct-reply-request (-> start-resp meta :direct-reply (u/capture-request))
        body    (json/decode (slurp (:body direct-reply-request)) true)]

    ; check direct reply does not mess with the message when it shouldn't
    (is (= other-resp nil))
    
    ; check that it returns a status 200
    (is (= start-resp {:status 200}))
    
    ; check that direct-reply is now post request
    (is (= :post (:request-method direct-reply-request)))

    ; check that default params are presented
    (is (u/has-subset? {:chat_id "fake-chat-id" :text "change"} [body]))))
