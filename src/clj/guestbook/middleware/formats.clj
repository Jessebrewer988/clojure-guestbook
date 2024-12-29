(ns guestbook.middleware.formats
  (:require
   [luminus-transit.time :as time]
   [muuntaja.core :as m]
   [muuntaja.format.json :as json-format]))

(def instance
  (m/create
   (-> m/default-options
       (update-in [:formats "application/json"] merge json-format/format)
       (update-in [:formats "application/transit+json" :decoder-opts]
                  (partial merge time/time-deserialization-handlers))
       (update-in [:formats "application/transit+json" :encoder-opts]
                  (partial merge time/time-serialization-handlers)))))