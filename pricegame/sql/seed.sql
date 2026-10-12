USE pricegame;

-- Mat khau = SHA-256(salt + password). Tai khoan mau: alice/bob/carol, mat khau 123456
INSERT INTO users (username, salt, password) VALUES
 ('alice', 's1', SHA2(CONCAT('s1','123456'),256)),
 ('bob',   's2', SHA2(CONCAT('s2','123456'),256)),
 ('carol', 's3', SHA2(CONCAT('s3','123456'),256));

-- image: ten file trong src/main/resources/images (neu thieu, client ve anh thay the)
INSERT INTO product (name, description, image, price) VALUES
 ('iPhone 15 128GB', 'Dien thoai Apple man hinh 6.1 inch, chip A16 Bionic', 'iphone15.jpg', 19990000),
 ('Samsung Galaxy S24', 'Dien thoai Samsung 256GB, camera 50MP', 's24.jpg', 22990000),
 ('Laptop Dell Inspiron 15', 'Core i5, RAM 16GB, SSD 512GB', 'dell15.jpg', 17500000),
 ('MacBook Air M2', 'Chip M2, 8GB RAM, 256GB SSD, 13.6 inch', 'macbookair.jpg', 26990000),
 ('Tai nghe AirPods Pro 2', 'Chong on chu dong, hop sac MagSafe', 'airpods.jpg', 5990000),
 ('Smart TV Sony 55 inch 4K', 'Google TV, man hinh 4K HDR', 'sonytv.jpg', 15900000),
 ('Tu lanh Panasonic 322L', 'Inverter, ngan dong mem', 'tulanh.jpg', 11200000),
 ('May giat LG 9kg', 'Inverter, lồng giặt ngang', 'maygiat.jpg', 8900000),
 ('Noi chien khong dau Philips 4.1L', 'Cong nghe Rapid Air', 'noichien.jpg', 2490000),
 ('Robot hut bui Xiaomi', 'Lau hut thong minh, dieu khien qua app', 'robot.jpg', 6490000),
 ('May anh Canon EOS R50', 'Mirrorless 24MP kem lens 18-45mm', 'canon.jpg', 20500000),
 ('Ban phim co Keychron K2', 'Khong day, switch Gateron', 'keychron.jpg', 2190000),
 ('Chuot Logitech MX Master 3S', 'Chuot khong day cao cap', 'mxmaster.jpg', 2390000),
 ('May choi game PlayStation 5', 'Phien ban dia, 825GB', 'ps5.jpg', 12990000),
 ('Nintendo Switch OLED', 'Man hinh OLED 7 inch', 'switch.jpg', 7990000),
 ('Dong ho Apple Watch SE', 'GPS, 40mm', 'applewatch.jpg', 6490000),
 ('Loa JBL Charge 5', 'Loa bluetooth chong nuoc IP67', 'jbl.jpg', 3990000),
 ('May loc khong khi Xiaomi 4 Pro', 'Loc bui min PM2.5', 'maylockk.jpg', 4290000),
 ('Xe may Honda Vision 2024', 'Xe ga 110cc, tieu thu nhien lieu thap', 'vision.jpg', 32000000),
 ('Ghe gaming Cougar', 'Ghe cong thai hoc, ngả 180 do', 'gheg.jpg', 3500000);
