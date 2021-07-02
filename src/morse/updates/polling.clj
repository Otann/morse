(ns morse.updates.polling
  "Declares long-polling routines for communication with Telegram Bot API"
  (:require [clojure.core.async :as a :refer [go go-loop >! <!]]
            [clojure.core.async.impl.protocols :refer [closed?]]
            [clojure.tools.logging :as log]

            [morse.updates.api :as m-u-api])
  (:import [java.util.concurrent TimeUnit]))

;; impl details

(defonce ^:private *running-channel (atom nil))

(def ^:private default-options {:timeout 1})

(defn- get-new-offset
  "Returns a new offset which identifies the first update to be returned."
  [updates default]
  (if (seq updates)
    (-> updates last :update_id inc)
    default))

(defn- create-producer
  "Creates an incoming updates producer with the specified 'token' & 'options'.
   The passed 'running' channel have to stay empty and gets closed to stop the
   long-polling process.
   Returns a channel with the received update objects."
  [running token options]
  (let [updates    (a/chan)
        ;; NB: Fix for JDK bug https://bugs.openjdk.java.net/browse/JDK-8075484
        ;;     Introduce an additional timeout 10 times longer than for Bot API.
        timeout-ms (->> (* 10 (:timeout options))
                        (.toMillis (TimeUnit/SECONDS)))]
    (go-loop [offset 0]
      (let [wait-timeout (go (<! (a/timeout timeout-ms))
                             ::wait-timeout)
            curr-options (assoc options :offset offset)
            response     (m-u-api/get-updates token curr-options)
            [data _] (a/alts! [running response wait-timeout])]
        (case data
          nil ;; 'running' channel got closed by the user
          (do (a/close! wait-timeout)
              (a/close! updates))

          ::wait-timeout
          (do (log/error "HTTP request timed out, stopping polling")
              (a/close! running)
              (a/close! updates))

          ::m-u-api/error
          (do (log/warn "Got error from Telegram API, stopping polling")
              (a/close! running)
              (a/close! updates))

          (do (a/close! wait-timeout)
              (doseq [upd data] (>! updates upd))
              (recur (get-new-offset data offset))))))
    updates))

(defn- create-consumer
  "Creates an incoming updates consumer from the given 'handler' function and
   'updates' channel.

   IMPLEMENTATION NOTE:
   Starts an infinite loop inside a go-routine that will pull messages from a
   channel. Will stop when the channel is closed."
  [updates handler]
  (go-loop []
    (when-let [data (<! updates)]
      (try
        (handler data)
        (catch Throwable t
          (log/error t "Unable to handle update" data)))
      (recur))))


;; public fns

(defn start!
  "Starts the updates long-polling process with the given updates 'handler'
   fn and the 'options' map for updates retrieval tuning.

   IMPORTANT: The passed 'handler' fn will be called in a blocking manner,
              so it must be non-blocking itself, i.e. return immediately."
  ([token handler]
   (start! token handler nil))
  ([token handler options]
   (log/info "Starting Telegram polling...")
   (let [running (reset! *running-channel (a/chan))
         options (merge default-options options)
         updates (create-producer running token options)]
     (create-consumer updates handler)
     nil)))

(defn stop!
  "Stops the updates long-polling process at the initiative of the user."
  []
  (log/info "Stopping Telegram polling...")
  (a/close! @*running-channel))

(defn has-stopped?
  "Finds out if the updates long-polling process has stopped."
  []
  (let [run-chan @*running-channel]
    (or (nil? run-chan)
        (closed? run-chan))))
