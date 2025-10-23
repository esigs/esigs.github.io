(ns ca.ericsigurdson.site-generator.interface
  (:require [ca.ericsigurdson.markdown-parser.interface :as parser]
            [ca.ericsigurdson.html-renderer.interface :as renderer]
            [ca.ericsigurdson.template.interface :as template]
            [ca.ericsigurdson.file-utils.interface :as files]
            [clojure.string :as str]
            [clojure.java.io :as io])
  (:import [java.time.format DateTimeFormatter]
           [java.time ZoneId]))

;; Date Formatting

(defn- format-date
  "Format a date object to YYYY-MM-DD string."
  [date]
  (when date
    (let [instant (.toInstant date)
          zdt (.atZone instant (ZoneId/systemDefault))
          formatter (DateTimeFormatter/ofPattern "MMMM d, yyyy")]
      (.format zdt formatter))))

;; URL Utilities

(defn- calculate-relative-path
  "Calculate relative path prefix based on URL depth.
   e.g., /index.html -> '', /posts/hello.html -> '../'"
  [url]
  (let [parts (str/split url #"/")
        depth (- (count parts) 2)]
    (if (pos? depth)
      (str/join (repeat depth "../"))
      "")))

(defn- resolve-url
  "Resolve a URL relative to the current context.
   Handles absolute URLs (starting with /) by making them relative."
  [context url]
  (if (str/starts-with? url "/")
    (str (:relative-path context) (subs url 1))
    url))

(defn- path->url
  "Convert a file path to a URL path.
   e.g., content/posts/hello.md -> /posts/hello.html"
  [base-dir file]
  (let [rel-path (files/relative-path base-dir file)
        without-ext (str/replace rel-path #"\.md$" ".html")]
    (str "/" without-ext)))

;; Context Building

(defn build-context
  "Build a context map for rendering a page at the given URL."
  [url site-config]
  (let [relative-path (calculate-relative-path url)]
    {:url url
     :site-config site-config
     :relative-path relative-path
     :resolve-url (partial resolve-url {:relative-path relative-path})}))

;; Page Rendering

(defn- render-markdown-page
  "Render a markdown file to HTML within a context."
  [context file-path]
  (let [content (files/read-file file-path)
        parsed (parser/parse content)
        metadata (:metadata parsed)
        markdown-content (:content parsed)
        content-hiccup (renderer/markdown->hiccup markdown-content)
        layout-type (get metadata :layout "page")
        ;; Merge metadata into context, preserving existing keys like :posts
        page-context (merge context
                           {:title (get metadata :title "Untitled")
                            :description (get metadata :description)
                            :date (format-date (get metadata :date))
                            :content-hiccup content-hiccup})]
    (case layout-type
      "post" (template/render-post page-context)
      "page" (template/render-page page-context)
      (template/render-page page-context))))

;; Post Metadata Collection

(defn- get-post-metadata
  "Extract metadata from a blog post file."
  [file-path url]
  (let [content (files/read-file file-path)
        parsed (parser/parse content)
        metadata (:metadata parsed)]
    {:url url
     :title (get metadata :title "Untitled")
     :date (format-date (get metadata :date))
     :description (get metadata :description)
     :layout (get metadata :layout "page")}))

(defn- get-all-posts
  "Get metadata for all blog posts in the posts directory."
  [content-dir]
  (let [posts-dir (str content-dir "/posts")
        post-files (when (files/file-exists? posts-dir)
                     (files/list-files posts-dir {:extension ".md"}))]
    (->> post-files
         (map (fn [file]
                (let [url (path->url content-dir file)
                      file-path (.getPath file)]
                  (get-post-metadata file-path url))))
         (filter :date)
         (sort-by :date)
         reverse
         vec)))

;; Page Discovery

(defn get-markdown-pages
  "Discover markdown files and return a map of URL -> page-fn."
  [content-dir]
  (let [md-files (files/list-files content-dir {:extension ".md"})
        posts (get-all-posts content-dir)]
    (into {}
          (for [file md-files]
            (let [url (path->url content-dir file)
                  file-path (.getPath file)]
              [url (fn [context]
                     (let [context-with-posts (if (= url "/index.html")
                                                (assoc context :posts posts)
                                                context)]
                       (render-markdown-page context-with-posts file-path)))])))))

(defn get-pages
  "Get all pages for the site as a map of URL -> content-fn.
   Each content-fn accepts a context map and returns HTML."
  [{:keys [content-dir] :or {content-dir "content"}}]
  (get-markdown-pages content-dir))

;; Export

(defn export-pages
  "Export pages to the output directory."
  [pages output-dir site-config]
  (doseq [[url page-fn] pages]
    (let [context (build-context url site-config)
          html (page-fn context)
          file-path (str output-dir url)]
      (println (str "  " url))
      (files/write-file file-path html))))

;; Main Generation Function

(defn generate-site
  "Generate the static site following the Stasis pattern.
   Options:
   - :content-dir - Directory containing markdown files (default: 'content')
   - :output-dir - Output directory for generated site (default: 'public')
   - :static-dir - Directory containing static assets (default: 'static')
   - :site-config - Site configuration map"
  [{:keys [content-dir output-dir static-dir site-config]
    :or {content-dir "content"
         output-dir "public"
         static-dir "static"
         site-config {}}}]

  (println "Generating site...")

  ;; Clean output directory
  (when (files/file-exists? output-dir)
    (files/clean-directory output-dir))
  (files/ensure-dir output-dir)

  ;; Copy static assets
  (when (files/file-exists? static-dir)
    (println "Copying static assets...")
    (files/copy-directory static-dir output-dir))

  ;; Get pages and export
  (let [pages (get-pages {:content-dir content-dir})]
    (println (str "Processing " (count pages) " pages..."))
    (export-pages pages output-dir site-config))

  (println "Site generation complete!")
  {:output-dir output-dir
   :status :success})
