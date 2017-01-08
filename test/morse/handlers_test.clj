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

  (let [start-handler (h/command "start" msg msg)
        start-command (command-message "start")]

    (is (= (start-handler {:message start-command})
           start-command))

    (is (= (start-handler {:message (command-message "help")})
           nil))

    (is (= (start-handler {:message (command-message "st")})
           nil))

    (is (= (start-handler {:inline (inline-query "Kitten")})
           nil))))

(deftest inlines

  (let [inline-handler (h/inline msg msg)
        query (inline-query "facepalm")]

    (is (= (inline-handler {:inline_query query})
           query))

    (is (= (inline-handler {:message (command-message "help")})
           nil))))

(deftest callbacks
  (let [callback-handler (h/callback msg msg)
        query (callback-query "31337")]
    (is (= (callback-handler {:callback_query query})
           query))
    (is (= (callback-handler {:message (command-message "help")})
           nil))))
