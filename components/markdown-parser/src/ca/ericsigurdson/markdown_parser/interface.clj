(ns ca.ericsigurdson.markdown-parser.interface
  (:require [clojure.string :as str]
            [clojure.edn :as edn]))

(defn- extract-frontmatter
  "Extract EDN frontmatter from markdown content.
   Returns a map with :metadata and :content keys.

   Expects frontmatter as an EDN map at the start of the file:
   {:title \"My Post\" :date \"2025-10-23\"}

   # Content here..."
  [text]
  (let [trimmed (str/trim text)]
    (if (str/starts-with? trimmed "{")
      (try
        (let [reader (java.io.PushbackReader. (java.io.StringReader. trimmed))
              metadata (edn/read reader)
              ;; Read remaining text after the EDN map
              remaining-text (slurp reader)
              content (str/trim remaining-text)]
          {:metadata metadata
           :content content})
        (catch Exception _
          {:metadata {}
           :content text}))
      {:metadata {}
       :content text})))

(defn parse
  "Parse markdown file content, extracting EDN frontmatter and content.
   Returns a map with :metadata and :content keys."
  [text]
  (extract-frontmatter text))
