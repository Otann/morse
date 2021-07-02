(ns morse.handlers-test
  (:require [clojure.test :refer :all]

            [morse.handlers :as m-h]))

;; aux fns

(defn to-command
  [cmd-name]
  {:text (str "/" cmd-name)
   :chat {:id "bar"}})


(defn to-inline-query
  [query-str]
  {:id     0
   :from   {:user_id 0}
   :query  query-str
   :offset 0})

(defn to-callback-query
  [query-str]
  {:id            0
   :from          {:user_id 0}
   :message       query-str
   :chat_instance 123
   :data          "test"})


;; test cases

(deftest commands
  (let [start-handler    (m-h/command "start" msg msg)
        start-handler-fn (m-h/command-fn "start" (fn [msg] msg))
        start-command    (to-command "start")]
    (is (= start-command
           (start-handler {:message start-command})))
    (is (= start-command
           (start-handler-fn {:message start-command})))

    (is (= nil
           (start-handler {:message (to-command "help")})))
    (is (= nil
           (start-handler-fn {:message (to-command "help")})))

    (is (= nil
           (start-handler {:message (to-command "st")})))
    (is (= nil
           (start-handler-fn {:message (to-command "st")})))

    (is (= nil
           (start-handler {:inline (to-inline-query "Kitten")})))
    (is (= nil
           (start-handler-fn {:inline (to-inline-query "Kitten")})))))


(deftest inlines
  (let [inline-handler    (m-h/inline msg msg)
        inline-handler-fn (m-h/inline-fn (fn [msg] msg))
        query             (to-inline-query "cats")]
    (is (= query
           (inline-handler {:inline_query query})))
    (is (= query
           (inline-handler-fn {:inline_query query})))

    (is (= nil
           (inline-handler {:message (to-command "help")})))
    (is (= nil
           (inline-handler-fn {:message (to-command "help")})))))


(deftest callbacks
  (let [callback-handler (m-h/callback msg msg)
        query (to-callback-query "31337")]
    (is (= (callback-handler {:callback_query query})
           query))
    (is (= (callback-handler {:message (to-command "help")})
           nil))))
