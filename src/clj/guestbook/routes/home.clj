(ns guestbook.routes.home
  (:require
   [guestbook.layout :as layout]
   [guestbook.db.core :as db]
   [guestbook.middleware :as middleware]
   [ring.util.http-response :as response]
   [struct.core :as st]))

(def message-schema
  [[:name
    st/required
    st/string]
   [:message
    st/required
    st/string
    {:message "Message must be at least 10 characters"
     :validate #(> (count %) 9)}]])

(def reaction-schema
  [[:message_id
    st/required
    st/integer-str]
   [:reaction_type
    st/required
    st/string
    {:message "Invalid reaction type"
     :validate #(contains? #{"👍" "❤️" "👎"} %)}]])

(defn validate-message [params]
  (first (st/validate params message-schema)))

(defn validate-reaction [params]
  (first (st/validate params reaction-schema)))

(defn save-message! [{:keys [params]}]
  (if-let [errors (validate-message params)]
    (-> (response/found "/")
        (assoc :flash (assoc params :errors errors)))
    (do
      (db/save-message!
       (assoc params :timestamp (java.util.Date.)))
      (response/found "/"))))

(defn save-reaction! [{:keys [params headers remote-addr]}]
  (println (str "Saving reaction: " params))
  (if-let [errors (validate-reaction params)]
    (response/bad-request {:errors errors})
    (do
      (db/save-reaction!
       (assoc params
              :user_identifier remote-addr
              :timestamp (java.util.Date.)))
      (println (str "Saved reaction: " params))
      (response/ok {:status "ok"}))))

(defn home-page [{:keys [flash remote-addr] :as request}]
  (layout/render
   request
   "home.html"
   (merge {:messages (db/get-messages remote-addr)}
          (select-keys flash [:name :message :errors]))))

(defn about-page [request]
  (layout/render request "about.html"))

(defn get-all-reactions [request]
  (response/ok {:reactions (db/get-all-reactions)}))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page
         :post save-message!}]
   ["/about" {:get about-page}]
   ["/api/reaction" {:post save-reaction!}]
   ["/api/debug/reactions" {:get get-all-reactions}]])  ; Add this debug route
