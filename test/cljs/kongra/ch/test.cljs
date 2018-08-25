(ns ^:figwheel-always kongra.ch.test
  (:require [reagent.core :as  r]
            [kongra.ch    :as ch]))

(enable-console-print!)

(defn mainView
  []
  [:h2 "kongra.ch Test Page"])

;; INSTRUMENTATION

(defn onJSreload
  []
  (println "Reloaded...")
  (r/render-component [mainView] (. js/document (getElementById "app"))))

(defn init
  []
  (onJSreload))

(defonce start (init))
