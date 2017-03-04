(ns morse.handlers-test
  (:require [clojure.test :refer :all]
            [morse.handlers :as h]))


(defn command-message [command]
  {:text (str "/" command)
   :chat {:id "bar"}})


(defn inline-query [query]
  {:id 0
   :from {:user_id 0}
   :query query
   :offset 0})

(defn callback-query [query]
  {:id 0
   :from {:user_id 0}
   :message query
   :chat_instance 123
   :data "test"})


(deftest commands

  (let [start-handler    (h/command "start" msg msg)
        start-handler-fn (h/command-fn "start" (fn [msg] msg))
        start-command    (command-message "start")]

    (is (= start-command
           (start-handler {:message start-command})))
    (is (= start-command
           (start-handler-fn {:message start-command})))

    (is (= nil
           (start-handler {:message (command-message "help")})))
    (is (= nil
           (start-handler-fn {:message (command-message "help")})))

    (is (= nil
           (start-handler {:message (command-message "st")})))
    (is (= nil
           (start-handler-fn {:message (command-message "st")})))

    (is (= nil
           (start-handler {:inline (inline-query "Kitten")})))
    (is (= nil
           (start-handler-fn {:inline (inline-query "Kitten")})))))

(deftest inlines

  (let [inline-handler    (h/inline msg msg)
        inline-handler-fn (h/inline-fn (fn [msg] msg))
        query             (inline-query "facepalm")]

    (is (= query
           (inline-handler {:inline_query query})))
    (is (= query
           (inline-handler-fn {:inline_query query})))

    (is (= nil
           (inline-handler {:message (command-message "help")})))
    (is (= nil
           (inline-handler-fn {:message (command-message "help")})))))

(deftest callbacks
  (let [callback-handler (h/callback msg msg)
        query (callback-query "31337")]
    (is (= (callback-handler {:callback_query query})
           query))
    (is (= (callback-handler {:message (command-message "help")})
           nil))))