(ns morse.handlers
  (:require [taoensso.timbre :as log]))

(def ^:private handlers (atom []))

(defn add-handler! [handler]
  (swap! handlers #(conj % handler)))

(defn reset-handlers!
  ([] (reset-handlers! []))
  ([value] (reset! handlers value)))

(defn handle [update]
  (if (empty? @handlers)
    (log/warn "There were no handlers to process update from Telegram")
    (doseq [handler @handlers]
      (try
        (handler update)
        (catch Exception e
          (log/error e "Got error in one of the handlers:"))))))

; TODO: ENLARGE API
(comment
  (make-handlers {:start (fn [text] #_( other shit ))
                  :help  (fn [text] #_( other shit ))
                  nil    (fn [text] #_( plain text message ))   }))
