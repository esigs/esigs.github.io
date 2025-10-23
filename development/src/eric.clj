(ns eric
  (:require [ca.ericsigurdson.site-generator.interface :as generator]
            [ca.ericsigurdson.markdown-parser.interface :as parser]
            [ca.ericsigurdson.html-renderer.interface :as renderer]
            [ca.ericsigurdson.template.interface :as template]
            [ca.ericsigurdson.file-utils.interface :as files]))

(comment

  ;; Generate the full site
  (generator/generate-site
   {:content-dir "content"
    :output-dir "public"
    :static-dir "static"
    :site-config {:site-title "My Site"
                  :css ["/css/style.css"]}})

  ;; Quick generate with defaults
  (generator/generate-site {})

  ;; Test markdown parsing
  (parser/parse "---\ntitle: Test\n---\nHello **world**")

  ;; Test markdown to hiccup conversion
  (renderer/markdown->hiccup "# Hello\n\nThis is a **test**")

  ;; Test template rendering
  (template/render-page
   {:title "Test Page"
    :content-hiccup [:div [:h1 "Hello"] [:p "World"]]
    :site-config {:site-title "My Site"
                  :css ["/css/style.css"]}
    :relative-path ""
    :resolve-url (fn [url] url)})

  ;; Get all pages
  (generator/get-pages {:content-dir "content"})

  ;; List content files
  (files/list-files "content" {:extension ".md"})

  ;; Read a specific content file
  (files/read-file "content/index.md")

  ;; Clean output directory
  (when (files/file-exists? "public")
    (files/clean-directory "public"))

  )
