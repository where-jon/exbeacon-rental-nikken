# exbeacon-daidan-web

# --- !Ups

--1.
-- Table: ユーザマスタ

CREATE TABLE public.user_master (
	user_id serial NOT NULL,
	email text NOT NULL,
	"name" text NOT NULL,
	password text NOT NULL,
	place_id int4 NULL,
	current_place_id int4 NULL,
	active_flg bool NOT NULL DEFAULT true,
	updatetime timestamp NULL DEFAULT now(),
	permission int4 NOT NULL DEFAULT 0,
	CONSTRAINT user_master_pkey PRIMARY KEY (user_id)

)
--WITH (
--  OIDS=FALSE
--);
--COMMENT ON TABLE user_master IS 'ユーザマスタ';
--COMMENT ON COLUMN user_master.user_id IS 'ユーザID';
--COMMENT ON COLUMN user_master.email IS 'メールアドレス';
--COMMENT ON COLUMN user_master.name IS '名前';
--COMMENT ON COLUMN user_master.password IS 'パスワード';
--COMMENT ON COLUMN user_master.place_id IS '現場ID';
--COMMENT ON COLUMN user_master.current_place_id IS '現在の現場ID';
--COMMENT ON COLUMN user_master.active_flg IS '有効フラグ';
--COMMENT ON COLUMN user_master.updatetime IS 'データ更新日時';


--2.
-- Table: place_master (建築現場マスタ)

CREATE TABLE public.place_master (
	place_id serial NOT NULL,
	place_name text NOT NULL,
	status int4 NOT NULL DEFAULT 0,
	btx_api_url text NOT NULL DEFAULT ''::text,
	exb_telemetry_url text NOT NULL DEFAULT ''::text,
	gateway_telemetry_url text NOT NULL DEFAULT ''::text,
	cms_password text NOT NULL DEFAULT ''::text,
	active_flg bool NOT NULL DEFAULT true,
	updatetime timestamp NULL DEFAULT now(),
	CONSTRAINT place_master_pkey PRIMARY KEY (place_id)
)
--WITH (
--  OIDS=FALSE
--);
--COMMENT ON TABLE place_master IS '建築現場マスタ';
--COMMENT ON COLUMN place_master.place_id IS '現場ID';
--COMMENT ON COLUMN place_master.place_name IS '現場名';
--COMMENT ON COLUMN place_master.status IS '状態';
--COMMENT ON COLUMN place_master.btx_api_url IS 'Tx測位結果取得APIのURL';
--COMMENT ON COLUMN place_master.exb_telemetry_url IS 'EXBeaconテレメトリURL';
--COMMENT ON COLUMN place_master.gateway_telemetry_url IS 'EXGatewayテレメトリURL';
--COMMENT ON COLUMN place_master.cms_password IS '管理ページパスワード';
--COMMENT ON COLUMN place_master.active_flg IS '有効フラグ';
--COMMENT ON COLUMN place_master.updatetime IS 'データ更新日時';


--3.
-- Table: company_master (業者マスタ)

CREATE TABLE public.company_master (
	company_id serial NOT NULL,
	company_name text NOT NULL,
	note text NOT NULL DEFAULT ''::text,
	place_id int4 NOT NULL,
	active_flg bool NOT NULL DEFAULT true,
	updatetime timestamp NULL DEFAULT now(),
	CONSTRAINT company_master_pkey PRIMARY KEY (company_id)
)
--WITH (
--  OIDS=FALSE
--);
--COMMENT ON TABLE company_master IS '業者マスタ';
--COMMENT ON COLUMN company_master.company_id IS '業者ID';
--COMMENT ON COLUMN company_master.company_name IS '業者名';
--COMMENT ON COLUMN company_master.note IS '備考';
--COMMENT ON COLUMN company_master.place_id IS '現場ID';
--COMMENT ON COLUMN company_master.active_flg IS '有効フラグ';
--COMMENT ON COLUMN company_master.updatetime IS 'データ更新日時';


--4.
-- Table: floor_master (フロアマスタ)

