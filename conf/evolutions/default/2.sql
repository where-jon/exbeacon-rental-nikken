--
--# --- !Ups
INSERT INTO user_master
(email, "name", password, place_id, current_place_id, active_flg, updatetime, permission)
VALUES('exb@where123.jp', '開発者', '$2a$10$3Gyu0kfE2oaTHfQfg6saZ.9A3eCGBF.6enfUtA7jRv0Y9Qd3prGnK', null, 1, true, now(), 4);

INSERT INTO user_master
(email, "name", password, place_id, current_place_id, active_flg, updatetime, permission)
VALUES('tenant@test.jp', 'テナント', '$2a$10$3Gyu0kfE2oaTHfQfg6saZ.9A3eCGBF.6enfUtA7jRv0Y9Qd3prGnK', null, 1, true, now(), 4);

--現場責任者
INSERT INTO user_master
(email, "name", password, place_id, current_place_id, active_flg, updatetime, permission)
VALUES('kanri', '現場責任者', '$2a$10$3Gyu0kfE2oaTHfQfg6saZ.9A3eCGBF.6enfUtA7jRv0Y9Qd3prGnK', 1, 1, true, now(), 3);


 -- place_master
INSERT INTO place_master (place_name, status, btx_api_url, exb_telemetry_url, gateway_telemetry_url, cms_password, active_flg, updatetime)
VALUES('渋谷現場', 0, 'https://ma4zj5805g.execute-api.ap-northeast-1.amazonaws.com/prod/beacon/position-kalman', 'https://ma4zj5805g.execute-api.ap-northeast-1.amazonaws.com/prod/telemetry/0', 'https://ma4zj5805g.execute-api.ap-northeast-1.amazonaws.com/prod/gateway/0'
, 'cmsPassWord', true, now());

-- work_type
INSERT INTO work_type (work_type_name, note, place_id, active_flg, updatetime) values ( '午前','note',1,'TRUE',now());
INSERT INTO work_type (work_type_name, note, place_id, active_flg, updatetime) values ( '午後','note',1,'TRUE',now());
INSERT INTO work_type (work_type_name, note, place_id, active_flg, updatetime) values ( '終日','note',1,'TRUE',now());

-- view_type
INSERT INTO view_type (view_type_name, note, place_id, active_flg, updatetime)  values ( 'Mx2','note',1,'TRUE',now());
INSERT INTO view_type (view_type_name, note, place_id, active_flg, updatetime)  values ( 'Mx3','note',1,'TRUE',now());
INSERT INTO view_type (view_type_name, note, place_id, active_flg, updatetime)  values ( 'Mx4','note',1,'TRUE',now());
INSERT INTO view_type (view_type_name, note, place_id, active_flg, updatetime)  values ( '2x2','note',1,'TRUE',now());
INSERT INTO view_type (view_type_name, note, place_id, active_flg, updatetime)  values ( '3x3','note',1,'TRUE',now());
INSERT INTO view_type (view_type_name, note, place_id, active_flg, updatetime)  values ( '2x5','note',1,'TRUE',now());
INSERT INTO view_type (view_type_name, note, place_id, active_flg, updatetime)  values ( '2x2_circle','note',1,'TRUE',now());
INSERT INTO view_type (view_type_name, note, place_id, active_flg, updatetime)  values ( '3x3_table','note',1,'TRUE',now());
INSERT INTO view_type (view_type_name, note, place_id, active_flg, updatetime)  values ( '1x1_right','note',1,'TRUE',now());
INSERT INTO view_type (view_type_name, note, place_id, active_flg, updatetime)  values ( 'all_circle','note',1,'TRUE',now());

--# --- !Downs
--
