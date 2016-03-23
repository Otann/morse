# morse [![Circle CI](https://circleci.com/gh/Otann/morse.svg?style=shield&no-cache=1)](https://circleci.com/gh/Otann/morse)

<img width="30%"
     align="right" padding="5px"
     alt=":)"
     src="http://otann.com/media/projects/morse/signature.gif"/> 

`morse` is a client for [Telegram](https://telegram.org) [Bot API](https://core.telegram.org/bots/api) for the [Clojure](http://clojure.org) programming language.

[![Clojars Project](http://clojars.org/morse/latest-version.svg?&no-cache=1)](https://clojars.org/morse)

## Installation

Add `[morse "0.0.1"]` to the dependency section in your project.clj file.

Import a namespace:

```clojure
(require '[telegram.core :as telegram])
```

Then somewhere, where you start your application, initialize API:
 
```clojure
(telegram/init! {:token "YOUR TELEGRAM TOKEN HERE"})
```

## Update Handlers

To process updates, you should provide your handlers to the library.

Handler is a function that receives [Update](https://core.telegram.org/bots/api#update) 
object from Telegram as a Clojure map:
 
```clojure
(defn handler [update]
  (when-let [message (:message update)]
    (api/send-message (-> update :message :chat :id)
                      (str "Hi there! 😊"))))
```                    

There two ways of providing your handlers to the API:

### Provide handlers in the initialization:

```clojure
(telegram/init! {:token (cfg/get :telegram-token)
                 :handlers [handler]
                 :polling true})
```
                 
### Or set them later, using a function
 
```clojure
(require '[morse.handlers :as h]

(h/add-handler! handler)
``` 

you can also replace all existing handlers with provided vector:

```clojure
(h/reset-handlers! [handler])
```
 
## Receiving updates 
 
You can use two methods of receiving updates from Telegram:

### Long Polling

Pass `:polling true` to the `init!` function:

```clojure
(telegram/init! {:token "YOUR TELEGRAM TOKEN HERE"
                 :polling true})
```

This will start a separate thread that will listen to updates from API.

### Webhook

If you already have a web-server, or don't want to consume extra or network, 
you can set a webhook, which Telegram will call for sending updates. 

```clojure
(telegram/init! {:token "YOUR TELEGRAM TOKEN HERE"
                 :webhook "http://example.com/telegram-hook"})
```

## Sending messages

Use `morse.api` to interact with Telegram chats:

```clojure
(require '[morse.api :as api])
```

Following methods from the API are implemented at the moment:

### [`sendMessage`](https://core.telegram.org/bots/api#sendmessage)

```clojure
(api/send-text chat-id "Hello, fellows")
```

You can use advanced options:

```clojure
(api/send-text chat-id
               {:parse_mode "Markdown"}
               "**Hello**, fellows")
```

### [`sendPhoto`](https://core.telegram.org/bots/api#sendphoto)

File, ByteArray and InputStream are supported as images for that function:
 
```clojure
(require '[clojure.java.io :as io])
(api/send-photo chat-id (io/file (io/resource "photo.png")))
```

You can use advanced options:

```clojure
(api/send-photo chat-id
                {:caption "Here is a map:"}
                (io/file (io/resource "map.png")))
```
 

## License

Copyright © 2016 Anton Chebotaev

Distributed under the Eclipse Public License either version 1.0.
