(ns com.joshuagriffith.async-agent
  (:require [manifold
             [deferred :as d]
             [stream :as s]])
  (:import clojure.lang.IDeref))

(def ^:const cores (.availableProcessors (Runtime/getRuntime)))

(deftype AsyncAgent [state update-stream]
  IDeref
  (deref [_] @state))

(defn async-agent
  ([v] (async-agent v (+ cores 2)))
  ([v buf]
   (let [s   (s/stream* {:buffer-size buf})
         val (volatile! v)]
     (d/loop [v v]
       (d/chain' (s/take! s)

         (fn [[f d]]
           (-> (d/chain (d/future (f v))
                 (fn [nv]
                   (vreset! val nv)
                   (d/success! d nv)
                   (d/recur nv)))

               (d/catch Exception
                 (fn [e]
                   (d/error! d e)
                   (d/recur v)))))))

     (AsyncAgent. val s))))

(defn async-send
  ([^AsyncAgent async-agent f & args]
   (async-send async-agent #(apply f (list* % args))))
  ([^AsyncAgent async-agent f]
   (let [complete (d/deferred)]
     (s/put! (.-update-stream async-agent) [f complete])
     complete)))
