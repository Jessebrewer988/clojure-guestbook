(ns guestbook.dev-middleware
  (:require
   [guestbook.config :refer [env]]
   [ring.middleware.reload :refer [wrap-reload]]
   [selmer.middleware :refer [wrap-error-page]]
   [prone.middleware :refer [wrap-exceptions]]))

(defn wrap-dev [handler]
  (-> handler
      wrap-reload
      wrap-error-page
      (cond-> (not (:async? env)) (wrap-exceptions {:app-namespaces ['guestbook]}))))