-- =============== ORDER (irms_order) ===============
\c irms_order

-- Orders
-- 001 DRAFT (chưa gửi bếp)
-- 002 PENDING (vừa gửi)
-- 003 COOKING (đang nấu)
-- 004 READY_TO_SERVE (xong, chờ phục vụ mang ra)
-- 005 SERVED (đã ra món)
-- 006 COMPLETED (đã thanh toán)
-- 007 COMPLETED (takeaway)
-- 008 CANCELLED
INSERT INTO orders (id, table_id, waiter_id, type, status, total_amount, special_note, created_at, updated_at) VALUES
  ('ffffffff-0000-0000-0000-000000000001','cccccccc-0000-0000-0000-000000000005','33333333-0000-0000-0000-000000000003','DINE_IN','DRAFT',         150000, NULL,                                            NOW() - INTERVAL '5 minutes',  NOW() - INTERVAL '5 minutes'),
  ('ffffffff-0000-0000-0000-000000000002','cccccccc-0000-0000-0000-000000000003','33333333-0000-0000-0000-000000000003','DINE_IN','PENDING',       450000, 'Khách hàng vội',                               NOW() - INTERVAL '20 minutes', NOW() - INTERVAL '15 minutes'),
  ('ffffffff-0000-0000-0000-000000000003','cccccccc-0000-0000-0000-000000000004','33333333-0000-0000-0000-000000000003','DINE_IN','COOKING',       700000, NULL,                                            NOW() - INTERVAL '40 minutes', NOW() - INTERVAL '20 minutes'),
  ('ffffffff-0000-0000-0000-000000000004','cccccccc-0000-0000-0000-000000000006','33333333-0000-0000-0000-000000000004','DINE_IN','READY_TO_SERVE', 985000, 'Bàn 6 người, kỷ niệm',                         NOW() - INTERVAL '55 minutes', NOW() - INTERVAL '5 minutes'),
  ('ffffffff-0000-0000-0000-000000000005','cccccccc-0000-0000-0000-000000000010','33333333-0000-0000-0000-000000000004','DINE_IN','SERVED',        515000, NULL,                                            NOW() - INTERVAL '85 minutes', NOW() - INTERVAL '20 minutes'),
  ('ffffffff-0000-0000-0000-000000000006', NULL,                                  '33333333-0000-0000-0000-000000000003','TAKEAWAY','COMPLETED',   240000, NULL,                                            NOW() - INTERVAL '3 hours',    NOW() - INTERVAL '2 hours 30 minutes'),
  ('ffffffff-0000-0000-0000-000000000007','cccccccc-0000-0000-0000-000000000001','33333333-0000-0000-0000-000000000004','DINE_IN','COMPLETED',     355000, NULL,                                            NOW() - INTERVAL '5 hours',    NOW() - INTERVAL '4 hours'),
  ('ffffffff-0000-0000-0000-000000000008','cccccccc-0000-0000-0000-000000000002','33333333-0000-0000-0000-000000000003','DINE_IN','CANCELLED',      85000, 'Khách đổi ý, huỷ',                             NOW() - INTERVAL '1 day',      NOW() - INTERVAL '23 hours')
ON CONFLICT DO NOTHING;

-- Order items
-- 001 DRAFT - Bàn T05 - Tổng 150,000
INSERT INTO order_items (id, order_id, menu_item_id, menu_item_name, quantity, price, status, note, created_at, updated_at) VALUES
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000001','bbbbbbbb-0000-0000-0000-000000000022','Coca Cola',           2, 25000,'PENDING', NULL, NOW(), NOW()),
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000001','bbbbbbbb-0000-0000-0000-000000000004','Khoai tây chiên',     1, 45000,'PENDING', NULL, NOW(), NOW()),
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000001','ffff0000-0000-0000-0000-000000000000','PLACEHOLDER',         0,     0,'CANCELLED', NULL, NOW(), NOW());

