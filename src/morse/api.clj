(ns morse.api
  (:require [clj-http.client :as http]))

(def base-url "https://api.telegram.org/bot")

(def token (atom nil))

(defn get-updates
  "Receive updates from Bot via long-polling endpoint"
  [{:keys [limit offset timeout]}]
  (let [url (str base-url @token "/getUpdates")
        query {:timeout (or timeout 1)
               :offset  (or offset 0)
               :limit   (or limit 100)}
        resp (http/get url {:as :json :query-params query})]
    (-> resp :body :result)))

(defn set-webhook
  "Register WebHook to receive updates from chats"
  [webhook-url]
  (let [url   (str base-url @token "/setWebhook")
        query {:url webhook-url}]
    (http/get url {:as :json :query-params query})))

(defn send-text
  "Sends message to the chat"
  ([chat-id text] (send-text chat-id {} text))
  ([chat-id options text]
   (let [url (str base-url @token "/sendMessage")
         query (into {:chat_id chat-id :text text} options)
         resp (http/get url {:as :json :query-params query})]
     (-> resp :body))))

(defn send-photo
  "Send image to the chat"
  ([chat-id image] (send-photo chat-id {} image))
  ([chat-id options image]
   (let [url (str base-url @token "/sendPhoto")
         base-form [{:part-name "chat_id" :content (str chat-id)}
                    {:part-name "photo" :content image :name "photo.png"}]
         options-form (for [[key value] options]
                        {:part-name (name key) :content value})
         form (into base-form options-form)
         resp (http/post url {:as :json :multipart form})]
     (-> resp :body))))

