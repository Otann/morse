(ns morse.polling
  "Declares long-polling routines to communicate with Telegram Bot API"
  (:require [clojure.tools.logging :as log]
            [clojure.core.async :refer [chan go go-loop thread
                                        >! <! close! alts!]]
            [morse.api :as api]))


(defn new-offset
  "Returns new offset for Telegram updates"
  [updates default]
  (if (seq updates)
    (-> updates last :update_id inc)
    default))


(defn create-producer
  "Passed channel should be always empty.
   Close it to stop long-polling.
   Returns channel with updates from Telegram"
  [running token]
  (let [updates (chan)]
    (go-loop [offset 0]
      (let [resopnse (thread (api/get-updates token {:offset offset}))
            [data _] (alts! [running resopnse])]
        (if-not data
          (close! updates)
          (do (doseq [upd data] (>! updates upd))
              (recur (new-offset data offset))))))
    updates))


(defn create-consumer
  "Creates consumer from given handler function
   and channel wuth updates.

   Start infinite loop inside go-routine
   that will pull messages from channel.

   Will be stopped when channel is closed."
  [updates handler]
  (go-loop []
    (when-let [data (<! updates)]
      (try
        (handler data)
        (catch Exception e
          (log/error e "Unable to handle update" data)))
      (recur))))


(defn start
  "Starts long-polling process"
  [token handler]
  (let [running (chan)
        updates (create-producer running token)]
    (create-consumer updates handler)
    running))


(defn stop
  "Stops everything"
  [running]
  (close! running))
