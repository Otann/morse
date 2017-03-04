(ns morse.handlers
  [:require [clojure.string :as s]
            [clojure.tools.macro :as macro]])


(defn handling
  "Apply list of handlers to Telegram update"
  [request & handlers]
  (some #(% request) handlers))


(defn handlers
  "Create handler by combining several into one"
  [& handlers]
  #(apply handling % handlers))


(defmacro defhandler
  "Define a Telegram handler function from a sequence of handlers.
  The name may optionally be followed by a doc-string and metadata map."
  [name & routes]
  (let [[name routes] (macro/name-with-attributes name routes)]
    `(def ~name (handlers ~@routes))))


(defn command?
  "Checks if message is a command with a name.
  /stars and /st are considered different."
  [update name]
  (some-> update
          :message
          :text
          (s/split #"\s+")
          (first)
          (s/split #"@")
          (first)
          (= (str "/" name))))


(defn command-fn
  "Generate command handler from an update function"
  [name handler]
  (fn [update]
    (if (command? update name)
      (handler (:message update)))))


(defmacro command
  "Generate command handler"
  [name bindings & body]
  `(command-fn ~name (fn [~bindings] ~@body)))


(defn update-fn [path handler-fn]
  (fn [update]
    (let [data (get-in update path)]
      (handler-fn data))))

(defn message-fn [handler-fn]
  (update-fn [:message] handler-fn))

(defmacro message
  [bindings & body]
  `(message-fn (fn [~bindings] ~@body)))


(defn inline-fn [handler-fn]
  (update-fn [:inline_query] handler-fn))

(defmacro inline
  [bindings & body]
  `(inline-fn (fn [~bindings] ~@body)))


(defn callback-fn [handler-fn]
  (update-fn [:callback_query] handler-fn))

(defmacro callback
  [bindings & body]
  `(callback-fn (fn [~bindings] ~@body)))

(comment "Examples of how to use handler definitions"

  ; creates a handler that will react on "/start" command
  (command-fn "start" (fn [{user :from}] (println "Detecte user:" user)))

  ; There is a macro for a shorthand usage
  (command "start" {user :from} (println "Detecte user:" user))

  ; Which you can use without desctructuring syntax
  (command "chroma" message (handle-text message))


  (defhandler handler
    (handler-fn "start"
                (fn [{{id :id :as chat} :chat}]
                  (println "Bot joined new chat: " chat)))

    (command "start" [{{id :id :as chat} :chat}]
      (println "Bot joined new chat: " chat)
      (t/send-text token id "Welcome!"))

    (command "help" {{id :id :as chat} :chat}
      (println "Help was requested in " chat)
      (t/send-text token id "Help is on the way"))

    (message {{id :id} :chat :as message}
      (println "Intercepted message: " message)
      (t/send-text token id "I don't do a whole lot ... yet.")))

  ; then run in your repl:
  (polling/start token bot-api)

  )
