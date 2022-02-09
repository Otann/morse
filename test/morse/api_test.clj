(ns morse.api-test
  (:require [cheshire.core :as json]
            [clojure.test :refer :all]
            [morse.api :as api]
            [morse.test-utils :as u]
            [clojure.core.async :as a]))

(def token "fake-token")
(def chat-id 239)
(def message-id 1)
(def inline-query-id 1337)
(def callback-query-id 1338)

(deftest extract-data-from-update
  (let [update-obj {:body "{\"key-num\": 1, \"key-ar-str\": [\"astring\"]}"}
        result {:key-num 1 :key-ar-str ["astring"]}]
    (is (= (api/extract-data update-obj) result))))

(deftest get-file-request
  (let [req (-> (api/get-file token 116)
                (u/capture-request))
        body (json/decode (slurp (:body req)) true)]

    ; check that it is now post request
    (is (= :post (:request-method req)))

    ; check that default params are presented
    (is (u/has-subset? {:file_id 116} [body]))))

(deftest get-user-profile-photos-request
  (let [options {:offset 2 :limit 5}
        req     (-> (api/get-user-profile-photos token 1185125 options)
                    (u/capture-request))
        body    (json/decode (slurp (:body req)) true)]

    ; check that it is now post request
    (is (= :post (:request-method req)))

    ; check that default params are presented
    (is (u/has-subset? {:user_id 1185125} [body]))

    ; check that a option has encoded
    (is (u/has-subset? {:offset 2 :limit 5} [body]))))

(deftest send-text-request
  (let [options {:parse_mode "Markdown" :reply_markup {:keyboard [[{:text "button"}]]}}
        req     (-> (api/send-text token chat-id options "message")
                    (u/capture-request))
        body    (json/decode (slurp (:body req)) true)]

    ; check that it is now post request
    (is (= :post (:request-method req)))

    ; check that default params are presented
    (is (u/has-subset? {:chat_id 239 :text "message"} [body]))

    ; check that a flat option has encoded
    (is (u/has-subset? {:parse_mode "Markdown"} [body]))

    ; check that a nested option has encoded
    (is (u/has-subset? {:reply_markup {:keyboard [[{:text "button"}]]}} [body]))))

(deftest forward-message-request
  (let [[chat-id
         from-chat-id
         message-id] [239 240 1]
        req          (-> (api/forward-message token chat-id from-chat-id message-id {})
                         (u/capture-request))
        body         (json/decode (slurp (:body req)) true)]

    ; check that it is now post request
    (is (= :post (:request-method req)))

    ; check that default params are presented
    (is (u/has-subset? {:chat_id      chat-id
                        :from_chat_id from-chat-id
                        :message_id   message-id} [body]))))

(deftest edit-text-request
  (let [options {:parse_mode "Markdown" :reply_markup {:keyboard [[{:text "button"}]]}}
        req     (-> (api/edit-text token chat-id message-id options "edited message")
                    (u/capture-request))
        body    (json/decode (slurp (:body req)) true)]

    ; check that it is now post request
    (is (= :post (:request-method req)))

    ; check that default params are presented
    (is (u/has-subset? {:chat_id    chat-id
                        :message_id message-id
                        :text       "edited message"} [body]))

    ; check that a flat option has encoded
    (is (u/has-subset? {:parse_mode "Markdown"} [body]))

    ; check that a nested option has encoded
    (is (u/has-subset? {:reply_markup {:keyboard [[{:text "button"}]]}} [body]))))

(deftest delete-text-request
  (let [req  (-> (api/delete-text token chat-id message-id)
                 (u/capture-request))
        body (json/decode (slurp (:body req)) true)]
    ; check that it is now post request
    (is (= :post (:request-method req)))

    ; check that default params are presented
    (is (u/has-subset? {:chat_id    chat-id
                        :message_id message-id} [body]))))

(deftest send-photo-request
  (let [data (byte-array (map byte "content"))
        req  (-> (api/send-photo token chat-id data)
                 (u/capture-request))
        body (:multipart req)]

    ; check that it is post request
    (is (= :post (:request-method req)))

    ; check that chat_id is presented
    (is (u/has-subset? {:part-name "chat_id" :content "239"} body))

    ; check that data was passed
    (is (u/has-subset? {:part-name "photo" :content data} body))

    ; check that "photo" has .jpg filename
    (is (->> body
             (find #(= (:part-name %) "photo"))
             (fn [^String s] (.endsWith s "png"))))))

(deftest get-updates-request
  (is (= #{"timeout=1" "offset=0" "limit=100"}
         (-> (api/get-updates-async token {})
             (u/capture-request-async)
             (u/extract-query-set))))

  (is (= #{"timeout=1" "offset=0" "limit=200"}
         (-> (api/get-updates-async token {:limit 200})
             (u/capture-request-async)
             (u/extract-query-set))))

  (is (= #{"timeout=1" "offset=31337" "limit=100"}
         (-> (api/get-updates-async token {:offset 31337})
             (u/capture-request-async)
             (u/extract-query-set))))

  (testing "method returns part of the reponse body"
    (let [updates {:foo "bar"}]
      (u/with-faked-updates updates
        (is (= updates (a/<!! (api/get-updates-async token {}))))))))

(deftest answer-inline-request
  (let [req  (-> (api/answer-inline token inline-query-id
                                    {:is_personal true}
                                    [{:type "gif" :id 31337 :gif_url "gif.gif"}])
                 (u/capture-request))
        body (json/decode (slurp (:body req)) true)]

    (is (= :post (:request-method req)))
    (is (u/has-subset? {:inline_query_id inline-query-id} [body]))
    (is (u/has-subset? {:results [{:type "gif" :id 31337 :gif_url "gif.gif"}]} [body]))
    (is (u/has-subset? {:is_personal true} [body]))))

(deftest answer-callback-request
  (let [req  (-> (api/answer-callback token callback-query-id "text" true)
                 (u/capture-request))
        body (json/decode (slurp (:body req)) true)]

    (is (= :post (:request-method req)))
    (is (u/has-subset? {:callback_query_id callback-query-id} [body]))
    (is (u/has-subset? {:text "text"} [body]))
    (is (u/has-subset? {:show_alert true} [body]))))
