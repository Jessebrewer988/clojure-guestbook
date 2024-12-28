(ns guestbook.handler-test
  (:require
    [clojure.test :refer [use-fixtures deftest is testing]]
    [ring.mock.request :as mock]
    [guestbook.handler :refer [app]]
    [guestbook.middleware.formats :as formats]
    [guestbook.config :as config]
    [muuntaja.core :as m]
    [mount.core :as mount]))

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'guestbook.config/env
                 #'guestbook.handler/app-routes)
    (f)))

(deftest test-app
  (testing "main route"
    (let [response ((app) (mock/request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response ((app) (mock/request :get "/invalid"))]
      (is (= 404 (:status response))))))
