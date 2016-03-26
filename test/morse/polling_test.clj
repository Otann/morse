(ns morse.polling-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer [chan go >! <!!]]
            [morse.handlers :as h]
            [morse.polling :as polling]
            [morse.test-utils :as u]))

(defn update-with-text [text]
  {:update_id 0
   :message {:text text
             :chat {:id "bar"}}})

(defn handler-for [channel]
  (fn [update] (go (>! channel update))))

(deftest handler-receives-update
  (let [update   (update-with-text "foo")
        received (chan)
        handler  (handler-for received)]
    (u/with-faked-updates [update]
      (h/reset-handlers! [handler])
      (polling/start!)
      (is (= update (<!! received)))
      #_(polling/stop!))))
