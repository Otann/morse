(ns morse.api
  (:require [clj-http.client :as http]
            [clojure.string :as string]))

(def base-url "https://api.telegram.org/bot")

(defn get-updates
  "Receive updates from Bot via long-polling endpoint"
  [token {:keys [limit offset timeout]}]
  (let [url (str base-url token "/getUpdates")
        query {:timeout (or timeout 1)
               :offset  (or offset 0)
               :limit   (or limit 100)}
        resp (http/get url {:as :json :query-params query})]
    (-> resp :body :result)))

(defn set-webhook
  "Register WebHook to receive updates from chats"
  [token webhook-url]
  (let [url   (str base-url @token "/setWebhook")
        query {:url webhook-url}]
    (http/get url {:as :json :query-params query})))

(defn send-text
  "Sends message to the chat"
  ([token chat-id text] (send-text token chat-id {} text))
  ([token chat-id options text]
   (let [url (str base-url token "/sendMessage")
         query (into {:chat_id chat-id :text text} options)
         resp (http/get url {:as :json :query-params query})]
     (-> resp :body))))

(defn send-file 
  ([token chat-id file method field filename] 
   (send-file token chat-id {} file method field filename))
  ([token chat-id options file method field filename]
   (let [url (str base-url token method)
         base-form [{:part-name "chat_id" :content (str chat-id)}
                    {:part-name field :content file :name filename}]
         options-form (for [[key value] options]
                        {:part-name (name key) :content value})
         form (into base-form options-form)
         resp (http/post url {:as :json :multipart form})]
     (-> resp :body))))

(defmacro accepted-formats [file valid-extensions & body]
  "If file is an instance of java.io.File, it checks its extension is valid.
  Only files are checked, if the file argument is not an instance of File
  the check will be omitted." 
  `(if (or (not= (type ~file) java.io.File) 
           ~@(map (fn [extension] `(.endsWith (.getName ~file) ~extension)) valid-extensions))
     (do ~@body)
     (throw (ex-info (str "Telegram API only supports the following formats: " 
                          ~(string/join ", " valid-extensions) 
                          " for this method. Other formats may be sent with send-document") 
                     {}))))

(defn send-photo [token chat-id image]
  (accepted-formats image ["jpg" "jpeg" "gif" "png" "tif" "bmp"] 
    (send-file token chat-id image "/sendPhoto" "photo" "photo.png")))

(defn send-document [token chat-id document]
  (send-file token chat-id document "/sendDocument" "document" "document"))

(defn send-video [token chat-id video]
  (accepted-formats video ["mp4"] 
    (send-file token chat-id video "/sendVideo" "video" "video.mp4")))

(defn send-audio [token chat-id audio]
  (accepted-formats audio ["mp3"] 
    (send-file token chat-id audio "/sendAudio" "audio" "audio.mp3")))
