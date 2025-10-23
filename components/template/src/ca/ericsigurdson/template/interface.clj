(ns ca.ericsigurdson.template.interface
  (:require [replicant.string :as replicant]))

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
  "Render a page with hiccup content.
   Context keys:
   - :title - Page title
   - :description - Meta description
   - :content-hiccup - Hiccup data structure for main content
   - :site-config - Site configuration
   - :resolve-url - Function to resolve URLs relative to current page"
  [context]
  (let [{:keys [content-hiccup]} context
        page-hiccup [:html {:lang "en"}
                     (page-head context)
                     [:body
                      [:div
                       (page-header context)
                       [:main content-hiccup]
                       (page-footer context)]]]]
    (str "<!DOCTYPE html>\n" (replicant/render page-hiccup))))

(defn render-post
  "Render a blog post with hiccup content.
   Context keys:
   - :title - Post title
   - :date - Publication date
   - :content-hiccup - Hiccup data structure for post content
   - Plus all keys from render-page"
  [context]
  (let [{:keys [title date content-hiccup]} context
        article-hiccup [:article
                        [:h1 title]
                        (when date [:time {:datetime date} date])
                        content-hiccup]]
    (render-page (assoc context :content-hiccup article-hiccup))))
