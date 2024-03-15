ALTER TABLE move ADD user_id BIGINT;
ALTER TABLE move ADD CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES account (id);