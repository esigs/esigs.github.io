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
  (let [{:keys [title description content-html]} context
        site-title (get-site-title context)
        css-paths (get-css-paths context)
        header-html (replicant/render
                     [:header
                      [:h1 site-title]
                      [:nav
                       [:a {:href (resolve-url context "/index.html")} "Home"]
                       [:a {:href (resolve-url context "/about.html")} "About"]]])
        footer-html (replicant/render [:footer [:p "© 2025"]])
        head-html (replicant/render
                   [:head
                    [:meta {:charset "utf-8"}]
                    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
                    (when title [:title title])
                    (when description [:meta {:name "description" :content description}])
                    (for [css-path css-paths]
                      [:link {:rel "stylesheet" :href css-path}])])]
    (str "<!DOCTYPE html>\n"
         "<html lang=\"en\">"
         head-html
         "<body><div>"
         header-html
         "<main><div class=\"markdown-content\">"
         content-html
         "</div></main>"
         footer-html
         "</div></body></html>")))

(defn render-post
  "Render a blog post with HTML content string.
   Context keys:
   - :title - Post title
   - :date - Publication date
   - :content-html - HTML string for post content
   - Plus all keys from render-page"
  [context]
  (let [{:keys [title date content-html]} context
        article-header (replicant/render
                        [:article
                         [:h1 title]
                         (when date [:time {:datetime date} date])])
        ;; Remove the closing tags from article-header since we'll add content
        article-start (str/replace article-header #"</article>$" "")
        full-html (str article-start
                      "<div class=\"markdown-content\">"
                      content-html
                      "</div></article>")]
    (render-page (assoc context :content-html full-html))))
