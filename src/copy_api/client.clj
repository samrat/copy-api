(ns copy-api.client
  (:require [oauth.client :as oauth]
            [oauth.signature :as oas]
            [clj-http.client :as http]
            [cheshire.core :refer :all]))

;; Authorization & Authentication
(defn make-consumer
  "Takes an API key and secret and returns a Copy OAuth consumer. The
  next step is to call request-token with the consumer returned
  here(optionally with a callback-url)."
  [consumer-key consumer-secret]
  (oauth/make-consumer consumer-key
                       consumer-secret
                       "https://api.copy.com/oauth/request"
                       "https://api.copy.com/oauth/access"
                       "https://www.copy.com/applications/authorize"
                       :hmac-sha1))

(defn request-token
  "Takes a consumer and optionally a callback-uri and returns a request
  token that user will need to authorize."
  [consumer callback-uri]
  (oauth/request-token consumer callback-uri))

(defn authorization-url
  "Takes a consumer and request-token and returns a URL to send the
  user to in order to authenticate."
  [consumer request-token]
  (oauth/user-approval-uri consumer
                           (:oauth_token request-token)))

(defn access-token-response
  "Takes a consumer and request-token and returns a map with
  :oauth_token and :oauth_token_secret."
  [consumer request-token verifier]
  (oauth/access-token consumer
                      request-token
                      verifier))

;; From https://github.com/adamwynne/twitter-api/blob/master/src/twitter/oauth.clj
(defn oauth-header-string
  "creates the string for the oauth header's 'Authorization' value, url
  encoding each value"
  [signing-map & {:keys [url-encode?] :or {url-encode? true}}]

  (let [val-transform (if url-encode? oas/url-encode identity)
        s (reduce (fn [s [k v]]
                    (format "%s%s=\"%s\","
                            s (name k) (val-transform (str v))))
                  "OAuth "
                  signing-map)]
    (.substring s 0 (dec (count s)))))


;; API calls

(defn make-credentials
  [consumer access-token-response method url body]
  (oauth/credentials consumer
                     (:oauth_token access-token-response)
                     (:oauth_token_secret access-token-response)
                     method
                     url
                     body))

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
                                     :content (clojure.java.io/file local-path)}]
                        :headers {"Authorization"
                                  (oauth-header-string credentials)
                                  "X-Api-Version" "1"}})))))