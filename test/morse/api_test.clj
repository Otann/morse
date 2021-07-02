(ns morse.api-test
  (:require [clojure.test :refer :all]

            [cheshire.core :as json]

            [morse.api :as m-api]
            [morse.utils.test :as m-u-t]))

;; test data

(def token "fake-token")
(def chat-id 239)
(def message-id 1)
(def inline-query-id 1337)
(def callback-query-id 1338)


;; test cases

(deftest get-file-request
  (let [req  (-> (m-api/get-file token 116)
                 (m-u-t/capture-request))
        body (json/decode (slurp (:body req)) true)]
    ; check that it is now post request
    (is (= :post (:request-method req)))

    ; check that default params are presented
    (is (m-u-t/has-subset? {:file_id 116} [body]))))


(deftest get-user-profile-photos-request
  (let [options {:offset 2 :limit 5}
        req     (-> (m-api/get-user-profile-photos token 1185125 options)
                    (m-u-t/capture-request))
        body    (json/decode (slurp (:body req)) true)]
    ; check that it is now post request
    (is (= :post (:request-method req)))

    ; check that default params are presented
    (is (m-u-t/has-subset? {:user_id 1185125} [body]))

    ; check that a option has encoded
    (is (m-u-t/has-subset? {:offset 2 :limit 5} [body]))))


(deftest send-text-request
  (let [options {:parse_mode   "Markdown"
                 :reply_markup {:keyboard [[{:text "button"}]]}}
        req     (-> (m-api/send-text token chat-id options "message")
                    (m-u-t/capture-request))
        body    (json/decode (slurp (:body req)) true)]
    ; check that it is now post request
    (is (= :post (:request-method req)))

    ; check that default params are presented
    (is (m-u-t/has-subset? {:chat_id 239 :text "message"} [body]))

    ; check that a flat option has encoded
    (is (m-u-t/has-subset? {:parse_mode "Markdown"} [body]))

    ; check that a nested option has encoded
    (is (m-u-t/has-subset? {:reply_markup
                            {:keyboard [[{:text "button"}]]}} [body]))))


(deftest forward-message-request
  (let [[chat-id from-chat-id message-id] [239 240 1]
        req  (-> (m-api/forward-message
                   token chat-id from-chat-id message-id {})
                 (m-u-t/capture-request))
        body (json/decode (slurp (:body req)) true)]

    ; check that it is now post request
    (is (= :post (:request-method req)))

    ; check that default params are presented
    (is (m-u-t/has-subset? {:chat_id      chat-id
                            :from_chat_id from-chat-id
                            :message_id   message-id} [body]))))


(deftest edit-text-request
  (let [options {:parse_mode   "Markdown"
                 :reply_markup {:keyboard [[{:text "button"}]]}}
        req     (-> (m-api/edit-text
                      token chat-id message-id options "edited message")
                    (m-u-t/capture-request))
        body    (json/decode (slurp (:body req)) true)]
    ; check that it is now post request
    (is (= :post (:request-method req)))

    ; check that default params are presented
    (is (m-u-t/has-subset? {:chat_id    chat-id
                            :message_id message-id
                            :text       "edited message"} [body]))

    ; check that a flat option has encoded
    (is (m-u-t/has-subset? {:parse_mode "Markdown"} [body]))

    ; check that a nested option has encoded
    (is (m-u-t/has-subset? {:reply_markup
                            {:keyboard [[{:text "button"}]]}} [body]))))


(deftest delete-text-request
  (let [req  (-> (m-api/delete-text token chat-id message-id)
                 (m-u-t/capture-request))
        body (json/decode (slurp (:body req)) true)]
    ; check that it is now post request
    (is (= :post (:request-method req)))

    ; check that default params are presented
    (is (m-u-t/has-subset? {:chat_id    chat-id
                            :message_id message-id} [body]))))


(deftest send-photo-request
  (let [data (byte-array (map byte "content"))
        req  (-> (m-api/send-photo token chat-id data)
                 (m-u-t/capture-request))
        body (:multipart req)]
    ; check that it is post request
    (is (= :post (:request-method req)))

    ; check that chat_id is presented
    (is (m-u-t/has-subset? {:part-name "chat_id" :content "239"} body))

    ; check that data was passed
    (is (m-u-t/has-subset? {:part-name "photo" :content data} body))

    ; check that "photo" has .jpg filename
    (is (->> body
             (find #(= (:part-name %) "photo"))
             (fn [^String s] (.endsWith s "png"))))))


(deftest answer-inline-request
  (let [res  {:type "gif" :id 31337 :gif_url "cat.gif"}
        req  (-> (m-api/answer-inline token inline-query-id
                                      {:is_personal true}
                                      [res])
                 (m-u-t/capture-request))
        body (json/decode (slurp (:body req)) true)]
    (is (= :post (:request-method req)))
    (is (m-u-t/has-subset? {:inline_query_id inline-query-id} [body]))
    (is (m-u-t/has-subset? {:results [res]} [body]))
    (is (m-u-t/has-subset? {:is_personal true} [body]))))


(deftest answer-callback-request
  (let [req  (-> (m-api/answer-callback token callback-query-id "text" true)
                 (m-u-t/capture-request))
        body (json/decode (slurp (:body req)) true)]
    (is (= :post (:request-method req)))
    (is (m-u-t/has-subset? {:callback_query_id callback-query-id} [body]))
    (is (m-u-t/has-subset? {:text "text"} [body]))
    (is (m-u-t/has-subset? {:show_alert true} [body]))))
