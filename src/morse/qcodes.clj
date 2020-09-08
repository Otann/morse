(ns morse.qcodes
  (:require [morse.api :as api]))

(defn- only [pred val]
  (when (pred val)
    val))

(defn req->morse [handler]
  (comp handler api/extract-data))

(defn direct-reply [token handler]
  (fn [{{{chatid :id} :chat} :message :as req}]
    (when-let [reply (only string? (handler req))]
      (api/send-text token chatid reply))
    {:status 200}))
