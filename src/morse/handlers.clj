(ns morse.handlers
  "Provides convenient means for defining the Telegram Bot API update handlers."
  (:require [clojure.string :as s]
            [clojure.tools.macro :as macro]))

;; impl details

(defn- handling-one-by-one
  "Applies each handler in a sequence of 'handlers' one-by-one to
   the 'update' object until one of them returns a non-nil result."
  [update & handlers]
  (some #(% update) handlers))

(defn- combine-sequentially
  "Returns a fn that sequentially applies 'handlers' to an update."
  [& handlers]
  (fn [update]
    (apply handling-one-by-one update handlers)))


;; public fns - defining handlers

(defmacro defhandler
  "Creates an update handler function by combining several 'handlers' into one.
   The 'name' may optionally be followed by a doc-string and metadata map."
  {:arglists '([name docstring? attr-map? & handlers])}
  [name & handlers]
  (let [[name handlers] (macro/name-with-attributes name handlers)]
    `(def ~name (combine-sequentially ~@handlers))))

(defn update-fn
  "Produces an update handler function that calls the passed 'handler-fn' with
   an update data at the specified 'path', if any, and does nothing otherwise.

   E.g. `(update-fn [:message] handler-fn)` will process updates with Message."
  [path handler-fn]
  (fn [update]
    (if-let [data (get-in update path)]
      (handler-fn data))))


;; public fns - commands

(defn command?
  "Checks if the 'update' message contains a command with the specified 'name'.
   \"/stars\" and \"/st\" are considered different."
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
  "Creates an update handler for the bot command with the specified 'name',
   using the passed 'handler-fn' w/ the update's 'message' as an argument."
  [name handler-fn]
  (fn [update]
    (if (command? update name)
      (handler-fn (:message update)))))

(defmacro command
  "Creates an update handler for the bot command with the specified 'name',
   using the message handler fn in a form of (fn ['bindings'] 'body')."
  [name bindings & body]
  `(command-fn ~name (fn [~bindings] ~@body)))


;; public fns - messages

(defn message-fn
  [handler-fn]
  (update-fn [:message] handler-fn))

(defmacro message
  [bindings & body]
  `(message-fn (fn [~bindings] ~@body)))


;; public fns - inline queries

(defn inline-fn
  [handler-fn]
  (update-fn [:inline_query] handler-fn))

(defmacro inline
  [bindings & body]
  `(inline-fn (fn [~bindings] ~@body)))


;; public fns - callback queries

(defn callback-fn
  [handler-fn]
  (update-fn [:callback_query] handler-fn))

(defmacro callback
  [bindings & body]
  `(callback-fn (fn [~bindings] ~@body)))


;; examples

(comment
  "Use helper functions or macros to define simple handlers"
  ; create a handler that will react on "/start" command
  (command-fn "start" (fn [{user :from}] (println "Detected user:" user)))

  ; ... there is a macro for a shorthand usage
  (command "start" {user :from} (println "Detected user:" user))

  ; ... which you can use without a destructuring syntax
  (command "start" message (handle-text message)))

(comment
  "Compose a handler from several ones"
  (defhandler handler
    (command "start" [{{id :id :as chat} :chat}]
             (println "Bot joined a new chat:" chat)
             (morse.api/send-text token id "Welcome!"))

    (command "help" {{id :id :as chat} :chat}
             (println "Help was requested in chat:" chat)
             (morse.api/send-text token id "Help is on the way!"))

    (message {{id :id} :chat :as message}
             (println "Intercepted message:" message)
             (morse.api/send-text token id "I don't do a whole lot... yet."))))

(comment
  "Then, run this in your REPL to start receiving updates"
  (morse.updates.polling/start token handler))