CREATE TABLE public.floor_master (
	floor_id serial NOT NULL,
	floor_name text NOT NULL,
	display_order int4 NOT NULL,
	place_id int4 NOT NULL,
	active_flg bool NOT NULL DEFAULT true,
	updatetime timestamp NULL DEFAULT now(),
	floor_map_width int4 NOT NULL DEFAULT 0,
	floor_map_height int4 NOT NULL DEFAULT 0,
	floor_map_image varchar NOT NULL DEFAULT ''::character varying,
	CONSTRAINT floor_master_pkey PRIMARY KEY (floor_id)
)
--WITH (
--  OIDS=FALSE
--);
--COMMENT ON TABLE floor_master IS 'フロアマスタ';
--COMMENT ON COLUMN floor_master.floor_id IS 'フロアID';
--COMMENT ON COLUMN floor_master.floor_name IS 'フロア名';
--COMMENT ON COLUMN floor_master.display_order IS '表示順序';
--COMMENT ON COLUMN floor_master.floor_map_width IS 'フロアマップ横幅';
--COMMENT ON COLUMN floor_master.floor_map_height IS 'フロアマップ立幅';
--COMMENT ON COLUMN floor_master.floor_map_image IS 'フロアマップイメージ';
--COMMENT ON COLUMN floor_master.place_id IS '現場ID';
--COMMENT ON COLUMN floor_master.active_flg IS '有効フラグ';
--COMMENT ON COLUMN floor_master.updatetime IS 'データ更新日時';


--5.
-- Table: exb_master (EXBeaconマスタ)

CREATE TABLE public.exb_master (
	exb_id serial NOT NULL,
	exb_device_id int4 NOT NULL,
	exb_device_no int4 NOT NULL,
	exb_device_name text NOT NULL DEFAULT ''::text,
	exb_pos_name text NOT NULL DEFAULT ''::text,
	exb_pos_x text NOT NULL DEFAULT ''::text,
	exb_pos_y text NULL DEFAULT ''::text,
	exb_view_flag bool NOT NULL DEFAULT false,
	view_type_id int4 NOT NULL DEFAULT 1,
	view_tx_size int4 NOT NULL DEFAULT 35,
	view_tx_margin int4 NOT NULL DEFAULT '-1'::integer,
	view_tx_count int4 NOT NULL DEFAULT 1,
	place_id int4 NOT NULL,
	floor_id int4 NOT NULL,
	updatetime timestamp NULL DEFAULT now(),
	CONSTRAINT exb_master_pkey PRIMARY KEY (exb_id)
)
--WITH (
--  OIDS=FALSE
--);
--COMMENT ON TABLE exb_master IS 'EXBeaconマスタ';
--COMMENT ON COLUMN exb_master.exb_device_id IS 'EXBeaconデバイスID';
--COMMENT ON COLUMN exb_master.place_id IS '現場ID';
--COMMENT ON COLUMN exb_master.floor_id IS 'フロアID';
--COMMENT ON COLUMN exb_master.exb_id IS 'EXBeaconID';
--COMMENT ON COLUMN exb_master.exb_device_name IS 'EXBeaconデバイス名';
--COMMENT ON COLUMN exb_master.exb_pos_x IS 'EXBeacon設置 X座標';
--COMMENT ON COLUMN exb_master.exb_pos_y IS 'EXBeacon設置 Y座標';
--COMMENT ON COLUMN exb_master.exb_view_flag IS 'EXBeacon表示フラグ';
--COMMENT ON COLUMN exb_master.view_type_id IS 'TX表示種別ID';
--COMMENT ON COLUMN exb_master.view_tx_size IS 'TX表示サイズ';
--COMMENT ON COLUMN exb_master.view_tx_margin IS 'TX表示重複幅(予測)';
--COMMENT ON COLUMN exb_master.view_tx_count IS 'TX表示個数(予測)';
--COMMENT ON COLUMN exb_master.updatetime IS 'データ更新日時';

--6.
-- Table:item_car_master (作業車・立馬マスタ)

