-- =============== KITCHEN (irms_kitchen) ===============
\c irms_kitchen

-- Kitchen tickets
-- Order 002 PENDING -> ticket PENDING
-- Order 003 COOKING -> ticket PREPARING
-- Order 004 READY_TO_SERVE -> ticket READY_TO_SERVE
-- Order 005 SERVED -> ticket SERVED
-- Order 006 (takeaway, COMPLETED) -> ticket SERVED
-- Order 007 (COMPLETED) -> ticket SERVED
INSERT INTO kitchen_tickets (id, order_id, table_id, status, expected_ready_time, created_at, updated_at) VALUES
  ('99999999-0000-0000-0000-000000000002','ffffffff-0000-0000-0000-000000000002','cccccccc-0000-0000-0000-000000000003','PENDING',         NOW() + INTERVAL '15 minutes', NOW() - INTERVAL '15 minutes', NOW() - INTERVAL '15 minutes'),
  ('99999999-0000-0000-0000-000000000003','ffffffff-0000-0000-0000-000000000003','cccccccc-0000-0000-0000-000000000004','PREPARING',       NOW() + INTERVAL '5 minutes',  NOW() - INTERVAL '20 minutes', NOW() - INTERVAL '10 minutes'),
  ('99999999-0000-0000-0000-000000000004','ffffffff-0000-0000-0000-000000000004','cccccccc-0000-0000-0000-000000000006','READY_TO_SERVE',  NOW() - INTERVAL '5 minutes',  NOW() - INTERVAL '50 minutes', NOW() - INTERVAL '5 minutes'),
  ('99999999-0000-0000-0000-000000000005','ffffffff-0000-0000-0000-000000000005','cccccccc-0000-0000-0000-000000000010','SERVED',          NOW() - INTERVAL '60 minutes', NOW() - INTERVAL '85 minutes', NOW() - INTERVAL '50 minutes'),
  ('99999999-0000-0000-0000-000000000006','ffffffff-0000-0000-0000-000000000006', NULL,                                  'SERVED',          NOW() - INTERVAL '2 hours 45 minutes', NOW() - INTERVAL '3 hours', NOW() - INTERVAL '2 hours 30 minutes'),
  ('99999999-0000-0000-0000-000000000007','ffffffff-0000-0000-0000-000000000007','cccccccc-0000-0000-0000-000000000001','SERVED',          NOW() - INTERVAL '4 hours 30 minutes', NOW() - INTERVAL '5 hours', NOW() - INTERVAL '4 hours 30 minutes')
ON CONFLICT DO NOTHING;

-- Kitchen ticket items
-- Ticket 002 (PENDING)
INSERT INTO kitchen_ticket_items (id, ticket_id, menu_item_id, menu_item_name, quantity, station, status, notes, created_at, updated_at) VALUES
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000002','bbbbbbbb-0000-0000-0000-000000000010','Gà nướng mật ong',    1,'GRILL',  'PENDING','Không cay', NOW() - INTERVAL '15 minutes', NOW() - INTERVAL '15 minutes'),
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000002','bbbbbbbb-0000-0000-0000-000000000011','Mỳ Ý sốt bò bằm',     1,'GENERAL','PENDING', NULL,       NOW() - INTERVAL '15 minutes', NOW() - INTERVAL '15 minutes'),
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000002','bbbbbbbb-0000-0000-0000-000000000005','Salad Caesar',        1,'GENERAL','PENDING', NULL,       NOW() - INTERVAL '15 minutes', NOW() - INTERVAL '15 minutes'),
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000002','bbbbbbbb-0000-0000-0000-000000000023','Nước cam tươi',       1,'DRINK',  'PENDING', NULL,       NOW() - INTERVAL '15 minutes', NOW() - INTERVAL '15 minutes'),
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000002','bbbbbbbb-0000-0000-0000-000000000024','Cà phê đen',          1,'DRINK',  'PENDING', NULL,       NOW() - INTERVAL '15 minutes', NOW() - INTERVAL '15 minutes');

-- Ticket 003 (PREPARING)
INSERT INTO kitchen_ticket_items (id, ticket_id, menu_item_id, menu_item_name, quantity, station, status, notes, created_at, updated_at) VALUES
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000003','bbbbbbbb-0000-0000-0000-000000000008','Bò bít tết',          1,'GRILL',  'COOKING','Medium rare', NOW() - INTERVAL '20 minutes', NOW() - INTERVAL '15 minutes'),
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000003','bbbbbbbb-0000-0000-0000-000000000009','Cá hồi áp chảo',      1,'GRILL',  'COOKING', NULL,         NOW() - INTERVAL '20 minutes', NOW() - INTERVAL '15 minutes'),
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000003','bbbbbbbb-0000-0000-0000-000000000016','Bánh mì bơ tỏi',      2,'GENERAL','READY',  NULL,         NOW() - INTERVAL '20 minutes', NOW() - INTERVAL '10 minutes'),
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000003','bbbbbbbb-0000-0000-0000-000000000026','Bia Tiger',           2,'DRINK',  'READY',  NULL,         NOW() - INTERVAL '20 minutes', NOW() - INTERVAL '18 minutes');

