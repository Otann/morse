(ns morse.core
  (:require [taoensso.timbre :as log]
            [morse.api :as api]
            [morse.handlers :as h]
            [morse.polling :as polling]))


(defn init!
  "Initializes Telegram cliend and starts all necessary routines"
  [{:keys [token handlers polling webhook]}]

  (if token
    (reset! api/token token)
    (throw (Exception. "Can't intialize Telegram without a token")))

  (if (seq handlers)
    (h/reset-handlers! handlers)
    (log/warn "You initialized Telegram API without any handlers"))

  (cond
    polling (polling/start!)
    webhook (api/set-webhook webhook)
    :default (log/warn "You haven't used any of the update-listening options. "
                       "Consider passing :polling or :webhook to init! function.")))
