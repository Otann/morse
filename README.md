# morse

[![Circle CI](https://circleci.com/gh/Otann/morse.svg?style=shield&no-cache=2)](https://circleci.com/gh/Otann/morse)

<img width="30%"
     align="right" padding="5px"
     alt=":)"
     src="http://otann.com/media/projects/morse/signature.gif"/> 

`morse` is a client for [Telegram](https://telegram.org) [Bot API](https://core.telegram.org/bots/api) for the [Clojure](http://clojure.org) programming language.

[![Clojars Project](http://clojars.org/morse/latest-version.svg?&no-cache=2)](https://clojars.org/morse)

## Installation

Add `[morse "0.1.0"]` to the dependency section in your project.clj file.

## Update Handlers

Handler is a function that receives [Update](https://core.telegram.org/bots/api#update)
object from Telegram as a Clojure map. Morse provides some helpers for you:
 
```clojure
(require '[morse.handlers :refer :all])

(defhandler bot-api
  (command "start" {user :user} (println "User" user "joined"))
  (command "chroma" message (handle-text message))

  (mesage message (println "Intercepted message:" message)))
```

There are two possible helpers for messages:

    (command <command-name> <binding> <body>)
    (message <binding> <body>)

Where binding is same as you use anywhere in Clojure and will be applied to
[Message](https://core.telegram.org/bots/api#message) object.


### Starting bot

As Telegram documentation says, there are two ways of getting updates
from the bot: webhook and long-polling.

#### Webhook

If you develop a web application, you can use api call to
[register](https://core.telegram.org/bots/api#setwebhook) one of your endpoints in Telegram:

```clojure
(require '[morse.api :as api])

(api/set-webhook "abc:XXX" "http://example.com/handler")
```

Telegram will use this url to `POST` messages to it.
You can also use handler to react on these messages.
Here is quick example if you use [`compojure`](https://github.com/weavejester/compojure):

```clojure
(defhandler bot-api
  (command "help" {{id :chat_id} :message}
    (api/send-message token id "Help is on the way")))

(defroutes app-routes
  (POST "/handler" {body :body} (bot-api handler))
  (route/not-found "Not Found"))
```

#### Long-polling

This solution works perfectly if you don't plan on having a webserver
or want to test your bot from a local machine.

Start the process by simply calling `start` function and pass it token and your handler:

```clojure
(require '[morse.polling :as p])

(def channel
  (p/start token handler))
```

Then if you want to stop created background processes, call stop on returned channel:

```clojure
(p/stop channel)
```


## Sending messages

Use `morse.api` to interact with Telegram chats:

```clojure
(require '[morse.api :as api])
```

Following methods from the API are implemented at the moment:

### [`sendMessage`](https://core.telegram.org/bots/api#sendmessage)

```clojure
(api/send-text token chat-id "Hello, fellows")
```

You can use advanced options:

```clojure
(api/send-text token chat-id
               {:parse_mode "Markdown"}
               "**Hello**, fellows")
```

### [`sendPhoto`](https://core.telegram.org/bots/api#sendphoto)

File, ByteArray and InputStream are supported as images for that function:
 
```clojure
(require '[clojure.java.io :as io])

(api/send-photo token chat-id
                (io/file (io/resource "photo.png")))
```

You can use advanced options:

```clojure
(api/send-photo token chat-id
                {:caption "Here is a map:"}
                (io/file (io/resource "map.png")))
```
 

## License

Copyright © 2016 Anton Chebotaev

Distributed under the Eclipse Public License either version 1.0.
