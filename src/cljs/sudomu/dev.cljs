(ns sudomu.dev
  (:require [figwheel.client :as fw :include-macros true]))

(def localhost? (-> js/window
                    (.-location)
                    (.-host)
                    (.indexOf "localhost")
                    (>= 0)))

(if localhost?
  (enable-console-print!)
  (fw/watch-and-reload
   :websocket-url   "ws://localhost:3449/figwheel-ws"
   :jsload-callback (fn [] (print "reloaded"))))
