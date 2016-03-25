(defproject morse "0.0.1-SNAPSHOT"
  :description "Telegram Bot API"

  :url "https://github.com/otann/morse/"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.2.374"]
                 [com.taoensso/timbre "4.1.4"]
                 [cheshire "5.5.0"]
                 [clj-http "2.1.0"]]

  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[expectations "2.1.4"]
                                  [clj-http-fake "1.0.2"]]
                   :plugins [[expectations "2.0.9"][com.jakemccrary/lein-test-refresh "0.14.0"]
                             [venantius/ultra "0.4.1"]]}}

  ;; Artifact deployment info
  :scm {:name "git"
        :url "https://github.com/otann/morse"}

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :pom-addition [:developers [:developer
                              [:name "Anton Chebotaev"]
                              [:url "http://otann.com"]
                              [:email "anton.chebotaev@gmail.com"]
                              [:timezone "+1"]]])
