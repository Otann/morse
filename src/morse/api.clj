(ns morse.api
  "The available methods of the Telegram Bot API"
  (:require [clj-http.client :as http]
            [clojure.string :as string])
  (:import [java.io File]))


(def base-url "https://api.telegram.org/bot")


(defn get-file
  "Gets a URL of the file."
  [token file-id]
  (let [url  (str base-url token "/getFile")
        body {:file_id file-id}
        resp (http/post url {:content-type :json
                             :form-params  body
                             :as           :json})]
    (-> resp :body)))


(defn get-user-profile-photos
  "Gets the user profile photos object."
  ([token user-id]
   (get-user-profile-photos token user-id {}))
  ([token user-id options]
   (let [url  (str base-url token "/getUserProfilePhotos")
         body (into {:user_id user-id} options)
         resp (http/post url {:content-type :json
                              :form-params  body
                              :as           :json})]
     (-> resp :body))))


(defn send-text
  "Sends message to the chat."
  ([token chat-id text]
   (send-text token chat-id {} text))
  ([token chat-id options text]
   (let [url  (str base-url token "/sendMessage")
         body (into {:chat_id chat-id
                     :text    text}
                    options)
         resp (http/post url {:content-type :json
                              :form-params  body
                              :as           :json})]
     (-> resp :body))))

(defn forward-message
  "Forwards a message from one chat to another."
  ([token chat-id from-chat-id message-id]
   (forward-message token chat-id from-chat-id message-id {}))
  ([token chat-id from-chat-id message-id options]
   (let [url  (str base-url token "/forwardMessage")
         body (into {:chat_id      chat-id
                     :from_chat_id from-chat-id
                     :message_id   message-id}
                    options)
         resp (http/post url {:content-type :json
                              :form-params  body
                              :as           :json})]
     (-> resp :body))))

(defn edit-text
  "Edits the text of a previously sent message."
  ([token chat-id message-id text]
   (edit-text token chat-id message-id {} text))
  ([token chat-id message-id options text]
   (let [url   (str base-url token "/editMessageText")
         query (into {:chat_id    chat-id
                      :text       text
                      :message_id message-id}
                     options)
         resp  (http/post url {:content-type :json
                               :form-params  query
                               :as           :json})]
     (-> resp :body))))


(defn delete-text
  "Removes a message from the chat."
  [token chat-id message-id]
  (let [url   (str base-url token "/deleteMessage")
        query {:chat_id    chat-id
               :message_id message-id}
        resp  (http/post url {:content-type :json
                              :form-params  query
                              :as           :json})]
    (-> resp :body)))


(defn- is-file?
  "Is the 'value' a File?"
  [value]
  (= File (type value)))

(defn- of-type?
  "Checks if the extension of the 'file' matches any of the 'valid-extensions'."
  [file valid-extensions]
  (some #(-> file .getName (.endsWith %))
        valid-extensions))

(defn- assert-file-type
  "Throws an exception if the 'value' is a file with an invalid extension."
  [value valid-extensions]
  (when (and (is-file? value)
             (not (of-type? value valid-extensions)))
    (throw (ex-info (str "The Bot API method only supports these formats: "
                         (string/join ", " valid-extensions)
                         " Try sending the file using the `send-document`.")
                    {}))))


(defn send-file
  "Helper function for sending files of various types with multipart encoding."
  [token chat-id options file method field filename]
  (let [url          (str base-url token method)
        base-form    [{:part-name "chat_id"
                       :content   (str chat-id)}
                      {:part-name field
                       :content   file
                       :name      filename}]
        options-form (for [[key value] options]
                       {:part-name (name key) :content value})
        form         (into base-form options-form)
        resp         (http/post url {:multipart form
                                     :as        :json})]
    (-> resp :body)))


(defn send-photo
  "Sends an image to the chat."
  ([token chat-id image]
   (send-photo token chat-id {} image))
  ([token chat-id options image]
   (assert-file-type image ["jpg" "jpeg" "gif" "png" "tif" "bmp"])
   (send-file token chat-id options
              image "/sendPhoto" "photo" "photo.png")))


(defn send-document
  "Sends a document to the chat."
  ([token chat-id document]
   (send-document token chat-id {} document))
  ([token chat-id options document]
   (send-file token chat-id options
              document "/sendDocument" "document" "document")))


(defn send-video
  "Sends a video to the chat."
  ([token chat-id video]
   (send-video token chat-id {} video))
  ([token chat-id options video]
   (assert-file-type video ["mp4"])
   (send-file token chat-id options
              video "/sendVideo" "video" "video.mp4")))


(defn send-audio
  "Sends an audio note to the chat."
  ([token chat-id audio]
   (send-audio token chat-id {} audio))
  ([token chat-id options audio]
   (assert-file-type audio ["mp3"])
   (send-file token chat-id options
              audio "/sendAudio" "audio" "audio.mp3")))


(defn send-sticker
  "Sends a sticker to the chat."
  ([token chat-id sticker]
   (send-sticker token chat-id {} sticker))
  ([token chat-id options sticker]
   (assert-file-type sticker ["webp"])
   (send-file token chat-id options
              sticker "/sendSticker" "sticker" "sticker.webp")))


(defn answer-inline
  "Sends an answer to the inline query."
  ([token inline-query-id results]
   (answer-inline token inline-query-id {} results))
  ([token inline-query-id options results]
   (let [url  (str base-url token "/answerInlineQuery")
         body (into {:inline_query_id inline-query-id
                     :results         results}
                    options)
         resp (http/post url {:content-type :json
                              :form-params  body
                              :as           :json})]
     (-> resp :body))))


(defn answer-callback
  "Sends an answer to the callback query."
  ([token callback-query-id]
   (answer-callback token callback-query-id "" false))
  ([token callback-query-id text]
   (answer-callback token callback-query-id text false))
  ([token callback-query-id text show-alert]
   (let [url  (str base-url token "/answerCallbackQuery")
         body {:callback_query_id callback-query-id
               :text              text
               :show_alert        show-alert}
         resp (http/post url {:content-type :json
                              :form-params  body
                              :as           :json})]
     (-> resp :body))))
