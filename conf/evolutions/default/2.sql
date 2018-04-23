--
--# --- !Ups
INSERT INTO user_master (email, name, password, place_id, current_place_id) VALUES ('exb@where123.jp', 'exb', '$2a$10$3Gyu0kfE2oaTHfQfg6saZ.9A3eCGBF.6enfUtA7jRv0Y9Qd3prGnK', null, 1);
INSERT INTO user_master (email, name, password, place_id, current_place_id) VALUES ('takasago@example.jp', 'takasago', '$2a$10$oTBngKMZ36yDidDupqb5G.aQQaviFKS/xdEWnJbbH5jjYarGE0aV6', null, 1);
INSERT INTO user_master (email, name, password, place_id, current_place_id) VALUES ('tohoku@example.jp', 'tohoku', '$2a$10$zAqLi0NmVKs3Mhn1izD2EeJ3s2AEg888QvzJDu/AZUmjB119MSqhy', 1, 1);
--

 -- place_master
INSERT INTO place_master (place_name) VALUES ('東北医科薬科大学病院');

-- -- floor_master
--INSERT INTO floor_master (floor_name, display_order, place_id) VALUES ('8F', 1, 1);
--INSERT INTO floor_master (floor_name, display_order, place_id) VALUES ('7F', 2, 1);
--INSERT INTO floor_master (floor_name, display_order, place_id) VALUES ('6F', 3, 1);
--INSERT INTO floor_master (floor_name, display_order, place_id) VALUES ('5F', 4, 1);
--INSERT INTO floor_master (floor_name, display_order, place_id) VALUES ('4F', 5, 1);
--INSERT INTO floor_master (floor_name, display_order, place_id) VALUES ('3F', 6, 1);
--INSERT INTO floor_master (floor_name, display_order, place_id) VALUES ('2F', 7, 1);
--INSERT INTO floor_master (floor_name, display_order, place_id) VALUES ('1F', 8, 1);
--
-- -- company_master
--INSERT INTO company_master (company_name, place_id) VALUES ('ダクト1', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('ダクト1', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('ダクト2', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('配管1', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('配管2', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('保温1', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('保温2', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('計装1', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('計装2', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('多能1', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('多能2', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('ダクト2', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('ダクト3', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('ダクト4', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('ダクト5', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('ダクト6', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('ダクト7', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('ダクト8', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('ダクト9', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('ダクト10', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('ダクト11', 1);
--INSERT INTO company_master (company_name, place_id) VALUES ('ダクト12', 1);

--# --- !Downs
--
