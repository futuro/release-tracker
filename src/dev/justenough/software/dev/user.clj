(ns justenough.software.dev.user
  (:require [clojure.tools.namespace.repl :as tools-ns :refer [set-refresh-dirs]]
            [mount.core :as mount]
            ;; this is the top-level dependent component...mount will find the rest via ns requires
            [justenough.software.release-tracker.server :refer [http-server]]))

(defn start
  "Start the web server"
  [] (mount/start))

(defn stop
  "Stop the web server"
  [] (mount/stop))

(defn restart
  "Stop, reload code, and restart the server. If there is a compile error, use:

  ```
  (tools-ns/refresh)
  ```

  to recompile, and then use `start` once things are good."
  []
  (stop)
  (tools-ns/refresh :after 'justenough.software.dev.user/start))

(comment
  (start)
  (stop)
  (restart)
  )
