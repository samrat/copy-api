(ns copy-api.client
  (:require [clj-http.client :as http]
            [cheshire.core :refer :all]
            [copy-api.auth :refer [make-credentials]]
            [copy-api.utils :refer :all]))

(defn account-info
  "Retrieves information about the user's account."
  [consumer access-token-response]
  (let [request-url "https://api.copy.com/rest/user"
        credentials (make-credentials consumer
                                      access-token-response
                                      :GET
                                      request-url
                                      nil)]
    (parse-string (:body (http/get request-url
                                   {:headers {"Authorization"
                                              (oauth-header-string credentials)
                                              "X-Api-Version" "1"}})))))

(defn meta-info
  "Retrieves file and folder metadata for the specified path."
  [consumer access-token-response & {:keys [path]
                                     :or {path ""}}]
  (let [request-url (str "https://api.copy.com/rest/meta/copy" path)
        credentials (make-credentials consumer
                                      access-token-response
                                      :GET
                                      request-url
                                      nil)]
    (parse-string
     (:body (http/get request-url
                      {:headers {"Authorization"
                                 (oauth-header-string credentials)
                                 "X-Api-Version" "1"}})))))

(defn file-activity
  "Retrieves file revisions metadata for a file at the specified path."
  [consumer access-token-response & {:keys [path time]
                                     :or {path ""}}]
  (let [request-url (str "https://api.copy.com/rest/meta/copy"
                         path
                         (if (= (str (last path)) "/")
                           nil
                           "/")
                         "@activity"
                         (when time
                           (str "/@time:" time)))
        credentials (make-credentials consumer
                                      access-token-response
                                      :GET
                                      request-url
                                      nil)]
    (parse-string
     (:body (http/get request-url
                      {:headers {"Authorization"
                                 (oauth-header-string credentials)
                                 "X-Api-Version" "1"}})))))

(defn upload-file
  "Retrieves file and folder metadata for the specified path."
  [consumer access-token-response & {:keys [path local-path]}]
  (let [request-url (str "https://api.copy.com/rest/files" path)
        credentials (make-credentials consumer
                                      access-token-response
                                      :POST
                                      request-url
                                      nil)]
    (parse-string
     (:body (http/post request-url
                       {:multipart [{:name "file"
                                     :content (clojure.java.io/file
                                               local-path)}]
                        :headers {"Authorization"
                                  (oauth-header-string credentials)
                                  "X-Api-Version" "1"}})))))

(defn delete-file
  "Deletes the file or directory at the specified path."
  [consumer access-token-response & {:keys [path]}]
  (let [request-url (str "https://api.copy.com/rest/files" path)
        credentials (make-credentials consumer
                                      access-token-response
                                      :DELETE
                                      request-url
                                      nil)]
    (parse-string
     (:body (http/delete request-url
                      {:headers {"Authorization"
                                 (oauth-header-string credentials)
                                 "X-Api-Version" "1"}})))))