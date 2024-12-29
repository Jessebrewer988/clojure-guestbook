(ns guestbook.db.core
  (:require
   [next.jdbc.date-time]
   [next.jdbc.result-set]
   [next.jdbc :as sql]
   [conman.core :as conman]
   [mount.core :refer [defstate]]
   [guestbook.config :refer [env]])
  (:refer-clojure :exclude [*db*]))

(declare ^:dynamic *db*)

(defstate ^:dynamic *db*
  :start (if-let [jdbc-url (env :database-url)]
           (conman/connect! {:jdbc-url jdbc-url})
           (throw (Exception. "Database configuration not found")))
  :stop (when *db* (conman/disconnect! *db*)))

(conman/bind-connection *db* "sql/queries.sql")

(defn get-messages [user-identifier]
  (let [messages (next.jdbc/execute!
                  *db*
                  ["SELECT m.*,
                     COALESCE((SELECT COUNT(*) FROM reactions r 
                      WHERE r.message_id = m.id AND r.reaction_type = '👍'), 0) as thumbs_up_count,
                     COALESCE((SELECT COUNT(*) FROM reactions r 
                      WHERE r.message_id = m.id AND r.reaction_type = '❤️'), 0) as heart_count,
                     COALESCE((SELECT COUNT(*) FROM reactions r 
                      WHERE r.message_id = m.id AND r.reaction_type = '👎'), 0) as thumbs_down_count,
                     COALESCE((SELECT COUNT(*) FROM reactions r 
                      WHERE r.message_id = m.id AND r.reaction_type = '👍' AND r.user_identifier = ?), 0) as user_thumbs_up,
                     COALESCE((SELECT COUNT(*) FROM reactions r 
                      WHERE r.message_id = m.id AND r.reaction_type = '❤️' AND r.user_identifier = ?), 0) as user_heart,
                     COALESCE((SELECT COUNT(*) FROM reactions r 
                      WHERE r.message_id = m.id AND r.reaction_type = '👎' AND r.user_identifier = ?), 0) as user_thumbs_down
                    FROM guestbook m
                    ORDER BY m.timestamp DESC"
                   user-identifier
                   user-identifier
                   user-identifier]
                  {:builder-fn next.jdbc.result-set/as-unqualified-lower-maps})]
    (map (fn [message]
           (assoc message
                  :reactions {"👍" (:thumbs_up_count message 0)
                              "❤️" (:heart_count message 0)
                              "👎" (:thumbs_down_count message 0)}
                  :user_reactions {"👍" (pos? (:user_thumbs_up message))
                                   "❤️" (pos? (:user_heart message))
                                   "👎" (pos? (:user_thumbs_down message))}))
         messages)))

(defn save-message! [message]
  (next.jdbc/execute! *db* ["INSERT INTO guestbook (name, message, timestamp) VALUES (?, ?, ?)" (:name message) (:message message) (:timestamp message)]))

(defn get-all-reactions []
  (next.jdbc/execute!
   *db*
   ["SELECT * FROM reactions"]
   {:builder-fn next.jdbc.result-set/as-unqualified-lower-maps}))

(defn get-user-reaction [message-id user-identifier]
  (next.jdbc/execute!
   *db*
   ["SELECT reaction_type FROM reactions 
      WHERE message_id = ? AND user_identifier = ?"
    message-id user-identifier]
   {:builder-fn next.jdbc.result-set/as-unqualified-lower-maps}))

(defn save-reaction! [reaction]
  (let [reaction (update reaction :message_id #(Integer/parseInt %))
        existing (first (get-user-reaction (:message_id reaction) (:user_identifier reaction)))]

    (if existing
      ;; If clicking same reaction type, delete it (toggle off)
      (if (= (:reaction_type existing) (:reaction_type reaction))
        (next.jdbc/execute!
         *db*
         ["DELETE FROM reactions 
            WHERE message_id = ? AND user_identifier = ?"
          (:message_id reaction)
          (:user_identifier reaction)])
        ;; If different reaction type, update it
        (next.jdbc/execute!
         *db*
         ["UPDATE reactions 
            SET reaction_type = ? 
            WHERE message_id = ? AND user_identifier = ?"
          (:reaction_type reaction)
          (:message_id reaction)
          (:user_identifier reaction)]))
      ;; No existing reaction, create new one
      (next.jdbc/execute!
       *db*
       ["INSERT INTO reactions (message_id, user_identifier, reaction_type)
          VALUES (?, ?, ?)"
        (:message_id reaction)
        (:user_identifier reaction)
        (:reaction_type reaction)]))))


(defn clear-reactions! []
  (next.jdbc/execute!
   *db*
   ["DELETE FROM reactions"]))

(extend-protocol next.jdbc.result-set/ReadableColumn
  java.sql.Timestamp
  (read-column-by-label [^java.sql.Timestamp v _]
    (.toLocalDateTime v))
  (read-column-by-index [^java.sql.Timestamp v _2 _3]
    (.toLocalDateTime v))
  java.sql.Date
  (read-column-by-label [^java.sql.Date v _]
    (.toLocalDate v))
  (read-column-by-index [^java.sql.Date v _2 _3]
    (.toLocalDate v))
  java.sql.Time
  (read-column-by-label [^java.sql.Time v _]
    (.toLocalTime v))
  (read-column-by-index [^java.sql.Time v _2 _3]
    (.toLocalTime v)))