CREATE TABLE public.item_car_master (
	item_car_id serial NOT NULL,
	item_type_id int4 NOT NULL,
	item_car_btx_id int4 NOT NULL,
	item_car_key_btx_id int4 NOT NULL DEFAULT '-1'::integer,
	item_car_no text NOT NULL,
	item_car_name text NOT NULL,
	note text NOT NULL DEFAULT ''::text,
	place_id int4 NOT NULL,
	active_flg bool NOT NULL DEFAULT true,
	updatetime timestamp NULL DEFAULT now(),
	CONSTRAINT item_car_master2_pkey PRIMARY KEY (item_car_id)
)
--WITH (
--  OIDS=FALSE
--);
--COMMENT ON TABLE item_car_master IS '作業車・立馬マスタ';
--COMMENT ON COLUMN item_car_master.item_car_id IS '作業車・立馬ID';
--COMMENT ON COLUMN item_car_master.item_car_id IS '仮設材種別ID';
--COMMENT ON COLUMN item_car_master.note IS '備考';
--COMMENT ON COLUMN item_car_master.item_car_no IS '作業車・立馬番号';
--COMMENT ON COLUMN item_car_master.item_car_name IS '作業車・立馬名';
--COMMENT ON COLUMN item_car_master.item_car_btx_id IS '作業車・立馬TxビーコンID';
--COMMENT ON COLUMN item_car_master.item_car_key_btx_id IS '作業車鍵TxビーコンID';
--COMMENT ON COLUMN item_car_master.place_id IS '現場ID';
--COMMENT ON COLUMN item_car_master.active_flg IS '有効フラグ';
--COMMENT ON COLUMN item_car_master.updatetime IS 'データ更新日時';

--7.
-- Table: item_other_master (その他仮設材マスタ)

CREATE TABLE public.item_other_master (
	item_other_id serial NOT NULL,
	item_type_id int4 NOT NULL,
	item_other_btx_id int4 NOT NULL,
	item_other_no text NOT NULL,
	item_other_name text NOT NULL,
	note text NOT NULL DEFAULT ''::text,
	place_id int4 NOT NULL,
	active_flg bool NOT NULL DEFAULT true,
	updatetime timestamp NULL DEFAULT now(),
	CONSTRAINT item_other_master_pkey PRIMARY KEY (item_other_id)
)
--WITH (
--  OIDS=FALSE
--);
--COMMENT ON TABLE item_other_master IS '仮設材種別マスタ';
--COMMENT ON COLUMN item_other_master.item_other_id IS '仮設材種別ID';
--COMMENT ON COLUMN item_other_master.item_other_name IS '仮設材種別名';
--COMMENT ON COLUMN item_other_master.note IS '備考';
--COMMENT ON COLUMN item_other_master.item_type_id IS '仮設材種別ID';
--COMMENT ON COLUMN item_other_master.item_other_btx_id IS 'その他仮設材TxビーコンID';
--COMMENT ON COLUMN item_other_master.item_other_no IS 'その他仮設材番号';
--COMMENT ON COLUMN item_other_master.place_id IS '現場ID';
--COMMENT ON COLUMN item_other_master.active_flg IS '有効フラグ';
--COMMENT ON COLUMN item_other_master.updatetime IS 'データ更新日時';

--8.
-- Table: item_type (仮設材種別)

CREATE TABLE public.item_type (
	item_type_id int4 NOT NULL,
	item_type_name text NOT NULL,
	item_type_category text NOT NULL,
	item_type_icon_color text NOT NULL,
	item_type_text_color text NOT NULL,
	item_type_row_color text NOT NULL,
	note text NOT NULL DEFAULT ''::text,
	place_id int4 NOT NULL,
	active_flg bool NOT NULL DEFAULT true,
	updatetime timestamp NULL DEFAULT now(),
)
--WITH (
--  OIDS=FALSE
--);
--COMMENT ON TABLE item_type IS '仮設材種';
--COMMENT ON COLUMN item_type.item_type_id IS '仮設材種別ID';
--COMMENT ON COLUMN item_type.item_type_name IS '仮設材種別名';
--COMMENT ON COLUMN item_type.item_type_category IS '仮設材カテゴリー名';
--COMMENT ON COLUMN item_type.item_type_text_color IS '仮設材種別アイコンカラー';
--COMMENT ON COLUMN item_type.item_type_category IS '仮設材種別文字カラー';
--COMMENT ON COLUMN item_type.item_type_row_color IS '仮設材種別行カラー';
--COMMENT ON COLUMN item_type.note IS '備考';
--COMMENT ON COLUMN item_type.place_id IS '現場ID';
--COMMENT ON COLUMN item_type.active_flg IS '有効フラグ';
--COMMENT ON COLUMN item_type.updatetime IS 'データ更新日時';


