(ns morse.polling
  "Declares long-polling routines to communicate with Telegram Bot API"
  (:require [clojure.tools.logging :as log]
            [clojure.core.async :as a
             :refer [chan go go-loop thread
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
  [running token opts]
  (let [updates (a/chan)
        timeout (or (:timeout opts) 1000)]
    (go-loop [offset 0]
      (let [response (a/thread (api/get-updates token (merge opts {:offset offset})))
            [data _] (a/alts! [running response])]
        (case data
          nil
          (do (close! running)
              (close! updates))

          ::api/error
          (do (log/warn "Got error from Telegram API, retrying in" timeout "ms")
              (<! (a/timeout timeout))
              (recur offset))

          (do (doseq [upd data] (>! updates upd))
              (recur (new-offset data offset))))))
    updates))


(defn create-consumer
  "Creates consumer from given handler function
   and channel with updates.

   Start infinite loop inside go-routine
   that will pull messages from channel.

   Will be stopped when channel is closed."
  [updates handler]
  (go-loop []
    (when-let [data (<! updates)]
      (try
        (handler data)
        (catch Throwable t
          (log/error t "Unable to handle update" data)))
      (recur))))


(defn start
  "Starts long-polling process.
  Handler is supposed to process immediately, as it will
  be called in a blocking manner."
  ([token handler] (start token handler {}))
  ([token handler opts]
   (let [running (chan)
         updates (create-producer running token opts)]
     (create-consumer updates handler)
     running)))


(defn stop
  "Stops everything"
  [running]
  (close! running))
