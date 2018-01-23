(ns morse.polling
  "Declares long-polling routines to communicate with Telegram Bot API"
  (:require [clojure.tools.logging :as log]
            [clojure.core.async :as a
             :refer [chan go go-loop thread
                     >!! >! <! close! alts!]]
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
        updates-caller (or (:get-updates-fn opts) api/get-updates)
        timeout (or (:timeout opts) 1000)]
    (go-loop [offset 0]
      (let [response (a/chan)
            ;; in clj-http async handlers the context is
            ;; outside of go-loop - that's why using >!! here
            _ (updates-caller
               token
               #(>!! response %)
               (fn [exception]
                 (log/errorf
                  "Got exception while calling Telegram API: %s"
                  (.getMessage exception))
                 (close! running))
               (merge opts {:offset offset}))
            wait-timeout (a/timeout (or (:wait-timeout opts) (* timeout 5)))
            [data _] (a/alts! [running response wait-timeout])]
        (case data
          nil (do
                (log/info "Stopping Telegram polling...")
                (close! wait-timeout)
                (close! running)
                (close! updates))

          (do
            (close! wait-timeout)
            (doseq [upd data] (>! updates upd))
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
