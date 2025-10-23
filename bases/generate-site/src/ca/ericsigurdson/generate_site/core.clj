(ns ca.ericsigurdson.generate-site.core
  (:require [ca.ericsigurdson.site-generator.interface :as generator]
            [clojure.tools.cli :as cli])
  (:gen-class))

(def cli-options
  [["-c" "--content-dir DIR" "Content directory"
    :default "content"]
   ["-o" "--output-dir DIR" "Output directory"
    :default "public"]
   ["-s" "--static-dir DIR" "Static assets directory"
    :default "static"]
   ["-t" "--site-title TITLE" "Site title"
    :default "Eric Sigurdson"]
   ["-h" "--help" "Show help"]])

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options)
      (do
        (println "Static Site Generator")
        (println "")
        (println "Usage: generate-site [options]")
        (println "")
        (println "Options:")
        (println summary)
        (System/exit 0))

      errors
      (do
        (doseq [error errors]
          (println "Error:" error))
        (System/exit 1))

      :else
      (try
        (let [site-config {:site-title (:site-title options)
                          :css ["/css/style.css"]}
              result (generator/generate-site
                      {:content-dir (:content-dir options)
                       :output-dir (:output-dir options)
                       :static-dir (:static-dir options)
                       :site-config site-config})]
          (when (= :success (:status result))
            (System/exit 0)))
        (catch Exception e
          (println "Error generating site:" (.getMessage e))
          (.printStackTrace e)
          (System/exit 1))))))
