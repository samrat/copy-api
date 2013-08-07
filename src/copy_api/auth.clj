(ns copy-api.auth
  (:require [oauth.client :as oauth]))

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

(defn make-credentials
  [consumer access-token-response method url body]
  (oauth/credentials consumer
                     (:oauth_token access-token-response)
                     (:oauth_token_secret access-token-response)
                     method
                     url
                     body))