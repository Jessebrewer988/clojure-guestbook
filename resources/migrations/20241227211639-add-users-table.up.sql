CREATE TABLE guestbook
(id SERIAL PRIMARY KEY,
 name VARCHAR(30),
 message VARCHAR(200),
 timestamp TIMESTAMP);

 CREATE TABLE reactions (
    id SERIAL PRIMARY KEY,
    message_id INTEGER NOT NULL,
    reaction_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_identifier VARCHAR(100),
    FOREIGN KEY (message_id) REFERENCES guestbook(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_reaction UNIQUE(message_id, user_identifier)
);

CREATE INDEX idx_reactions_message ON reactions(message_id);