(ns GG.M.M-4-2-02
  (:require [clojure2d.core :refer :all]
            [fastmath.core :as m]
            [fastmath.vector :as v])
  (:import [fastmath.vector Vec2]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(m/use-primitive-operators)

(def ^:const ^int w 800)
(def ^:const ^int h 800)
(def ^:const ^int min-x 0)
(def ^:const ^int min-y 0)
(def ^:const ^int max-x w)
(def ^:const ^int max-y h)
(def ^:const ^double radius 200.0)
(def ^:const ^double rradius (/ radius))
(def ^:const ^double grid-size 600.0)
(def ^:const ^double strength -10.0)
(def ^:const ^double ramp 0.4)
(def ^:const ^int x-count 301)
(def ^:const ^int y-count 301)

(def ^:const ^double x-factor (/ grid-size (dec x-count)))
(def ^:const ^double y-factor (/ grid-size (dec y-count)))
(def ^:const ^double x-shift (* 0.5 (- w grid-size)))
(def ^:const ^double y-shift (* 0.5 (- h grid-size)))

(deftype Node [pos velocity ^double damping])

(defn update-node
  ""
  [^Node node]
  (let [^Vec2 velocity (.velocity node)
        ^Vec2 npos (v/add (.pos node) velocity)
        x (m/constrain (.x npos) 0 (dec w))
        y (m/constrain (.y npos) 0 (dec h))
        vx (if (or (< x min-x)
                   (> x max-x))
             (- (.x velocity))
             (.x velocity))
        vy (if (or (< y min-y)
                   (> y max-y))
             (- (.y velocity))
             (.y velocity))]
    (Node. (v/vec2 x y) (v/mult (v/vec2 vx vy) (- 1.0 (.damping node))) (.damping node))))

(defn attract
  ""
  [^Vec2 xy ^Node node]
  (let [^Vec2 pos (.pos node)
        dxy (v/vec2 (- (.x xy) (.x pos))
                    (- (.y xy) (.y pos)))
        d (v/mag dxy)]
    (if (and (pos? d) (< d radius))
      (let [s (* d rradius)
            f (* strength (dec (/ (m/pow s (* 0.5 ramp)))) rradius)]
        (Node. pos (v/add (.velocity node) (v/mult dxy f)) (.damping node)))
      node)))

(defn init-grid
  ""
  []
  (for [^int y (range y-count)
        ^int x (range x-count)
        :let [x-pos (+ x-shift (* x x-factor))
              y-pos (+ y-shift (* y y-factor))]]
    (Node. (v/vec2 x-pos y-pos) (v/vec2 0.0 0.0) 0.8)))

(defn draw
  ""
  [canvas window _ nodes]  
  (-> canvas
      (set-background :white)
      (set-color :black))

  (let [nodes (if (or (not nodes)
                      (and (key-pressed? window) (= (key-char window) \r)))
                (init-grid)
                nodes)
        mpos (mouse-pos window)]
    
    (mapv #(let [^Node node (update-node (if (mouse-pressed? window) (attract mpos %) %))
                 ^Vec2 pos (.pos node)]
             (rect canvas (.x pos) (.y pos) 1 1)
             node) nodes)))

(def window (show-window {:canvas (canvas w h)
                          :draw-fn draw}))
