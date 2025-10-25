(ns ca.ericsigurdson.html-renderer.interface
  (:require [markdown-to-hiccup.core :as md]
            [clojure.walk :as walk]
            [clojure.string :as str]))

(defn- escape-html
  "Escape HTML entities in a string."
  [s]
  (when s
    (-> s
        (str/replace "&" "&amp;")
        (str/replace "<" "&lt;")
        (str/replace ">" "&gt;")
        (str/replace "\"" "&quot;"))))

(defn- fix-code-blocks
  "Walk through hiccup structure and escape HTML entities in code blocks.
   This prevents angle brackets in code from being interpreted as HTML tags."
  [hiccup]
  (walk/postwalk
    (fn [form]
      (if (and (vector? form)
               (= :code (first form))
               (map? (second form)))
        ;; Found a code block: [:code {:class ...} content]
        ;; Escape HTML entities in the content
        (let [[tag attrs & content] form]
          (into [tag attrs] (map escape-html content)))
        form))
    hiccup))

(defn markdown->hiccup
  "Convert markdown string to hiccup data structure."
  [markdown]
  (->> markdown
       md/md->hiccup
       md/component
       fix-code-blocks))