-- Đính chính: Xoá placeholder, recalc:
DELETE FROM order_items WHERE menu_item_name='PLACEHOLDER';

-- 002 PENDING - Bàn T03 - Tổng 450,000
INSERT INTO order_items (id, order_id, menu_item_id, menu_item_name, quantity, price, status, note, created_at, updated_at) VALUES
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000002','bbbbbbbb-0000-0000-0000-000000000010','Gà nướng mật ong',    1,175000,'PENDING','Không cay', NOW(), NOW()),
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000002','bbbbbbbb-0000-0000-0000-000000000011','Mỳ Ý sốt bò bằm',     1,135000,'PENDING', NULL,        NOW(), NOW()),
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000002','bbbbbbbb-0000-0000-0000-000000000005','Salad Caesar',        1, 85000,'PENDING', NULL,        NOW(), NOW()),
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000002','bbbbbbbb-0000-0000-0000-000000000023','Nước cam tươi',       1, 45000,'PENDING', NULL,        NOW(), NOW()),
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000002','bbbbbbbb-0000-0000-0000-000000000024','Cà phê đen',          1,  30000,'PENDING', NULL,        NOW(), NOW());

-- Wait, 175+135+85+45+30 = 470. Adjust order total.
UPDATE orders SET total_amount = 470000 WHERE id='ffffffff-0000-0000-0000-000000000002';

-- 003 COOKING - Bàn T04 - Tổng ~700,000
INSERT INTO order_items (id, order_id, menu_item_id, menu_item_name, quantity, price, status, note, created_at, updated_at) VALUES
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000003','bbbbbbbb-0000-0000-0000-000000000008','Bò bít tết',          1,285000,'COOKING','Medium rare', NOW() - INTERVAL '40 minutes', NOW() - INTERVAL '20 minutes'),
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000003','bbbbbbbb-0000-0000-0000-000000000009','Cá hồi áp chảo',      1,265000,'COOKING', NULL,         NOW() - INTERVAL '40 minutes', NOW() - INTERVAL '20 minutes'),
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000003','bbbbbbbb-0000-0000-0000-000000000016','Bánh mì bơ tỏi',      2, 35000,'READY_TO_SERVE', NULL, NOW() - INTERVAL '40 minutes', NOW() - INTERVAL '15 minutes'),
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000003','bbbbbbbb-0000-0000-0000-000000000026','Bia Tiger',           2, 40000,'SERVED',  NULL,         NOW() - INTERVAL '40 minutes', NOW() - INTERVAL '35 minutes');
-- 285+265+70+80 = 700
UPDATE orders SET total_amount = 700000 WHERE id='ffffffff-0000-0000-0000-000000000003';

-- 004 READY_TO_SERVE - Bàn T06 - 6 người
INSERT INTO order_items (id, order_id, menu_item_id, menu_item_name, quantity, price, status, note, created_at, updated_at) VALUES
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000004','bbbbbbbb-0000-0000-0000-000000000012','Pizza hải sản',       2,195000,'READY_TO_SERVE', NULL, NOW() - INTERVAL '55 minutes', NOW() - INTERVAL '5 minutes'),
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000004','bbbbbbbb-0000-0000-0000-000000000010','Gà nướng mật ong',    2,175000,'READY_TO_SERVE', NULL, NOW() - INTERVAL '55 minutes', NOW() - INTERVAL '5 minutes'),
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000004','bbbbbbbb-0000-0000-0000-000000000018','Tiramisu',            2, 75000,'READY_TO_SERVE', NULL, NOW() - INTERVAL '55 minutes', NOW() - INTERVAL '8 minutes'),
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000004','bbbbbbbb-0000-0000-0000-000000000022','Coca Cola',           4, 25000,'SERVED',         NULL, NOW() - INTERVAL '55 minutes', NOW() - INTERVAL '50 minutes'),
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000004','bbbbbbbb-0000-0000-0000-000000000023','Nước cam tươi',       2, 45000,'SERVED',         NULL, NOW() - INTERVAL '55 minutes', NOW() - INTERVAL '50 minutes');
-- 390+350+150+100+90 = 1080. Adjust:
UPDATE orders SET total_amount = 1080000 WHERE id='ffffffff-0000-0000-0000-000000000004';