--9.
-- Table: view_type (TX表示種別)

CREATE TABLE public.view_type (
	view_type_id serial NOT NULL,
	view_type_name text NOT NULL,
	note text NOT NULL DEFAULT ''::text,
	place_id int4 NOT NULL,
	active_flg bool NOT NULL DEFAULT true,
	updatetime timestamp NULL DEFAULT now(),
	CONSTRAINT view_type_pkey PRIMARY KEY (view_type_id)
)
--WITH (
--  OIDS=FALSE
--);
--COMMENT ON TABLE view_type IS 'TX表示種別';
--COMMENT ON COLUMN view_type.view_type_id IS 'TX表示種別ID';
--COMMENT ON COLUMN view_type.view_type_name IS 'TX表示種別名';
--COMMENT ON COLUMN view_type.note IS '備考';
--COMMENT ON COLUMN view_type.place_id IS '現場ID';
--COMMENT ON COLUMN view_type.active_flg IS '有効フラグ';
--COMMENT ON COLUMN view_type.updatetime IS 'データ更新日時';



--10.
-- Table: work_type (働き方種別)

CREATE TABLE public.work_type (
	work_type_id serial NOT NULL,
	work_type_name text NOT NULL,
	note text NOT NULL DEFAULT ''::text,
	place_id int4 NOT NULL,
	active_flg bool NOT NULL DEFAULT true,
	updatetime timestamp NULL DEFAULT now(),
	CONSTRAINT work_type_pkey PRIMARY KEY (work_type_id)
)
--WITH (
--  OIDS=FALSE
--);
--COMMENT ON TABLE work_type IS '働き方種別';
--COMMENT ON COLUMN work_type.work_type_id IS '働き方種別ID';
--COMMENT ON COLUMN work_type.work_type_name IS '働き方種別名';
--COMMENT ON COLUMN work_type.note IS '備考';
--COMMENT ON COLUMN work_type.place_id IS '現場ID';
--COMMENT ON COLUMN work_type.active_flg IS '有効フラグ';
--COMMENT ON COLUMN work_type.updatetime IS 'データ更新日時';


--11.
-- Table: reserve_table (予約テーブル)

CREATE TABLE public.reserve_table (
	reserve_id serial NOT NULL,
	item_type_id int4 NOT NULL,
	item_id int4 NOT NULL,
	floor_id int4 NOT NULL,
	place_id int4 NOT NULL,
	company_id int4 NOT NULL,
	reserve_start_date date NOT NULL,
	reserve_end_date date NOT NULL,
	active_flg bool NOT NULL DEFAULT true,
	updatetime timestamp NULL DEFAULT now(),
	work_type_id int4 NOT NULL,
	CONSTRAINT reserve_table_new_pkey PRIMARY KEY (reserve_id)
)
--WITH (
--  OIDS=FALSE
--);
--COMMENT ON TABLE reserve_table IS '予約テーブル';
--COMMENT ON COLUMN reserve_table.reserve_id IS '予約ID';
--COMMENT ON COLUMN reserve_table.item_id IS '仮設材ID';
--COMMENT ON COLUMN reserve_table.floor_id IS 'フロアID';
--COMMENT ON COLUMN reserve_table.company_id IS '業者ID';
--COMMENT ON COLUMN reserve_table.reserve_start_date IS '予約開始日';
--COMMENT ON COLUMN reserve_table.reserve_end_date IS '予約終了日';
--COMMENT ON COLUMN reserve_table.active_flg IS '有効フラグ';
--COMMENT ON COLUMN reserve_table.updatetime IS 'データ更新日時';


--12.
-- Table: item_log (TX位置ロぐバッチテーブル)

