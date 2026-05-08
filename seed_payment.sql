-- =============== PAYMENT (irms_payment) ===============
\c irms_payment

INSERT INTO payments (id, order_id, amount, payment_method, status, transaction_id, created_at, updated_at) VALUES
  ('aaaaaaaa-1111-0000-0000-000000000001','ffffffff-0000-0000-0000-000000000006', 240000,'E_WALLET',   'COMPLETED','MOMO-20260507-0001', NOW() - INTERVAL '2 hours 30 minutes', NOW() - INTERVAL '2 hours 30 minutes'),
  ('aaaaaaaa-1111-0000-0000-000000000002','ffffffff-0000-0000-0000-000000000007', 420000,'CREDIT_CARD','COMPLETED','VISA-20260507-0117',  NOW() - INTERVAL '4 hours',            NOW() - INTERVAL '4 hours'),
  ('aaaaaaaa-1111-0000-0000-000000000003','ffffffff-0000-0000-0000-000000000008',      0,'CASH',       'REFUNDED', NULL,                  NOW() - INTERVAL '23 hours',           NOW() - INTERVAL '23 hours'),
  -- Một payment đang pending để FE có dữ liệu test
  ('aaaaaaaa-1111-0000-0000-000000000004','ffffffff-0000-0000-0000-000000000005', 600000,'CASH',       'PENDING',  NULL,                  NOW() - INTERVAL '5 minutes',          NOW() - INTERVAL '5 minutes')
ON CONFLICT DO NOTHING;
