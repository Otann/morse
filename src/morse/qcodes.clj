(ns morse.qcodes
  (:require [morse.api :as api]))

(defn req->morse [handler]
  (comp handler api/extract-data))

(defn direct-reply [token handler]
  (fn [{{{chatid :id} :chat} :message :as req}]
    (when-let [reply (handler req)]
      (if (string? reply)
        (with-meta
          {:status 200}
          {:direct-reply (api/send-text token chatid reply)})
        {:status 200}))))
