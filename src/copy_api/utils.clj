(ns copy-api.utils
  (:require [oauth.signature :as oas]))

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