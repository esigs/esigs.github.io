(ns ca.ericsigurdson.markdown-parser.interface
  (:require [clojure.string :as str]))

(defn- parse-yaml-line
  "Parse a simple YAML key-value line.
   Handles: key: value, key: \"value\", key: 'value'"
  [line]
  (when-let [[_ k v] (re-matches #"^\s*([^:]+):\s*(.*)$" line)]
    (let [key (str/trim k)
          val (str/trim v)
          value (cond
                  (and (str/starts-with? val "\"") (str/ends-with? val "\""))
                  (subs val 1 (dec (count val)))

                  (and (str/starts-with? val "'") (str/ends-with? val "'"))
                  (subs val 1 (dec (count val)))

                  :else val)]
      [key value])))

(defn- parse-simple-yaml
  "Parse a simple subset of YAML into a map.
   Only handles simple key: value pairs."
  [yaml-str]
  (when yaml-str
    (reduce
     (fn [acc line]
       (if-let [[k v] (parse-yaml-line line)]
         (assoc acc k v)
         acc))
     {}
     (str/split-lines yaml-str))))

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
                metadata (parse-simple-yaml (str/join "\n" frontmatter-lines))]
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
