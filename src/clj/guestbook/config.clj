(ns guestbook.config
  (:require
   [cprop.core :refer [load-config]]
   [cprop.source :as source]
   [mount.core :refer [args defstate]])
  (:refer-clojure :exclude [env]))

;; Define the env state
(declare env)

(defstate ^:dynamic env
  :start
  (load-config
   :merge
   [(args)
    (source/from-system-props)
    (source/from-env)]))