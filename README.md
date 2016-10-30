# morse

[![Circle CI](https://circleci.com/gh/Otann/morse.svg?style=shield&no-cache=2)](https://circleci.com/gh/Otann/morse)

<img width="30%"
     align="right" padding="5px"
     alt=":)"
     src="http://otann.github.io/media/projects/morse/signature.gif"/> 

`morse` is a client for [Telegram](https://telegram.org) [Bot API](https://core.telegram.org/bots/api) for the [Clojure](http://clojure.org) programming language.

[![Clojars Project](http://clojars.org/morse/latest-version.svg?&no-cache=4)](https://clojars.org/morse)

## Installation

Add `[morse "0.2.1"]` to the dependency section in your project.clj file.

## Update Handlers

Handler is a function that receives [Update](https://core.telegram.org/bots/api#update)
object from Telegram as a Clojure map. Morse provides some helpers for you:
 
```clojure
(require '[morse.handlers :refer :all])

(defhandler bot-api
  (command "start" {user :user} (println "User" user "joined"))
  (command "chroma" message (handle-text message))

  (message message (println "Intercepted message:" message)))
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
  (command "help" {{id :id} :chat}
    (api/send-text token id "Help is on the way")))

(defroutes app-routes
  (POST "/handler" {{updates :result} :body} (map bot-api updates))
  (route/not-found "Not Found"))
```

#### Long-polling

This solution works perfectly if you don't plan on having a webserver
or want to test your bot from a local machine.

Start the process by simply calling `start` function and pass it token and your handler:

```clojure
(require '[morse.polling :as p])

(def channel (p/start token handler))
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

Following methods from the API are implemented at the moment. All of them may use the advanced options by providing an additional option map argument. For all functions sending files File, ByteArray and InputStream are supported as arguments.

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

This sends a photo that will be displayed using the embedded image viewer where available.
 
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
 
### [`sendVideo`](https://core.telegram.org/bots/api#sendvideo)

Sends the given mp4 file as a video to the chat which will be shown using the embedded player where available.

```clojure
(api/send-video token chat-id
                (io/file (io/resource "video.mp4")))
```


### [`sendAudio`](https://core.telegram.org/bots/api#sendaudio)

Sends the given mp3 file as an audio message to the chat.

```clojure
(api/send-audio token chat-id
                (io/file (io/resource "audio.mp3")))
```

### [`sendSticker`](https://core.telegram.org/bots/api#sendsticker)

Sends the given WebP image as a sticker to the chat.

```clojure
(api/send-sticker token chat-id
                  (io/file (io/resource "sticker.webp")))
```

### [`sendDocument`](https://core.telegram.org/bots/api#senddocument)

This method can be used for any other kind of file not supported by the other methods, or if you don't want telegram to make a special handling of your file (i.e. sending music as a voice message).

```clojure
(api/send-document token chat-id
                   (io/file (io/resource "document.pdf")))
```

## License

Copyright © 2016 Anton Chebotaev

Distributed under the Eclipse Public License either version 1.0.
