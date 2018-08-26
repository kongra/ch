(ns ^:figwheel-always kongra.ch)

(defn errMsg
  [x]
  (if (nil? x) "nil" (str x)))
