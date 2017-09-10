(ns morse.api
  (:require [clojure.tools.logging :as log]
            [clj-http.client :as http]
            [clojure.string :as string])
  (:import (java.io File)))


(def base-url "https://api.telegram.org/bot")


(defn get-updates
  "Receive updates from Bot via long-polling endpoint"
  [token {:keys [limit offset timeout]}]
  (let [url (str base-url token "/getUpdates")
        query {:timeout (or timeout 1)
               :offset  (or offset 0)
               :limit   (or limit 100)}
        resp (http/get url {:as :json
                            :query-params query
                            :throw-exceptions false})]
    (if (-> resp :status (< 300))
      (-> resp :body :result)
      (log/error "Telegram returned" (:status resp)
                 "from /getUpdates:" (:body resp)))))


(defn set-webhook
  "Register WebHook to receive updates from chats"
  [token webhook-url]
  (let [url   (str base-url token "/setWebhook")
        query {:url webhook-url}]
    (http/get url {:as :json :query-params query})))


(defn send-text
  "Sends message to the chat"
  ([token chat-id text] (send-text token chat-id {} text))
  ([token chat-id options text]
   (let [url (str base-url token "/sendMessage")
         body (into {:chat_id chat-id :text text} options)
         resp (http/post url {:content-type :json
                              :as :json
                              :form-params body})]
     (-> resp :body))))

(defn edit-text
  "Edits a sent message
  (https://core.telegram.org/bots/api#editmessagetext)"
  ([token chat-id message-id text] (edit-text token chat-id message-id {} text))
  ([token chat-id message-id options text]
    (let [url (str base-url token "/editMessageText")
          query (into {:chat_id chat-id :text text :message_id message-id} options)
          resp (http/post url {:content-type :json
                               :as :json
                               :form-params query})
          ]
         (-> resp :body))))

(defn send-file [token chat-id options file method field filename]
  "Helper function to send various kinds of files as multipart-encoded"
  (let [url (str base-url token method)
        base-form [{:part-name "chat_id" :content (str chat-id)}
                   {:part-name field :content file :name filename}]
        options-form (for [[key value] options]
                       {:part-name (name key) :content value})
        form (into base-form options-form)
        resp (http/post url {:as :json :multipart form})]
    (-> resp :body)))


(defn is-file?
  "Is value a file?"
  [value] (= File (type value)))


(defn of-type?
  "Does the extension of file match any of the
  extensions in valid-extensions?"
  [file valid-extensions]
  (some #(-> file .getName (.endsWith %))
        valid-extensions))


(defn assert-file-type
  "Throws if value is a file but it's extension is not valid."
  [value valid-extensions]
  (when (and (is-file? value)
             (not (of-type? value valid-extensions)))
    (throw (ex-info (str "Telegram API only supports the following formats: "
                         (string/join ", " valid-extensions)
                         " for this method. Other formats may be sent using send-document")
                    {}))))


(defn send-photo
  "Sends an image to the chat"
  ([token chat-id image] (send-photo token chat-id {} image))
  ([token chat-id options image]
   (assert-file-type image ["jpg" "jpeg" "gif" "png" "tif" "bmp"])
   (send-file token chat-id options image "/sendPhoto" "photo" "photo.png")))


(defn send-document
  "Sends a document to the chat"
  ([token chat-id document] (send-document token chat-id {} document))
  ([token chat-id options document]
   (send-file token chat-id options document "/sendDocument" "document" "document")))


(defn send-video
  "Sends a video to the chat"
  ([token chat-id video] (send-video token chat-id {} video))
  ([token chat-id options video]
   (assert-file-type video ["mp4"])
   (send-file token chat-id options video "/sendVideo" "video" "video.mp4")))


(defn send-audio
  "Sends an audio message to the chat"
  ([token chat-id audio] (send-audio token chat-id {} audio))
  ([token chat-id options audio]
   (assert-file-type audio ["mp3"])
   (send-file token chat-id options audio "/sendAudio" "audio" "audio.mp3")))


(defn send-sticker
  "Sends a sticker to the chat"
  ([token chat-id sticker] (send-sticker token chat-id {} sticker))
  ([token chat-id options sticker]
   (assert-file-type sticker ["webp"])
   (send-file token chat-id options sticker "/sendSticker" "sticker" "sticker.webp")))

(defn answer-inline
  "Sends an answer to an inline query"
  ([token inline-query-id results] (answer-inline token inline-query-id {} results))
  ([token inline-query-id options results]
    (let [url (str base-url token "/answerInlineQuery")
          body (into {:inline_query_id inline-query-id :results results} options)
          resp (http/post url {:content-type :json
                               :as :json
                               :form-params body})]
     (-> resp :body))))

(defn answer-callback
 "Sends an answer to an callback query"
 ([token callback-query-id] (answer-callback token callback-query-id "" false))
 ([token callback-query-id text] (answer-callback token callback-query-id text false))
 ([token callback-query-id text show-alert]
   (let [url (str base-url token "/answerCallbackQuery")
         body {:callback_query_id callback-query-id :text text :show_alert show-alert}
         resp (http/post url {:content-type :json
                              :as :json
                              :form-params body})]
    (-> resp :body))))
