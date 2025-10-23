(ns ca.ericsigurdson.template.interface
  (:require [hiccup.page :refer [html5]]))

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
  (get-in context [:site-config :site-title] "Eric Sigurdson"))

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
  [:footer])

(defn- post-footer
  "Build the post-specific footer with author bio."
  [context]
  [:section.post-footer
   [:hr]
   [:div.author-bio
    [:h3 "About Eric"]
    [:p "I'm a software developer passionate about building bridges between legacy systems and modern technology. I work with Clojure and .NET/C#, focusing on clean architecture and practical solutions to real-world problems."]
    [:p [:a {:href "https://www.linkedin.com/in/ericronaldsigurdson/" :target "_blank" :rel "noopener noreferrer"} "Connect with me on LinkedIn"] " to discuss legacy system modernization, AI integration, or anything related to software architecture."]]])

(defn- posts-list
  "Build a list of blog posts as hiccup."
  [context posts]
  (when (seq posts)
    [:section.posts
     [:h2 "What I'm working on:"]
     [:ul.post-list
      (for [post posts]
        [:li
         [:article
          [:h3 [:a {:href (resolve-url context (:url post))} (:title post)]]
          (when (:date post)
            [:time {:datetime (:date post)} (:date post)])
          (when (:description post)
            [:p (:description post)])]])]]))

;; Rendering functions

(defn render-page
  "Render a page with hiccup content.
   Context keys:
   - :title - Page title
   - :description - Meta description
   - :content-hiccup - Hiccup data structure for main content
   - :posts - List of blog posts (optional, for index page)
   - :show-footer? - Whether to show footer (default: true)
   - :site-config - Site configuration
   - :resolve-url - Function to resolve URLs relative to current page"
  [context]
  (let [{:keys [content-hiccup posts show-footer?]} context
        show-footer? (if (nil? show-footer?) true show-footer?)]
    (html5 {:lang "en"}
      (page-head context)
      [:body
       [:div
        (page-header context)
        [:main
         content-hiccup
         (when posts
           (posts-list context posts))]
        (when show-footer?
          (page-footer context))]])))

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
                        content-hiccup
                        (post-footer context)]]
    (render-page (assoc context :content-hiccup article-hiccup))))
