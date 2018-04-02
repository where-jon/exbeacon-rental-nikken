# hitachiap-roommng-web

# --- !Ups

-- Table: reserve_table (予約テーブル)

CREATE TABLE reserve_table
(
  reserve_id serial NOT NULL,
  car_id integer NOT NULL,
  floor_id integer NOT NULL,
  company_id integer NOT NULL,
  reserve_date date NOT NULL,
  active_flg boolean NOT NULL DEFAULT true,
  updatetime timestamp without time zone DEFAULT now(),
  CONSTRAINT reserve_table_pkey PRIMARY KEY (reserve_id)
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE reserve_table IS '予約テーブル';
COMMENT ON COLUMN reserve_table.reserve_id IS '予約ID';
COMMENT ON COLUMN reserve_table.car_id IS '作業車ID';
COMMENT ON COLUMN reserve_table.floor_id IS 'フロアID';
COMMENT ON COLUMN reserve_table.company_id IS '業者ID';
COMMENT ON COLUMN reserve_table.reserve_date IS '予約日';
COMMENT ON COLUMN reserve_table.active_flg IS '有効フラグ';
COMMENT ON COLUMN reserve_table.updatetime IS 'データ更新日時';

-- Table: exb_master (EXBeaconマスタ)

CREATE TABLE exb_master
(
  exb_device_id text NOT NULL,
  floor_id integer NOT NULL,
  CONSTRAINT exb_master_pkey PRIMARY KEY (exb_device_id)
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE exb_master IS 'EXBeaconマスタ';
COMMENT ON COLUMN exb_master.exb_device_id IS 'EXBeaconデバイスID';
COMMENT ON COLUMN exb_master.floor_id IS 'フロアID';

-- Table: btx_master (Txビーコンマスタ)

CREATE TABLE btx_master
(
  btx_id integer NOT NULL,
  place_id integer NOT NULL,
  active_flg boolean NOT NULL DEFAULT true,
  updatetime timestamp without time zone DEFAULT now(),
  CONSTRAINT btx_master_pkey PRIMARY KEY (btx_id, place_id)
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE btx_master IS 'Txビーコンマスタ';
COMMENT ON COLUMN btx_master.btx_id IS 'TxビーコンID';
COMMENT ON COLUMN btx_master.place_id IS '現場ID';
COMMENT ON COLUMN btx_master.active_flg IS '有効フラグ';
COMMENT ON COLUMN btx_master.updatetime IS 'データ更新日時';


-- Table: item_table (仮設材テーブル)

CREATE TABLE item_table
(
  item_id serial NOT NULL,
  item_no text NOT NULL,
  item_kind_id integer NOT NULL,
  item_btx_id integer NOT NULL,
  active_flg boolean NOT NULL DEFAULT true,
  updatetime timestamp without time zone DEFAULT now(),
  CONSTRAINT item_table_pkey PRIMARY KEY (item_id)
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE item_table IS '仮設材テーブル';
COMMENT ON COLUMN item_table.item_id IS '仮設材ID';
COMMENT ON COLUMN item_table.item_no IS '仮設材管理No';
COMMENT ON COLUMN item_table.item_kind_id IS '仮設材種別ID';
COMMENT ON COLUMN item_table.item_btx_id IS '仮設材TxビーコンID';
COMMENT ON COLUMN item_table.active_flg IS '有効フラグ';
COMMENT ON COLUMN item_table.updatetime IS 'データ更新日時';

-- Table: item_kind_master (仮設材種別マスタ)

CREATE TABLE item_kind_master
(
  item_kind_id serial NOT NULL,
  item_kind_name text NOT NULL,
  note text NOT NULL DEFAULT cast('' as text),
  place_id integer NOT NULL,
  active_flg boolean NOT NULL DEFAULT true,
  updatetime timestamp without time zone DEFAULT now(),
  CONSTRAINT item_kind_master_pkey PRIMARY KEY (item_kind_id)
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE item_kind_master IS '仮設材種別マスタ';
COMMENT ON COLUMN item_kind_master.item_kind_id IS '仮設材種別ID';
COMMENT ON COLUMN item_kind_master.item_kind_name IS '仮設材種別名';
COMMENT ON COLUMN item_kind_master.note IS '備考';
COMMENT ON COLUMN item_kind_master.place_id IS '現場ID';
COMMENT ON COLUMN item_kind_master.active_flg IS '有効フラグ';
COMMENT ON COLUMN item_kind_master.updatetime IS 'データ更新日時';

-- Table: car_master (作業車マスタ)

CREATE TABLE car_master
(
  car_id serial NOT NULL,
  car_no text NOT NULL,
  car_name text NOT NULL,
  car_btx_id integer NOT NULL,
  car_key_btx_id integer NOT NULL,
  place_id integer NOT NULL,
  active_flg boolean NOT NULL DEFAULT true,
  updatetime timestamp without time zone DEFAULT now(),
  CONSTRAINT car_master_pkey PRIMARY KEY (car_id)
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE car_master IS '作業車マスタ';
COMMENT ON COLUMN car_master.car_id IS '作業車ID';
COMMENT ON COLUMN car_master.car_no IS '作業車番号';
COMMENT ON COLUMN car_master.car_name IS '作業車名';
COMMENT ON COLUMN car_master.car_btx_id IS '作業車TxビーコンID';
COMMENT ON COLUMN car_master.car_key_btx_id IS '作業車鍵TxビーコンID';
COMMENT ON COLUMN car_master.place_id IS '現場ID';
COMMENT ON COLUMN car_master.active_flg IS '有効フラグ';
COMMENT ON COLUMN car_master.updatetime IS 'データ更新日時';

-- Table: company_master (業者マスタ)

CREATE TABLE company_master
(
  company_id serial NOT NULL,
  company_name text NOT NULL,
  note text NOT NULL DEFAULT cast('' as text),
  place_id integer NOT NULL,
  active_flg boolean NOT NULL DEFAULT true,
  updatetime timestamp without time zone DEFAULT now(),
  CONSTRAINT company_master_pkey PRIMARY KEY (company_id)
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE company_master IS '業者マスタ';
COMMENT ON COLUMN company_master.company_id IS '業者ID';
COMMENT ON COLUMN company_master.company_name IS '業者名';
COMMENT ON COLUMN company_master.note IS '備考';
COMMENT ON COLUMN company_master.place_id IS '現場ID';
COMMENT ON COLUMN company_master.active_flg IS '有効フラグ';
COMMENT ON COLUMN company_master.updatetime IS 'データ更新日時';

-- Table: floor_master (フロアマスタ)

CREATE TABLE floor_master
(
  floor_id serial NOT NULL,
  floor_name text NOT NULL,
  display_order integer NOT NULL,
  place_id integer NOT NULL,
  active_flg boolean NOT NULL DEFAULT true,
  updatetime timestamp without time zone DEFAULT now(),
  CONSTRAINT floor_master_pkey PRIMARY KEY (floor_id)
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE floor_master IS 'フロアマスタ';
COMMENT ON COLUMN floor_master.floor_id IS 'フロアID';
COMMENT ON COLUMN floor_master.floor_name IS 'フロア名';
COMMENT ON COLUMN floor_master.display_order IS '表示順序';
COMMENT ON COLUMN floor_master.place_id IS '現場ID';
COMMENT ON COLUMN floor_master.active_flg IS '有効フラグ';
COMMENT ON COLUMN floor_master.updatetime IS 'データ更新日時';

-- Table: place_master (建築現場マスタ)

CREATE TABLE place_master
(
  place_id serial NOT NULL,
  place_name text NOT NULL,
  status integer NOT NULL DEFAULT 0,
  btx_api_url text NOT NULL DEFAULT cast('' as text),
  exb_telemetry_url text NOT NULL DEFAULT cast('' as text),
  gateway_telemetry_url text NOT NULL DEFAULT cast('' as text),
  cms_password text NOT NULL DEFAULT cast('' as text),
  active_flg boolean NOT NULL DEFAULT true,
  updatetime timestamp without time zone DEFAULT now(),
  CONSTRAINT place_master_pkey PRIMARY KEY (place_id)
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE place_master IS '建築現場マスタ';
COMMENT ON COLUMN place_master.place_id IS '現場ID';
COMMENT ON COLUMN place_master.place_name IS '現場名';
COMMENT ON COLUMN place_master.status IS '状態';
COMMENT ON COLUMN place_master.btx_api_url IS 'Tx測位結果取得APIのURL';
COMMENT ON COLUMN place_master.exb_telemetry_url IS 'EXBeaconテレメトリURL';
COMMENT ON COLUMN place_master.gateway_telemetry_url IS 'EXGatewayテレメトリURL';
COMMENT ON COLUMN place_master.cms_password IS '管理ページパスワード';
COMMENT ON COLUMN place_master.active_flg IS '有効フラグ';
COMMENT ON COLUMN place_master.updatetime IS 'データ更新日時';

-- Table: ユーザマスタ

CREATE TABLE user_master
(
  user_id serial NOT NULL,
  email text NOT NULL,
  name text NOT NULL,
  password text NOT NULL,
  place_id integer,
  current_place_id integer DEFAULT NULL,
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
COMMENT ON COLUMN user_master.place_id IS '現場ID';
COMMENT ON COLUMN user_master.current_place_id IS '現在の現場ID';
COMMENT ON COLUMN user_master.active_flg IS '有効フラグ';
COMMENT ON COLUMN user_master.updatetime IS 'データ更新日時';

# --- !Downs
DROP TABLE place_master ;
DROP TABLE floor_master ;
DROP TABLE company_master;
DROP TABLE car_table ;
DROP TABLE item_kind_master;
DROP TABLE item_table;
DROP TABLE btx_master;
DROP TABLE exb_master;
DROP TABLE reserve_table;
DROP TABLE user_master;


