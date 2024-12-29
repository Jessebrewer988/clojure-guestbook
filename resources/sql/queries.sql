-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(id, first_name, last_name, email, pass)
VALUES (:id, :first_name, :last_name, :email, :pass)

-- :name update-user! :! :n
-- :doc update an existing user record
UPDATE users
SET first_name = :first_name, last_name = :last_name, email = :email
WHERE id = :id

-- :name get-user :? :1
-- :doc retrieve a user given the id.
SELECT * FROM users
WHERE id = :id

-- :name save-message! :! :n
-- :doc creates a new message
INSERT INTO guestbook
(name, message, timestamp)
VALUES (:name, :message, :timestamp)

-- :name get-messages :? :*
-- :doc selects all available messages
SELECT * FROM guestbook

CREATE INDEX idx_reactions_message ON reactions(message_id);
-- :name save-reaction! :! :n
-- :doc creates a new reaction
INSERT INTO reactions
(message_id, reaction_type, user_identifier)
VALUES (:message_id, :reaction_type, :user_identifier)

-- :name get-message-reactions :? :*
-- :doc gets all reactions for a message
SELECT reaction_type, COUNT(*) as count 
FROM reactions 
WHERE message_id = :message_id 
GROUP BY reaction_type

-- :name get-messages :? :*
-- :doc selects all available messages with their reactions
SELECT m.*,
  (SELECT COUNT(*) FROM reactions r 
   WHERE r.message_id = m.id AND r.reaction_type = 'thumbsup') as thumbs_up_count,
  (SELECT COUNT(*) FROM reactions r 
   WHERE r.message_id = m.id AND r.reaction_type = 'heart') as heart_count,
  (SELECT COUNT(*) FROM reactions r 
   WHERE r.message_id = m.id AND r.reaction_type = 'thumbsdown') as thumbs_down_count,
  (SELECT COUNT(*) > 0 FROM reactions r 
   WHERE r.message_id = m.id AND r.reaction_type = 'thumbsup' AND r.user_identifier = :user_identifier) as user_thumbs_up,
  (SELECT COUNT(*) > 0 FROM reactions r 
   WHERE r.message_id = m.id AND r.reaction_type = 'heart' AND r.user_identifier = :user_identifier) as user_heart,
  (SELECT COUNT(*) > 0 FROM reactions r 
   WHERE r.message_id = m.id AND r.reaction_type = 'thumbsdown' AND r.user_identifier = :user_identifier) as user_thumbs_down
FROM guestbook m
ORDER BY m.timestamp DESC

-- :name delete-reaction! :! :n
-- :doc removes a reaction
DELETE FROM reactions 
WHERE message_id = :message_id 
AND user_identifier = :user_identifier
AND reaction_type = :reaction_type

-- :name reaction-exists? :? :1
-- :doc check if a reaction exists
SELECT reaction_type FROM reactions 
WHERE message_id = :message_id 
AND user_identifier = :user_identifier;

-- :name get-all-reactions :? :*
-- :doc get all reactions for debugging
SELECT * FROM reactions

-- :name clear-reactions! :! :n
-- :doc delete all reactions from the table
DELETE FROM reactions;