-- Ticket 004 (READY_TO_SERVE)
INSERT INTO kitchen_ticket_items (id, ticket_id, menu_item_id, menu_item_name, quantity, station, status, notes, created_at, updated_at) VALUES
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000004','bbbbbbbb-0000-0000-0000-000000000012','Pizza hải sản',       2,'GENERAL','READY', NULL, NOW() - INTERVAL '50 minutes', NOW() - INTERVAL '5 minutes'),
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000004','bbbbbbbb-0000-0000-0000-000000000010','Gà nướng mật ong',    2,'GRILL',  'READY', NULL, NOW() - INTERVAL '50 minutes', NOW() - INTERVAL '5 minutes'),
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000004','bbbbbbbb-0000-0000-0000-000000000018','Tiramisu',            2,'DESSERT','READY', NULL, NOW() - INTERVAL '50 minutes', NOW() - INTERVAL '8 minutes'),
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000004','bbbbbbbb-0000-0000-0000-000000000022','Coca Cola',           4,'DRINK',  'READY', NULL, NOW() - INTERVAL '50 minutes', NOW() - INTERVAL '48 minutes'),
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000004','bbbbbbbb-0000-0000-0000-000000000023','Nước cam tươi',       2,'DRINK',  'READY', NULL, NOW() - INTERVAL '50 minutes', NOW() - INTERVAL '46 minutes');

-- Ticket 005 (SERVED)
INSERT INTO kitchen_ticket_items (id, ticket_id, menu_item_id, menu_item_name, quantity, station, status, notes, created_at, updated_at) VALUES
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000005','bbbbbbbb-0000-0000-0000-000000000013','Cơm chiên Dương Châu',2,'GENERAL','READY', NULL, NOW() - INTERVAL '85 minutes', NOW() - INTERVAL '60 minutes'),
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000005','bbbbbbbb-0000-0000-0000-000000000014','Phở bò tái',          2,'GENERAL','READY', NULL, NOW() - INTERVAL '85 minutes', NOW() - INTERVAL '60 minutes'),
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000005','bbbbbbbb-0000-0000-0000-000000000003','Súp cua bóng cá',     2,'GENERAL','READY', NULL, NOW() - INTERVAL '85 minutes', NOW() - INTERVAL '70 minutes'),
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000005','bbbbbbbb-0000-0000-0000-000000000025','Trà đào cam sả',      2,'DRINK',  'READY', NULL, NOW() - INTERVAL '85 minutes', NOW() - INTERVAL '78 minutes');

-- Ticket 006
INSERT INTO kitchen_ticket_items (id, ticket_id, menu_item_id, menu_item_name, quantity, station, status, notes, created_at, updated_at) VALUES
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000006','bbbbbbbb-0000-0000-0000-000000000012','Pizza hải sản',       1,'GENERAL','READY', NULL, NOW() - INTERVAL '3 hours', NOW() - INTERVAL '2 hours 40 minutes'),
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000006','bbbbbbbb-0000-0000-0000-000000000004','Khoai tây chiên',     1,'FRYER',  'READY', NULL, NOW() - INTERVAL '3 hours', NOW() - INTERVAL '2 hours 50 minutes');

-- Ticket 007
INSERT INTO kitchen_ticket_items (id, ticket_id, menu_item_id, menu_item_name, quantity, station, status, notes, created_at, updated_at) VALUES
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000007','bbbbbbbb-0000-0000-0000-000000000008','Bò bít tết',          1,'GRILL',  'READY','Well done', NOW() - INTERVAL '5 hours', NOW() - INTERVAL '4 hours 35 minutes'),
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000007','bbbbbbbb-0000-0000-0000-000000000019','Kem socola',          1,'DESSERT','READY', NULL,       NOW() - INTERVAL '5 hours', NOW() - INTERVAL '4 hours 35 minutes'),
  (gen_random_uuid(),'99999999-0000-0000-0000-000000000007','bbbbbbbb-0000-0000-0000-000000000026','Bia Tiger',           2,'DRINK',  'READY', NULL,       NOW() - INTERVAL '5 hours', NOW() - INTERVAL '4 hours 50 minutes');
