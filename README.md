# Morse

[![Circle CI](https://circleci.com/gh/Otann/morse.svg?style=shield&no-cache=5)](https://circleci.com/gh/Otann/morse)
[![Clojars](https://img.shields.io/clojars/v/morse.svg)](https://clojars.org/morse)
[![codecov](https://codecov.io/gh/Otann/morse/branch/master/graph/badge.svg)](https://codecov.io/gh/Otann/morse)

<img width="30%"
     align="right" padding="5px"
     alt=":)"
     src="http://otann.github.io/media/projects/morse/signature.gif"/>

Morse is a client for [Telegram](https://telegram.org) [Bot API](https://core.telegram.org/bots/api) for the [Clojure](http://clojure.org) programming language.


## Installation

Add `[morse "0.4.0"]` to the dependency section in your project.clj file.

There is also a template which you can use to bootstrap your project:

    lein new morse my-project
    cd my-project
    export TELEGRAM_TOKEN=...
    lein run

## Detecting user's actions

Telegram sends updates about events in chats in form of
[Update](https://core.telegram.org/bots/api#update) objects.

Inside those there could be commands, inline queries and many more.
To help you with these Morse provides you helpers and some macros in
`morse.handlers` namespace.

If you are familiar with building web-service with Compojure,
you'll find similarities here:

```clojure
(ns user
  (:require [morse.handlers :as h]
            [morse.api :as t]))

(def token "YOUR-BIG-SECRET")          

; This will define bot-api function, which later could be
; used to start your bot
(h/defhandler bot-api
  ; Each bot has to handle /start and /help commands.
  ; This could be done in form of a function:
  (h/command-fn "start" (fn [{{id :id :as chat} :chat}]
                          (println "Bot joined new chat: " chat)
                          (t/send-text token id "Welcome!")))

  ; You can use short syntax for same purposes
  ; Destructuring works same way as in function above
  (h/command "help" {{id :id :as chat} :chat}
    (println "Help was requested in " chat)
    (t/send-text token id "Help is on the way"))

  ; Handlers will be applied until there are any of those
  ; returns non-nil result processing update.

  ; Note that sending stuff to the user returns non-nil
  ; response from Telegram API.     

  ; So match-all catch-through case would look something like this:
  (h/message message (println "Intercepted message:" message)))

```

### Messages

Receives [Message](https://core.telegram.org/bots/api#message) object as
first parameter in a function or target of binding:

```clojure
(command-fn "start" (fn [msg] (println "Received command: " msg)))
; or in a macro form
(command "start" msg (println "Received command: " msg))
```

If you wish to process messages that are not prefixed by a command,
there is also a helper:

```clojure
(message-fn (fn [msg] (println "Received message: " msg)))
; or in a macro form
(message "start" msg (println "Received message: " msg))
```

### Inline requests

There is also a helper to define handlers for [InlineQueries](https://core.telegram.org/bots/api#inlinequery)
in a similar form:

```clojure
(inline-fn (fn [inline] (println "Received inline: " inline)))
; or in a macro form
(inline inline (println "Received inline: " inline))
```

### Callbacks

You can provide handlers for [Callbacks](https://core.telegram.org/bots/api#answercallbackquery)
which are sent from [inline keyboards](https://core.telegram.org/bots#inline-keyboards-and-on-the-fly-updating)

```clojure
(callback-fn (fn [data] (println "Received callback: " inline)))
; or in a macro form
(callback data (println "Received callback: " inline))
```


## Starting your bot

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

### [`answerInlineQuery`](https://core.telegram.org/bots/api#answerinlinequery)

Sends an answer to an inline query.

```clojure
(api/answer-inline token inline-query-id options
                   [{:type "gif"
                     :id "gif1"
                     :gif_url "http://funnygifs/gif.gif"}])
```

### [`answerCallbackQuery`](https://core.telegram.org/bots/api#answercallbackquery)

Sends an answer to an callback query sent from inline keyboards.

```clojure
(api/answer-callback token
                     callback-query-id
                     text
                     show-alert)
```

## License

Copyright © 2017 Anton Chebotaev

Distributed under the Eclipse Public License either version 1.0.
