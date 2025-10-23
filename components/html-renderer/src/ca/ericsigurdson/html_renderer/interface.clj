(ns ca.ericsigurdson.html-renderer.interface
  (:require [markdown.core :as md]))

(defn markdown->html
  "Convert markdown string to HTML string."
  [markdown]
  (md/md-to-html-string markdown))
