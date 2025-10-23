(ns ca.ericsigurdson.html-renderer.interface
  (:require [markdown-to-hiccup.core :as md]))

(defn markdown->hiccup
  "Convert markdown string to hiccup data structure."
  [markdown]
  (->> markdown
       md/md->hiccup
       md/component))
