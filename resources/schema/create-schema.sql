CREATE SEQUENCE user_id_seq;
 
CREATE TABLE t_user (
   id BIGINT PRIMARY KEY DEFAULT nextval('user_id_seq'),
   jid VARCHAR(300)
);

CREATE INDEX user_jid_index ON t_user(jid);
 
CREATE SEQUENCE item_id_seq;
 
CREATE TABLE item (
   id BIGINT PRIMARY KEY DEFAULT nextval('item_id_seq'),
   jid VARCHAR(300),
   title VARCHAR(300),
   description VARCHAR(500)
);

CREATE INDEX item_jid_index ON item(jid);
 
CREATE TABLE taste_preferences (
   user_id BIGINT NOT NULL,
   item_id BIGINT NOT NULL,
   PRIMARY KEY (user_id, item_id)
);

CREATE INDEX taste_preferences_user_id_index ON taste_preferences (user_id);
CREATE INDEX taste_preferences_item_id_index ON taste_preferences (item_id);

CREATE TABLE taste_item_similarity (
	item_id_a BIGINT NOT NULL,
	item_id_b BIGINT NOT NULL,
	similarity FLOAT NOT NULL,
	PRIMARY KEY (item_id_a, item_id_b)
);