(ns morse.handlers)

; TODO: ENLARGE API
(comment

  (defhandler handler
              (COMMAND "start" [args]
                       (api/send-message "You executed command"))

              (PHOTO [photo]
                     (api/send-message "Your picture was received"))))
