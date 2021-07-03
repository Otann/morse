(ns morse.updates.api
  "The Telegram Bot API methods for getting updates"
  (:require [clojure.core.async :as a]
            [clojure.tools.logging :as log]

            [cheshire.core :as json]
            [clj-http.client :as http]

            [morse.api :as api]))

(defn get-updates
  "Receives incoming updates from the Telegram Bot API via long-polling.
   Returns a one-off channel with an invocation result, which is either
   an Update object in case of success or an `:api/error` otherwise."
  [token {:keys [timeout offset limit]
          :or   {timeout 1 offset 0 limit 100}}]
  (let [url        (str api/base-url token "/getUpdates")
        request    {:query-params {:timeout timeout
                                   :offset  offset
                                   :limit   limit}
                    :async?       true}
        result     (a/chan)
        on-success (fn [resp]
                     (if-let [data (-> resp :body
                                       (json/parse-string true)
                                       :result)]
                       (a/put! result data)
                       (a/put! result ::error))
                     (a/close! result))
        on-failure (fn [err]
                     (log/debug err "Failed to get updates from Telegram")
                     (a/put! result ::error)
                     (a/close! result))]
    (http/get url request on-success on-failure)
    result))

(defn set-webhook
  "Specifies a URL to receive incoming updates via an outgoing webhook."
  [token webhook-url]
  (let [url   (str api/base-url token "/setWebhook")
        query {:url webhook-url}]
    (http/get url {:as :json :query-params query})))
