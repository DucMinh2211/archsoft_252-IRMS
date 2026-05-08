-- =============== TABLE (irms_table) ===============
\c irms_table

-- Restaurant tables
INSERT INTO restaurant_tables (id, table_number, capacity, location, status, current_order_id, seated_at, created_at, updated_at) VALUES
  ('cccccccc-0000-0000-0000-000000000001','T01', 2, 'Indoor',     'AVAILABLE', NULL, NULL, NOW(), NOW()),
  ('cccccccc-0000-0000-0000-000000000002','T02', 2, 'Indoor',     'AVAILABLE', NULL, NULL, NOW(), NOW()),
  ('cccccccc-0000-0000-0000-000000000003','T03', 4, 'Indoor',     'OCCUPIED',  'ffffffff-0000-0000-0000-000000000002', NOW() - INTERVAL '30 minutes', NOW(), NOW()),
  ('cccccccc-0000-0000-0000-000000000004','T04', 4, 'Indoor',     'OCCUPIED',  'ffffffff-0000-0000-0000-000000000003', NOW() - INTERVAL '45 minutes', NOW(), NOW()),
  ('cccccccc-0000-0000-0000-000000000005','T05', 4, 'Indoor',     'AVAILABLE', NULL, NULL, NOW(), NOW()),
  ('cccccccc-0000-0000-0000-000000000006','T06', 6, 'Indoor',     'OCCUPIED',  'ffffffff-0000-0000-0000-000000000004', NOW() - INTERVAL '60 minutes', NOW(), NOW()),
  ('cccccccc-0000-0000-0000-000000000007','T07', 6, 'Indoor',     'CLEANING',  NULL, NULL, NOW(), NOW()),
  ('cccccccc-0000-0000-0000-000000000008','P01', 2, 'Patio',      'AVAILABLE', NULL, NULL, NOW(), NOW()),
  ('cccccccc-0000-0000-0000-000000000009','P02', 4, 'Patio',      'RESERVED',  NULL, NULL, NOW(), NOW()),
  ('cccccccc-0000-0000-0000-000000000010','P03', 4, 'Patio',      'OCCUPIED',  'ffffffff-0000-0000-0000-000000000005', NOW() - INTERVAL '90 minutes', NOW(), NOW()),
  ('cccccccc-0000-0000-0000-000000000011','V01', 8, 'VIP Room',   'RESERVED',  NULL, NULL, NOW(), NOW()),
  ('cccccccc-0000-0000-0000-000000000012','V02',10, 'VIP Room',   'AVAILABLE', NULL, NULL, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Reservations
INSERT INTO reservations (id, customer_name, customer_phone, party_size, reservation_time, status, table_id, expected_duration_minutes, notes, created_at, updated_at) VALUES
  ('dddddddd-0000-0000-0000-000000000001','Nguyễn Văn An',  '0901234567', 4, NOW() + INTERVAL '2 hours',  'CONFIRMED','cccccccc-0000-0000-0000-000000000009', 90, 'Sinh nhật, cần bánh kem', NOW(), NOW()),
  ('dddddddd-0000-0000-0000-000000000002','Trần Thị Bình',  '0912345678', 8, NOW() + INTERVAL '3 hours',  'CONFIRMED','cccccccc-0000-0000-0000-000000000011', 120, 'Họp công ty', NOW(), NOW()),
  ('dddddddd-0000-0000-0000-000000000003','Lê Văn Cường',   '0923456789', 2, NOW() + INTERVAL '5 hours',  'PENDING',  NULL, 60, NULL, NOW(), NOW()),
  ('dddddddd-0000-0000-0000-000000000004','Phạm Thị Dung',  '0934567890', 6, NOW() + INTERVAL '1 day',    'PENDING',  NULL, 90, 'Ăn chay', NOW(), NOW()),
  ('dddddddd-0000-0000-0000-000000000005','Hoàng Văn Em',   '0945678901', 4, NOW() - INTERVAL '1 day',    'NO_SHOW',  NULL, 60, NULL, NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day'),
  ('dddddddd-0000-0000-0000-000000000006','Vũ Thị Phương',  '0956789012', 3, NOW() - INTERVAL '2 hours',  'SEATED',  'cccccccc-0000-0000-0000-000000000003', 90, NULL, NOW() - INTERVAL '3 hours', NOW())
ON CONFLICT DO NOTHING;

-- Waitlist entries
INSERT INTO waitlist_entries (id, customer_name, customer_phone, party_size, queue_position, estimated_wait_minutes, status, notified_at, created_at, updated_at) VALUES
  ('eeeeeeee-0000-0000-0000-000000000001','Đỗ Văn Hùng',    '0967890123', 2, 1, 15, 'WAITING',  NULL,                       NOW() - INTERVAL '10 minutes', NOW()),
  ('eeeeeeee-0000-0000-0000-000000000002','Bùi Thị Hoa',    '0978901234', 4, 2, 30, 'WAITING',  NULL,                       NOW() - INTERVAL '5 minutes',  NOW()),
  ('eeeeeeee-0000-0000-0000-000000000003','Mai Văn Khoa',   '0989012345', 6, 3, 45, 'NOTIFIED', NOW() - INTERVAL '2 minutes', NOW() - INTERVAL '20 minutes', NOW())
ON CONFLICT DO NOTHING;
