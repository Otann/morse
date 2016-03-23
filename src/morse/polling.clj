(ns morse.polling
  "Declares long-polling routines to communicate with Telegram Bot API"
  (:require [taoensso.timbre :as log]
            [clojure.core.async :refer [>!! <! go chan close! thread]]

            [morse.api :as api]
            [morse.handlers :refer [handle]]))

;; this holds updates from Telegram
(def updates (atom nil))

;; this controls if loops are rolling
(def running (atom false))

(defn start!
  "Starts long-polling process"
  []
  (log/debug "Trying to start polling threads")
  (reset! updates (chan))
  (reset! running true)

  ;; Start infinite loop inside go-routine
  ;; that will pull messages from channel
  (go (loop []
        (handle (<! @updates))
        (if @running (recur))))

  ;; Start thread with polling process
  ;; that will populate channel
  (thread (loop [offset 0]
            (let [updates-data (api/get-updates {:offset offset})
                  new-offset (if (empty? updates-data)
                               offset
                               (-> updates-data last :update_id inc))]
              (doseq [update updates-data] (>!! @updates update))
              (if @running (recur new-offset)))))

  (log/info "Started long-polling for Telegram updates"))

(defn stop!
  "Stops everything"
  []
  (reset! running false)
  (close! @updates))
