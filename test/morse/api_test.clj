(ns morse.api-test
  (:require [cheshire.core :as json]
            [clojure.test :refer :all]
            [morse.api :as api]
            [morse.test-utils :as u]))

(def token "fake-token")
(def chat-id 239)

(deftest send-text-request
  (let [options {:parse_mode "Markdown" :reply_markup {:keyboard [[{:text "button"}]]}}
        req (-> (api/send-text token chat-id options "message")
                (u/capture-request))
        body (json/decode (slurp (:body req)) true)]

    ; check that it is now post request
    (is (= :post (:request-method req)))

    ; check that default params are presented
    (is (u/has-subset? {:chat_id 239 :text "message"} [body]))

    ; check that a flat option has encoded
    (is (u/has-subset? {:parse_mode "Markdown"} [body]))

    ; check that a nested option has encoded
    (is (u/has-subset? {:reply_markup {:keyboard [[{:text "button"}]]}} [body]))))

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
         (-> (api/get-updates token {})
             (u/capture-request)
             (u/extract-query-set))))

  (is (= #{"timeout=1" "offset=0" "limit=200"}
         (-> (api/get-updates token {:limit 200})
             (u/capture-request)
             (u/extract-query-set))))

  (is (= #{"timeout=1" "offset=31337" "limit=100"}
         (-> (api/get-updates token {:offset 31337})
             (u/capture-request)
             (u/extract-query-set))))

  (testing "method returns part of the reponse body"
    (let [updates {:foo "bar"}]
      (u/with-faked-updates updates
        (is (= updates
               (api/get-updates token {})))))))