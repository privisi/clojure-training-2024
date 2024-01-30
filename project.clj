(defproject clojure-training-2024 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 ;; JDBC
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.xerial/sqlite-jdbc "3.30.1"]

                 [clj-time "0.15.2"]
                 [org.clojure/tools.logging "1.2.4"]

                 [superstring "3.0.0"]
                 [funcool/cuerdas "RELEASE"]
                 [slingshot "0.12.2"]

                 [clj-http "3.10.1"]
                 [medley "1.3.0"]
                 [cheshire "5.9.0"]
                 [camel-snake-kebab "0.4.0"]]
  :main ^:skip-aot clojure-training-2024.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
