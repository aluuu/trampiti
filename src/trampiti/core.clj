(ns trampiti.core
  (:require [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [clj-time.format :as tf]
            [clj-time.core :as t]
            [clj-time.local :as tl]
            [clj-time.coerce :as coerce]
            [clj-time.periodic :refer [periodic-seq]]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [taoensso.timbre :as timbre
             :refer (log trace debug info)]
            [trampiti.utils :refer [query-string]])
  (:use [pandect.core]))


(defprotocol Exchange

  (api-url [this])
  (public-api-url [this])
  (sign [this request])
  (headers [this request])
  (fetch [this url])
  (query [this method request])

  ;; Public API
  (market-data [this market-id])
  (order-data [this market-id])

  ;; Private API
  (get-info [this])
  (get-markets [this])
  (my-transactions [this])
  (market-trades [this market-id])
  (market-orders [this market-id])
  (my-trades [this market-id limit])
  (all-my-trades [this])
  (my-orders [this market-id])
  (all-my-orders [this])
  (depth [this market-id])
  (create-order [this market-id order-type quantity price])
  (cancel-order [this order-id])
  (cancel-market-orders [this market-id])
  (cancel-all-orders [this])
  (calculate-fees [this order-type quantity price])
  (generate-new-address [this currency-id currency-code]))

(deftype Cryptsy [api-key api-secret]
  Exchange
  (api-url [this]
    "https://www.cryptsy.com/api")

  (public-api-url [this]
    "http://pubapi.cryptsy.com/api.php")

  (sign [this params]
    (sha512-hmac (query-string params) api-secret))

  (headers [this params]
    {:headers {"Sign" (sign this params)
               "Key" api-key}})

  (fetch [this url]
    (debug "Fetching (GET)" url)
    (let [options {:insecure? false
                   :user-agent "cljyptsy-0.0.1"
                   :content-type "application/json"}
          response (-> @(http/get url options)
                      :body
                      (json/read-str))]
      response))

  (query [this method params]
    (debug "Querying (POST)" method)
    (let [params (merge params
                        {"nonce" (coerce/to-long (t/now))
                         "method" method})
          options (merge {:insecure? false
                          :user-agent "cljyptsy-0.0.1"
                          :content-type "application/json"
                          :form-params params}
                         (headers this params))
          response (-> @(http/post (api-url this) options)
                      :body
                      (json/read-str))]
      response))

  ;; Public part of API
  (market-data [this market-id]
    (if (= market-id :all)
      (fetch this (str (public-api-url this)
                       "?method=marketdatav2"))
      (fetch this (str (public-api-url this)
                       "?method=singlemarketdata&marketid="
                       market-id))))

  (order-data [this market-id]
    (if (= market-id :all)
      (fetch this (str (public-api-url this)
                       "?method=orderdata"))
      (fetch this (str (public-api-url this)
                       "?method=singleorderdata&marketid="
                       market-id))))

  ;; Private part of API
  (get-info [this]
    (query this "getinfo" {}))

  (get-markets [this]
    (query this "getmarkets" {}))

  (my-transactions [this]
    (query this "mytransactions" {}))

  (market-trades [this market-id]
    (query this "markettrades" {"marketid" market-id}))

  (market-orders [this market-id]
    (query this "marketorders" {"marketid" market-id}))

  (my-trades [this market-id limit]
    (query this "mytrades" {"marketid" market-id
                            "limit" limit}))

  (all-my-trades [this]
    (query this "allmytrades" {}))

  (my-orders [this market-id]
    (query this "myorders" {"marketid" market-id}))

  (depth [this market-id]
    (query this "depth" {"marketid" market-id}))

  (all-my-orders [this]
    (query this "allmyorders" {}))

  (create-order [this market-id order-type quantity price]
    (query this "createorder" {"marketid" market-id
                               "ordertype" order-type
                               "quantity" quantity
                               "price" price}))

  (cancel-order [this order-id]
    (query this "cancelorder" {"orderid" order-id}))

  (cancel-market-orders [this market-id]
    (query this "cancelmarketorders" {"marketid" market-id}))

  (cancel-all-orders [this]
    (query this "cancelallorders" {}))

  (calculate-fees [this order-type quantity price]
    (query this "calculatefees" {"ordertype" order-type
                                 "quantity" quantity
                                 "price" price}))

  (generate-new-address [this currency-id currency-code]
    (query this "generatenewaddress" {"currencyid" currency-id
                                      "currencycode" currency-code})))
