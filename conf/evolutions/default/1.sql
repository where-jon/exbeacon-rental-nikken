# hitachiap-roommng-web

# --- !Ups

-- Table: user_master

CREATE TABLE user_master
(
  user_id serial NOT NULL,
  email text NOT NULL,
  name text NOT NULL,
  password text NOT NULL,
  permission integer NOT NULL DEFAULT 0,
  company_id integer,
  place_id integer,
  active_flg boolean NOT NULL DEFAULT true,
  updatetime timestamp without time zone DEFAULT now(),
  CONSTRAINT user_master_pkey PRIMARY KEY (user_id)
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE user_master IS 'ユーザマスタ';
COMMENT ON COLUMN user_master.user_id IS 'ユーザID';
COMMENT ON COLUMN user_master.email IS 'メールアドレス';
COMMENT ON COLUMN user_master.name IS '名前';
COMMENT ON COLUMN user_master.password IS 'パスワード';
COMMENT ON COLUMN user_master.permission IS '権限値';
COMMENT ON COLUMN user_master.company_id IS '業者ID';
COMMENT ON COLUMN user_master.place_id IS '現場ID';
COMMENT ON COLUMN user_master.active_flg IS '有効フラグ';
COMMENT ON COLUMN user_master.updatetime IS 'データ更新日時';

# --- !Downs

DROP TABLE user_master;


