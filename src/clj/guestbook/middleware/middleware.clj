(ns guestbook.middleware
  (:require
   [guestbook.middleware.formats :refer [instance]]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
   [muuntaja.middleware :refer [wrap-format wrap-params]]))

(defn wrap-csrf [handler]
  (wrap-anti-forgery
   handler
   {:error-handler
    (fn [request]
      {:status 403
       :headers {"Content-Type" "text/plain"}
       :body "Invalid anti-forgery token"})}))

(defn wrap-formats [handler]
  (-> handler
      (wrap-params)
      (wrap-format instance)))

(def middleware
  [#(wrap-defaults % site-defaults)
   wrap-csrf
   wrap-formats])