(defproject trampiti "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/data.json "0.2.3"]
                 [org.clojure/data.csv "0.1.2"]
                 [http-kit "2.1.13"]
                 [clj-time "0.5.1"]
                 [jarohen/chime "0.1.2"]
                 [pandect "0.3.0"]
                 [com.taoensso/timbre "2.7.1"]]

  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.3"]
                                  [org.clojure/java.classpath "0.2.0"]]}})
