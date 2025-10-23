(ns ca.ericsigurdson.template.interface
  (:require [replicant.string :as replicant]
            [clojure.string :as str]))

;; Context-aware URL resolution

(defn- resolve-url
  "Resolve a URL using the context's resolve-url function."
  [context url]
  (if-let [resolve-fn (:resolve-url context)]
    (resolve-fn url)
    url))

(defn- get-css-paths
  "Get CSS paths from site config and resolve them."
  [context]
  (let [css-urls (get-in context [:site-config :css] ["/css/style.css"])]
    (map #(resolve-url context %) css-urls)))

(defn- get-site-title
  "Get site title from config."
  [context]
  (get-in context [:site-config :site-title] "My Site"))

;; Hiccup building functions

(defn- page-head
  "Build the <head> section as hiccup."
  [context]
  (let [{:keys [title description]} context
        css-paths (get-css-paths context)]
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     (when title [:title title])
     (when description [:meta {:name "description" :content description}])
     (for [css-path css-paths]
       [:link {:rel "stylesheet" :href css-path}])]))

(defn- page-header
  "Build the header section as hiccup."
  [context]
  (let [site-title (get-site-title context)]
    [:header
     [:h1 site-title]
     [:nav
      [:a {:href (resolve-url context "/index.html")} "Home"]
      [:a {:href (resolve-url context "/about.html")} "About"]]]))

(defn- page-footer
  "Build the footer section as hiccup."
  [context]
  [:footer [:p "© 2025"]])

;; Rendering functions

(defn render-page
  "Render a page with HTML content string.
   Context keys:
   - :title - Page title
   - :description - Meta description
   - :content-html - HTML string for main content
   - :site-config - Site configuration
   - :resolve-url - Function to resolve URLs relative to current page"
  [context]
  (let [{:keys [content-html]} context
        ;; Build page structure as hiccup, render the wrapper ONCE
        page-wrapper [:html {:lang "en"}
                      (page-head context)
                      [:body
                       [:div
                        (page-header context)
                        ;; Placeholder for content
                        ::content-placeholder
                        (page-footer context)]]]
        wrapper-html (replicant/render page-wrapper)
        ;; Replace placeholder with actual markdown HTML
        final-html (str/replace wrapper-html
                                (str (replicant/render ::content-placeholder))
                                (str "<main>" content-html "</main>"))]
    (str "<!DOCTYPE html>\n" final-html)))

(defn render-post
  "Render a blog post with HTML content string.
   Context keys:
   - :title - Post title
   - :date - Publication date
   - :content-html - HTML string for post content
   - Plus all keys from render-page"
  [context]
  (let [{:keys [title date content-html]} context
        ;; Build article wrapper, render once
        article-wrapper [:article
                         [:h1 title]
                         (when date [:time {:datetime date} date])
                         ::content-placeholder]
        article-html (replicant/render article-wrapper)
        ;; Replace placeholder with markdown HTML
        final-article (str/replace article-html
                                   (str (replicant/render ::content-placeholder))
                                   content-html)]
    (render-page (assoc context :content-html final-article))))
