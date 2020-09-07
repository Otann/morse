(defproject morse "0.4.3"
  :description "Telegram Bot API"

  :url "https://github.com/otann/morse/"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.macro "0.1.5"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/tools.logging "0.4.1"]
                 [org.clojure/spec.alpha "0.1.143"]
                 [clj-stacktrace "0.2.8"]
                 [cheshire "5.8.1"]
                 [clj-http "3.9.1"]]

  :plugins [[lein-cloverage "1.0.10"]]

  :profiles {:uberjar {:aot :all}
             :test    {:dependencies [[clj-http-fake "1.0.3"]]
                       :plugins      [[pjstadig/humane-test-output "0.8.2"]
                                      [com.jakemccrary/lein-test-refresh "0.14.0"]
                                      [com.taoensso/timbre "4.1.4"]]}}

  ;; Artifact deployment info
  :scm {:name "git"
        :url  "https://github.com/otann/morse"}

  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "patch"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy" "clojars"]]

  :pom-addition [:developers [:developer
                              [:name "Anton Chebotaev"]
                              [:url "http://otann.com"]
                              [:email "anton.chebotaev@gmail.com"]
                              [:timezone "+1"]]])
