(ns ca.ericsigurdson.markdown-parser.interface
  (:require [clojure.string :as str]
            [yaml.core :as yaml]))

(defn- extract-frontmatter
  "Extract YAML frontmatter from markdown content.
   Returns a map with :metadata and :content keys."
  [text]
  (let [lines (str/split-lines text)]
    (if (and (seq lines) (= "---" (first lines)))
      (let [remaining (rest lines)
            end-idx (some #(when (= "---" (second %)) (first %))
                         (map-indexed vector remaining))]
        (if end-idx
          (let [frontmatter-lines (take end-idx remaining)
                content-lines (drop (inc end-idx) remaining)
                yaml-str (str/join "\n" frontmatter-lines)
                metadata (try
                           (yaml/parse-string yaml-str)
                           (catch Exception _
                             {}))]
            {:metadata (or metadata {})
             :content (str/join "\n" content-lines)})
          {:metadata {}
           :content text}))
      {:metadata {}
       :content text})))

(defn parse
  "Parse markdown file content, extracting frontmatter and content.
   Returns a map with :metadata and :content keys."
  [text]
  (extract-frontmatter text))
