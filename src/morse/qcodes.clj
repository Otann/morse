(ns morse.qcodes
  (:require [compojure.core :refer [make-route]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [morse.api :as api]))

(defn- only [pred val]
  (when (pred val)
    val))

(defn req->morse [url-path handler]
  (make-route :post url-path
              (wrap-json-response
               (wrap-json-body
                (comp handler :body)
                {:keywords? true :bigdecimals? true}))))

(defn direct-reply [token handler]
  (fn [{{{chatid :id} :chat} :message :as req}]
    (when-let [reply (only string? (handler req))]
      (api/send-text token chatid reply))
    {:status 200}))
