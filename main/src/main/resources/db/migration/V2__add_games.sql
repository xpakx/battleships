CREATE TABLE game (
   id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,

   accepted BOOLEAN DEFAULT FALSE NOT NULL,
   rejected BOOLEAN DEFAULT FALSE NOT NULL,
   type SMALLINT NOT NULL,
   ruleset SMALLINT NOT NULL,
   ai_type SMALLINT NOT NULL,

   finished BOOLEAN DEFAULT FALSE NOT NULL,
   won BOOLEAN DEFAULT FALSE NOT NULL,
   lost BOOLEAN DEFAULT FALSE NOT NULL,
   drawn BOOLEAN DEFAULT FALSE NOT NULL,

   started_at TIME,
   last_move_at TIME,

   user_current_state VARCHAR(255) NOT NULL,
   opponent_current_state VARCHAR(255) NOT NULL,
   user_ships VARCHAR(255) NOT NULL,
   opponent_ships VARCHAR(255) NOT NULL,

   user_starts BOOLEAN NOT NULL,
   user_turn BOOLEAN NOT NULL,

   user_id BIGINT NOT NUll,
   opponent_id BIGINT,
   CONSTRAINT pk_game PRIMARY KEY (id),
   CONSTRAINT fk_user_id
      FOREIGN KEY(user_id)
      REFERENCES account(id),
   CONSTRAINT fk_opponent_id
      FOREIGN KEY(opponent_id)
      REFERENCES account(id)
);
