(ns sudomu.core
  (:require [clojure.browser.repl]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [figwheel.client :as fw :include-macros true]
            [clojure.string :refer [join]]
            sudomu.dev))


(defonce app-state (atom {:selected [0 0]
                          :solved [[1 2 3 4 5 6 7 8 9]
                                   [4 5 6 7 8 9 1 2 3]
                                   [7 8 9 1 2 3 4 5 6]
                                   [2 3 4 5 6 7 8 9 1]
                                   [5 6 7 8 9 1 2 3 4]
                                   [8 9 1 2 3 4 5 6 7]
                                   [3 4 5 6 7 8 9 1 2]
                                   [6 7 8 9 1 2 3 4 5]
                                   [9 1 2 3 4 5 6 7 8]]
                          :start [[0 0 0 4 0 0 0 8 0]
                                  [0 0 6 0 0 9 0 2 0]
                                  [7 0 9 0 2 0 0 5 0]
                                  [0 0 4 5 0 0 8 9 1]
                                  [0 6 0 8 0 0 2 0 4]
                                  [0 0 0 0 0 0 0 6 0]
                                  [0 4 5 6 7 0 9 0 2]
                                  [0 7 0 0 1 2 3 0 0]
                                  [0 0 0 0 4 0 0 0 0]]
                          :input (vec (repeat 9 (vec (repeat 9 0))))}))

(defn app->board [app]
  (for [x (range 9)]
    (for [y (range 9)]
      (let [solved (get-in (:solved app) [x y])
            start (get-in (:start app) [x y])
            input (get-in (:input app) [x y])]
        {:x x
         :y y
         :display (if (= 0 start) (if (= 0 input) "" input) start)
         :selected (= [x y] (:selected app))
         :editable (= 0 start)
         :error (and (not (= solved input))
                     (not (= 0 input)) )}))))

(defn render-board [board]
  (apply dom/div nil (map render-row board)))

(defn render-row [row]
  (apply dom/div #js {:className "row"} (map render-box row)))

(defn render-box [cell]
  (let [class (cond-> (str "box" " x-" (:x cell) " y-" (:y cell))
                      (:editable cell) (str " editable")
                      (:error cell) (str " error")
                      (:selected cell) (str " selected"))]
    (dom/div #js {:className class
                  :onClick (fn [_]
                             (swap! app-state #(assoc % :selected [(:x cell) (:y cell)])))}
             (:display cell))))

(defn handle-input [i]
  (swap! app-state #(assoc-in % (list* :input (:selected @app-state)) i)))

(defn render-controls []
  (apply dom/div #js {:className "controls"}
         (map (fn [i] (dom/div #js {:className "control"
                                   :onClick (fn [_] (handle-input i))} i))
              (range 1 10))))

(defn go-right []
  (swap! app-state #(update-in % [:selected 1] inc)))

(defn go-left []
  (swap! app-state #(update-in % [:selected 1] dec)))

(defn go-up []
  (swap! app-state #(update-in % [:selected 0] dec)))

(defn go-down []
  (swap! app-state #(update-in % [:selected 0] inc)))

(om/root
 (fn [app owner]
   (reify
     om/IDidMount
     (did-mount [_]
       (.addEventListener js/document "keydown" (fn [e]
                                                  (cond
                                                   (= "Right" (.-key e)) (go-right)
                                                   (= "Left" (.-key e)) (go-left)
                                                   (= "Up" (.-key e)) (go-up)
                                                   (= "Down" (.-key e)) (go-down))
                                                  (let [keycode (.-keyCode e)]
                                                    (if (and (>= keycode 49) (<= keycode 57))
                                                      (handle-input (js/parseInt (.-key e))))))))

     om/IRender
     (render [_]
       (dom/div nil
                (render-board (app->board app))
                ;;(render-controls)
                ))))
 app-state
 {:target (. js/document (getElementById "app"))})

(fw/watch-and-reload
 :websocket-url   "ws://localhost:3449/figwheel-ws"
 :jsload-callback (fn [] (print "reloaded")))

(enable-console-print!)
(println "foo")
