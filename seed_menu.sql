-- =============== MENU (irms_menu) ===============
\c irms_menu

-- Categories
INSERT INTO categories (id, name, display_order, created_at, updated_at) VALUES
  ('aaaaaaaa-0000-0000-0000-000000000001','Khai vị',     1, NOW(), NOW()),
  ('aaaaaaaa-0000-0000-0000-000000000002','Salad',       2, NOW(), NOW()),
  ('aaaaaaaa-0000-0000-0000-000000000003','Món chính',   3, NOW(), NOW()),
  ('aaaaaaaa-0000-0000-0000-000000000004','Món ăn kèm',  4, NOW(), NOW()),
  ('aaaaaaaa-0000-0000-0000-000000000005','Tráng miệng', 5, NOW(), NOW()),
  ('aaaaaaaa-0000-0000-0000-000000000006','Đồ uống',     6, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Menu items
INSERT INTO menu_items (id, category_id, name, description, price, preparation_time, is_available, image_url, created_at, updated_at) VALUES
  -- Khai vị
  ('bbbbbbbb-0000-0000-0000-000000000001','aaaaaaaa-0000-0000-0000-000000000001','Gỏi cuốn tôm thịt','Bánh tráng cuốn tôm, thịt heo, bún, rau sống',  55000, 10, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000002','aaaaaaaa-0000-0000-0000-000000000001','Chả giò hải sản','Chả giò chiên giòn nhân hải sản',                75000, 12, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000003','aaaaaaaa-0000-0000-0000-000000000001','Súp cua bóng cá','Súp cua nóng với bóng cá, nấm, trứng',           65000, 15, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000004','aaaaaaaa-0000-0000-0000-000000000001','Khoai tây chiên',  'Khoai tây chiên giòn ăn kèm sốt mayonnaise',     45000,  8, TRUE, NULL, NOW(), NOW()),

  -- Salad
  ('bbbbbbbb-0000-0000-0000-000000000005','aaaaaaaa-0000-0000-0000-000000000002','Salad Caesar',     'Xà lách romaine, sốt Caesar, bánh mì nướng, parmesan', 85000, 10, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000006','aaaaaaaa-0000-0000-0000-000000000002','Salad bò Nga',    'Bò xốt mayonnaise, khoai tây, dưa chuột',              95000, 10, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000007','aaaaaaaa-0000-0000-0000-000000000002','Salad rau trộn',  'Rau xà lách, cà chua, dưa chuột, sốt giấm balsamic',   55000,  8, TRUE, NULL, NOW(), NOW()),

  -- Món chính
  ('bbbbbbbb-0000-0000-0000-000000000008','aaaaaaaa-0000-0000-0000-000000000003','Bò bít tết',      'Steak bò Úc 200g sốt tiêu đen + khoai tây nghiền',    285000, 20, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000009','aaaaaaaa-0000-0000-0000-000000000003','Cá hồi áp chảo',  'Cá hồi Na Uy áp chảo sốt bơ tỏi',                     265000, 18, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000010','aaaaaaaa-0000-0000-0000-000000000003','Gà nướng mật ong','Đùi gà nướng nguyên cái với sốt mật ong',             175000, 25, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000011','aaaaaaaa-0000-0000-0000-000000000003','Mỳ Ý sốt bò bằm', 'Spaghetti với sốt cà chua thịt bò bằm',               135000, 15, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000012','aaaaaaaa-0000-0000-0000-000000000003','Pizza hải sản',   'Pizza đế dày, hải sản tươi, phô mai mozzarella',      195000, 22, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000013','aaaaaaaa-0000-0000-0000-000000000003','Cơm chiên Dương Châu','Cơm chiên với tôm, xúc xích, lạp xưởng, trứng',  95000, 12, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000014','aaaaaaaa-0000-0000-0000-000000000003','Phở bò tái',      'Phở bò truyền thống với thịt bò tái',                  85000, 12, TRUE, NULL, NOW(), NOW()),

  -- Món ăn kèm
  ('bbbbbbbb-0000-0000-0000-000000000015','aaaaaaaa-0000-0000-0000-000000000004','Cơm trắng',       'Cơm trắng dẻo thơm',                                   15000,  5, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000016','aaaaaaaa-0000-0000-0000-000000000004','Bánh mì bơ tỏi',  'Bánh mì nướng với bơ tỏi',                             35000,  7, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000017','aaaaaaaa-0000-0000-0000-000000000004','Rau củ luộc',     'Súp lơ, cà rốt, đậu Hà Lan luộc',                      40000,  8, TRUE, NULL, NOW(), NOW()),

  -- Tráng miệng
  ('bbbbbbbb-0000-0000-0000-000000000018','aaaaaaaa-0000-0000-0000-000000000005','Tiramisu',        'Tiramisu Ý truyền thống',                              75000,  5, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000019','aaaaaaaa-0000-0000-0000-000000000005','Kem socola',      'Kem socola Bỉ cao cấp 3 viên',                         55000,  5, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000020','aaaaaaaa-0000-0000-0000-000000000005','Bánh flan caramel','Bánh flan trứng caramel',                              45000,  5, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000021','aaaaaaaa-0000-0000-0000-000000000005','Chè khúc bạch',   'Chè khúc bạch sữa tươi, hạnh nhân',                    50000,  5, TRUE, NULL, NOW(), NOW()),

  -- Đồ uống
  ('bbbbbbbb-0000-0000-0000-000000000022','aaaaaaaa-0000-0000-0000-000000000006','Coca Cola',       'Lon 330ml',                                             25000,  2, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000023','aaaaaaaa-0000-0000-0000-000000000006','Nước cam tươi',   'Nước cam vắt nguyên chất 300ml',                        45000,  5, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000024','aaaaaaaa-0000-0000-0000-000000000006','Cà phê đen',      'Cà phê đen pha phin',                                   30000,  5, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000025','aaaaaaaa-0000-0000-0000-000000000006','Trà đào cam sả',  'Trà đào với cam và sả tươi',                            55000,  5, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000026','aaaaaaaa-0000-0000-0000-000000000006','Bia Tiger',       'Lon 330ml',                                             40000,  2, TRUE, NULL, NOW(), NOW()),
  ('bbbbbbbb-0000-0000-0000-000000000027','aaaaaaaa-0000-0000-0000-000000000006','Sinh tố bơ',      'Sinh tố bơ sữa tươi',                                   55000,  6, FALSE,NULL, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Allergens
INSERT INTO menu_item_allergens (menu_item_id, allergen) VALUES
  ('bbbbbbbb-0000-0000-0000-000000000001','SHRIMP'),
  ('bbbbbbbb-0000-0000-0000-000000000002','SHELLFISH'),
  ('bbbbbbbb-0000-0000-0000-000000000003','EGG'),
  ('bbbbbbbb-0000-0000-0000-000000000005','GLUTEN'),
  ('bbbbbbbb-0000-0000-0000-000000000005','DAIRY'),
  ('bbbbbbbb-0000-0000-0000-000000000005','EGG'),
  ('bbbbbbbb-0000-0000-0000-000000000009','FISH'),
  ('bbbbbbbb-0000-0000-0000-000000000011','GLUTEN'),
  ('bbbbbbbb-0000-0000-0000-000000000011','DAIRY'),
  ('bbbbbbbb-0000-0000-0000-000000000012','GLUTEN'),
  ('bbbbbbbb-0000-0000-0000-000000000012','DAIRY'),
  ('bbbbbbbb-0000-0000-0000-000000000012','SHELLFISH'),
  ('bbbbbbbb-0000-0000-0000-000000000016','GLUTEN'),
  ('bbbbbbbb-0000-0000-0000-000000000016','DAIRY'),
  ('bbbbbbbb-0000-0000-0000-000000000018','EGG'),
  ('bbbbbbbb-0000-0000-0000-000000000018','DAIRY'),
  ('bbbbbbbb-0000-0000-0000-000000000018','GLUTEN'),
  ('bbbbbbbb-0000-0000-0000-000000000019','DAIRY'),
  ('bbbbbbbb-0000-0000-0000-000000000020','EGG'),
  ('bbbbbbbb-0000-0000-0000-000000000020','DAIRY'),
  ('bbbbbbbb-0000-0000-0000-000000000021','DAIRY'),
  ('bbbbbbbb-0000-0000-0000-000000000021','NUTS');