CREATE TABLE public.item_log (
	item_log_id serial NOT NULL,
	item_type_id int4 NOT NULL,
	item_id int4 NOT NULL,
	item_name text NOT NULL,
	item_btx_id int4 NOT NULL,
	item_car_key_btx_id int4 NOT NULL,
	reserve_flg bool NOT NULL DEFAULT false,
	reserve_start_date date,
	reserve_end_date date,
	working_flg bool NOT NULL DEFAULT false,
	finish_floor_id int4 NOT NULL,
	finish_floor_name text NOT NULL,
	finish_exb_id int4 NOT NULL,
	finish_exb_name text NOT NULL,
	finish_updatetime timestamp NOT NULL,
	company_id int4 NOT NULL,
	company_name text NOT NULL,
	place_id int4 NOT NULL,
	updatetime timestamp NULL DEFAULT now(),
	CONSTRAINT item_log_pkey PRIMARY KEY (item_log_id)
)
--WITH (
--  OIDS=FALSE
--);
--COMMENT ON TABLE item_log IS '位置ロぐバッチテーブル';
--COMMENT ON COLUMN item_log.item_log_id IS '仮設材ログID';
--COMMENT ON COLUMN item_log.item_type_id IS '仮設材種別ID';
--COMMENT ON COLUMN item_log.item_id IS '仮設材ID';
--COMMENT ON COLUMN item_log.item_name IS '仮設材名';
--COMMENT ON COLUMN item_log.item_btx_id IS '仮設材TxビーコンID';
--COMMENT ON COLUMN item_log.item_car_key_btx_id IS '作業車・立馬鍵TxビーコンID';
--COMMENT ON COLUMN item_log.reserve_flg IS '予約フラグ';
--COMMENT ON COLUMN item_log.reserve_start_date IS '予約開始日';
--COMMENT ON COLUMN item_log.reserve_end_date IS '予約終了日';
--COMMENT ON COLUMN item_log.working_flg IS '稼働フラグ';
--COMMENT ON COLUMN item_log.finish_floor_id IS 'フロアID';
--COMMENT ON COLUMN item_log.finish_floor_name IS '検知フロア';
--COMMENT ON COLUMN item_log.finish_exb_id IS '検知ExbeaconID';
--COMMENT ON COLUMN item_log.finish_exb_name IS '検知Exbeacon名';
--COMMENT ON COLUMN item_log.finish_updatetime IS '最終取得時間';
--COMMENT ON COLUMN item_log.company_id IS '業者ID';
--COMMENT ON COLUMN item_log.company_name IS '業者名';
--COMMENT ON COLUMN item_log.place_id IS '現場ID';
--COMMENT ON COLUMN item_log.updatetime IS 'データ更新日時';


INSERT INTO user_master
(email, "name", password, place_id, current_place_id, active_flg, updatetime, permission)
VALUES('exb@where123.jp', '開発者', '$2a$10$3Gyu0kfE2oaTHfQfg6saZ.9A3eCGBF.6enfUtA7jRv0Y9Qd3prGnK', 0, 1, true, now(), 4);

INSERT INTO user_master
(email, "name", password, place_id, current_place_id, active_flg, updatetime, permission)
VALUES('tenant@test.jp', 'テナント', '$2a$10$3Gyu0kfE2oaTHfQfg6saZ.9A3eCGBF.6enfUtA7jRv0Y9Qd3prGnK', 0, 1, true, now(), 4);


 -- place_master
INSERT INTO place_master (place_name, status, btx_api_url, exb_telemetry_url, gateway_telemetry_url, cms_password, active_flg, updatetime)
VALUES('渋谷現場', 0, 'https://ma4zj5805g.execute-api.ap-northeast-1.amazonaws.com/prod/beacon/position-kalman', 'https://ma4zj5805g.execute-api.ap-northeast-1.amazonaws.com/prod/telemetry/0', 'https://ma4zj5805g.execute-api.ap-northeast-1.amazonaws.com/prod/gateway/0'
, 'sibuya', true, now());


# --- !Downs
DROP TABLE user_master;
DROP TABLE place_master;
DROP TABLE company_master;
DROP TABLE floor_master;
DROP TABLE exb_master;
DROP TABLE item_car_master;
DROP TABLE item_other_master;

DROP TABLE item_type;
DROP TABLE view_type;
DROP TABLE work_type;

DROP TABLE reserve_table;

DROP TABLE item_log;

