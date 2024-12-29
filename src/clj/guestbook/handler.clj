(ns guestbook.handler
  (:require
   [guestbook.middleware :as middleware]
   [guestbook.layout :refer [error-page]]
   [guestbook.routes.home :refer [home-routes]]
   [reitit.ring :as ring]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.webjars :refer [wrap-webjars]]
   [guestbook.env :refer [defaults]]
   [mount.core :as mount]))

(defn- async-aware-default-handler
  ([_] nil)
  ([_ respond _] (respond nil)))

(def app-routes
  (ring/ring-handler
   (ring/router
    [(home-routes)])
   (ring/routes
    (ring/create-resource-handler
     {:path "/"})
    (wrap-content-type
     (wrap-webjars async-aware-default-handler))
    (ring/create-default-handler
     {:not-found
      (constantly (error-page {:status 404, :title "404 - Page not found"}))
      :method-not-allowed
      (constantly (error-page {:status 405, :title "405 - Not allowed"}))
      :not-acceptable
      (constantly (error-page {:status 406, :title "406 - Not acceptable"}))}))))

(defn app []
  (middleware/wrap-base #'app-routes))

(defn init-app []
  ((or (:init defaults) (fn []))))

(defn stop-app []
  ((or (:stop defaults) (fn []))))

(defn init []
  (init-app)
  (mount/start))

(defn destroy []
  (stop-app)
  (mount/stop))