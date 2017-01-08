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


(defmacro command
  "Generate command handler"
  [name bindings & body]
  `(fn [update#]
     (if (command? update# ~name)
       (let [~bindings (:message update#)] ~@body))))


(defn compile-handler
  "Compile handler from subpath in update"
  [subpath binding body]
  `(fn [update#]
     (if-let [data# (get update# ~subpath)]
       (let [~binding data#] ~@body))))


(defmacro message
  "Generate command handler"
  [bindings & body]
  (compile-handler :message bindings body))


(defmacro inline
  "Generate command handler"
  [bindings & body]
  (compile-handler :inline_query bindings body))

(defmacro callback
  "Generate callback query handler"
  [bindings & body]
  (compile-handler :callback_query bindings body))

(comment "Examples of how to use handler definitions"

  (defhandler bot-api
    (command "start" {user :user} (println "User" user "joined"))
    (command "chroma" message (handle-text message))

    (message message (println "Intercepted message:" message))

    (inline {user :from q :query} (println "User" user "made inline query" q)))

  (polling/start token bot-api))
