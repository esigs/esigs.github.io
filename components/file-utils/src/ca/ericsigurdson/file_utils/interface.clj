(ns ca.ericsigurdson.file-utils.interface
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.io File]
           [java.nio.file Files Paths StandardCopyOption]))

(defn file-exists?
  "Check if a file exists."
  [path]
  (.exists (io/file path)))

(defn ensure-dir
  "Ensure a directory exists, creating it if necessary."
  [path]
  (let [dir (io/file path)]
    (when-not (.exists dir)
      (.mkdirs dir))
    dir))

(defn read-file
  "Read the contents of a file as a string."
  [path]
  (slurp path))

(defn write-file
  "Write content to a file, creating parent directories if needed."
  [path content]
  (let [file (io/file path)]
    (ensure-dir (.getParent file))
    (spit file content)))

(defn list-files
  "List all files in a directory recursively.
   Returns a sequence of File objects.
   Options:
   - :extension - Filter by file extension (e.g., '.md')"
  ([dir]
   (list-files dir {}))
  ([dir {:keys [extension]}]
   (let [dir-file (io/file dir)]
     (when (.isDirectory dir-file)
       (->> (file-seq dir-file)
            (filter #(.isFile %))
            (filter #(if extension
                      (str/ends-with? (.getName %) extension)
                      true)))))))

(defn relative-path
  "Get the relative path from base-dir to file."
  [base-dir file]
  (let [base-path (.toPath (io/file base-dir))
        file-path (.toPath (io/file file))]
    (.toString (.relativize base-path file-path))))

(defn copy-file
  "Copy a file from source to destination."
  [source dest]
  (let [source-path (Paths/get source (into-array String []))
        dest-path (Paths/get dest (into-array String []))]
    (ensure-dir (.getParent (io/file dest)))
    (Files/copy source-path dest-path
                (into-array [StandardCopyOption/REPLACE_EXISTING]))))

(defn copy-directory
  "Recursively copy a directory from source to destination."
  [source dest]
  (let [source-file (io/file source)
        dest-file (io/file dest)]
    (when (.isDirectory source-file)
      (ensure-dir dest)
      (doseq [file (file-seq source-file)]
        (when (.isFile file)
          (let [rel-path (relative-path source file)
                dest-path (io/file dest rel-path)]
            (copy-file (.getPath file) (.getPath dest-path))))))))

(defn clean-directory
  "Delete all contents of a directory."
  [path]
  (let [dir (io/file path)]
    (when (.exists dir)
      (doseq [file (reverse (file-seq dir))]
        (io/delete-file file true)))))