-- 005 SERVED - Bàn P03
INSERT INTO order_items (id, order_id, menu_item_id, menu_item_name, quantity, price, status, note, created_at, updated_at) VALUES
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000005','bbbbbbbb-0000-0000-0000-000000000013','Cơm chiên Dương Châu',2, 95000,'SERVED', NULL, NOW() - INTERVAL '85 minutes', NOW() - INTERVAL '50 minutes'),
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000005','bbbbbbbb-0000-0000-0000-000000000014','Phở bò tái',          2, 85000,'SERVED', NULL, NOW() - INTERVAL '85 minutes', NOW() - INTERVAL '50 minutes'),
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000005','bbbbbbbb-0000-0000-0000-000000000003','Súp cua bóng cá',     2, 65000,'SERVED', NULL, NOW() - INTERVAL '85 minutes', NOW() - INTERVAL '60 minutes'),
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000005','bbbbbbbb-0000-0000-0000-000000000025','Trà đào cam sả',      2, 55000,'SERVED', NULL, NOW() - INTERVAL '85 minutes', NOW() - INTERVAL '70 minutes');
-- 190+170+130+110 = 600
UPDATE orders SET total_amount = 600000 WHERE id='ffffffff-0000-0000-0000-000000000005';

-- 006 COMPLETED - takeaway
INSERT INTO order_items (id, order_id, menu_item_id, menu_item_name, quantity, price, status, note, created_at, updated_at) VALUES
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000006','bbbbbbbb-0000-0000-0000-000000000012','Pizza hải sản',       1,195000,'SERVED', NULL, NOW() - INTERVAL '3 hours', NOW() - INTERVAL '2 hours 30 minutes'),
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000006','bbbbbbbb-0000-0000-0000-000000000004','Khoai tây chiên',     1, 45000,'SERVED', NULL, NOW() - INTERVAL '3 hours', NOW() - INTERVAL '2 hours 30 minutes');
-- 195+45 = 240
UPDATE orders SET total_amount = 240000 WHERE id='ffffffff-0000-0000-0000-000000000006';

-- 007 COMPLETED - bàn T01
INSERT INTO order_items (id, order_id, menu_item_id, menu_item_name, quantity, price, status, note, created_at, updated_at) VALUES
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000007','bbbbbbbb-0000-0000-0000-000000000008','Bò bít tết',          1,285000,'SERVED','Well done', NOW() - INTERVAL '5 hours', NOW() - INTERVAL '4 hours 30 minutes'),
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000007','bbbbbbbb-0000-0000-0000-000000000019','Kem socola',          1, 55000,'SERVED', NULL,       NOW() - INTERVAL '5 hours', NOW() - INTERVAL '4 hours 30 minutes'),
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000007','bbbbbbbb-0000-0000-0000-000000000026','Bia Tiger',           2, 40000,'SERVED', NULL,       NOW() - INTERVAL '5 hours', NOW() - INTERVAL '4 hours 50 minutes');
-- 285+55+80 = 420
UPDATE orders SET total_amount = 420000 WHERE id='ffffffff-0000-0000-0000-000000000007';

-- 008 CANCELLED
INSERT INTO order_items (id, order_id, menu_item_id, menu_item_name, quantity, price, status, note, created_at, updated_at) VALUES
  (gen_random_uuid(),'ffffffff-0000-0000-0000-000000000008','bbbbbbbb-0000-0000-0000-000000000014','Phở bò tái',          1, 85000,'CANCELLED', NULL, NOW() - INTERVAL '1 day', NOW() - INTERVAL '23 hours');
UPDATE orders SET total_amount = 85000 WHERE id='ffffffff-0000-0000-0000-000000000008';
