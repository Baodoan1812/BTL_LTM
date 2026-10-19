USE pricegame;

-- Mat khau = SHA-256(salt + password). Tai khoan mau: alice/bob/carol, mat khau 123456
INSERT INTO users (username, salt, password) VALUES
 ('alice', 's1', SHA2(CONCAT('s1','123456'),256)),
 ('bob',   's2', SHA2(CONCAT('s2','123456'),256)),
 ('carol', 's3', SHA2(CONCAT('s3','123456'),256));

-- image: ten file trong src/main/resources/images (thieu anh thi client ve dau ?)
INSERT INTO product (id, name, description, image, price) VALUES
 (1, 'iPhone 15 128GB', 'Điện thoại của Apple với màn hình Super Retina XDR 6,1 inch và cổng sạc USB-C. Máy dùng chip A16 Bionic mạnh mẽ cùng camera chính 48MP. Có nhiều màu pastel trẻ trung như hồng, xanh và vàng.', 'iphone15.jpg', 19990000),
 (2, 'Samsung Galaxy S24 Ultra 256GB', 'Flagship của Samsung với khung viền titan và bút S Pen đi kèm trong thân máy. Màn hình Dynamic AMOLED 6,8 inch cùng chip Snapdragon 8 Gen 3 cho hiệu năng rất cao. Camera chính 200MP chụp thiếu sáng rất tốt.', 's24.jpg', 29990000),
 (3, 'MacBook Air M2 13,6 inch', 'Laptop mỏng nhẹ của Apple dùng chip M2 với 8GB RAM và SSD 256GB. Màn hình Liquid Retina 13,6 inch cho hình ảnh sắc nét và màu sắc chính xác. Pin dùng được khoảng 18 giờ trong một lần sạc.', 'macbookair.jpg', 26990000),
 (4, 'Tai nghe Apple AirPods Pro 2', 'Tai nghe không dây của Apple với khả năng chống ồn chủ động được cải tiến. Hộp sạc hỗ trợ MagSafe và có loa nhỏ giúp tìm máy khi bị thất lạc. Thời lượng pin khoảng 6 giờ nghe nhạc liên tục.', 'airpods.jpg', 5990000),
 (5, 'Máy ảnh Canon EOS R50 kèm lens 18-45mm', 'Máy ảnh mirrorless nhỏ gọn của Canon với cảm biến APS-C 24,2MP. Hỗ trợ quay video 4K và lấy nét tự động bám theo đối tượng. Phù hợp cho người mới học chụp ảnh và làm vlog.', 'canon.jpg', 20500000),
 (6, 'Chuột Logitech MX Master 3S', 'Chuột không dây cao cấp của Logitech với cảm biến 8000 DPI siêu nhạy. Con lăn MagSpeed cuộn rất nhanh mà gần như không phát ra tiếng ồn. Pin dùng được tới 70 ngày và kết nối được nhiều thiết bị.', 'mxmaster.jpg', 2390000),
 (7, 'Máy chơi game Sony PlayStation 5', 'Máy chơi game console của Sony có ổ SSD siêu nhanh giúp tải game gần như tức thì. Hỗ trợ độ phân giải 4K cùng hiệu ứng đổ bóng ray tracing chân thực. Tay cầm DualSense có rung phản hồi xúc giác rất độc đáo.', 'ps5.jpg', 12990000),
 (8, 'Nintendo Switch OLED', 'Máy chơi game lai của Nintendo với màn hình OLED 7 inch rực rỡ. Có thể cắm vào đế để chơi trên tivi hoặc tháo ra mang đi chơi mọi nơi. Sở hữu nhiều tựa game nổi tiếng như Mario, Zelda và Pokémon.', 'switch.jpg', 7990000),
 (9, 'Đồng hồ Apple Watch SE 40mm', 'Đồng hồ thông minh của Apple có mức giá dễ tiếp cận hơn các dòng Series. Theo dõi nhịp tim, giấc ngủ và các bài tập luyện hằng ngày. Có tính năng phát hiện va chạm và gọi khẩn cấp khi cần.', 'applewatch.jpg', 6490000),
 (10, 'Robot hút bụi Xiaomi Mi Robot Vacuum', 'Robot hút bụi của Xiaomi dùng cảm biến laser để lập bản đồ căn nhà. Có thể điều khiển và đặt lịch dọn dẹp qua ứng dụng trên điện thoại. Pin đủ dùng khoảng 2,5 giờ cho một lần làm việc.', 'robot.jpg', 5490000),
 (11, 'Xe máy Honda Vision 2024', 'Xe tay ga phổ thông của Honda, nổi tiếng nhỏ gọn và rất dễ lái. Động cơ 110cc tiết kiệm xăng, chỉ khoảng 1,9 lít cho mỗi 100 km. Cốp xe rộng rãi phù hợp đi học và đi làm hằng ngày.', 'vision.jpg', 31500000),
 (12, 'Tủ lạnh Panasonic Inverter 320 lít', 'Tủ lạnh 2 cánh ngăn đá trên của Panasonic có dung tích khoảng 320 lít. Công nghệ Inverter giúp tiết kiệm điện và vận hành êm ái. Có ngăn rau củ riêng giúp thực phẩm tươi lâu hơn.', 'tulanh.jpg', 9900000),
 (13, 'Máy giặt LG Inverter Direct Drive 7 kg', 'Máy giặt lồng ngang của LG với khối lượng giặt 7 kg, phù hợp gia đình 2 đến 4 người. Động cơ Inverter Direct Drive vận hành êm và bền bỉ, được bảo hành 10 năm. Có nhiều chế độ giặt như giặt nhanh 30 phút, giặt chăn và đồ len.', 'maygiat.jpg', 7500000),
 (14, 'Ghế gaming công thái học', 'Ghế có tựa lưng cao cùng gối đỡ cổ và đệm lưng giúp ngồi lâu không bị mỏi. Tựa lưng ngả được gần 180 độ để nằm nghỉ thư giãn. Khung thép chắc chắn và bọc da PU dễ lau chùi.', 'gheg.jpg', 3500000),
 (15, 'Nồi chiên không dầu Philips Airfryer', 'Nồi chiên không dầu của Philips dùng công nghệ Rapid Air để làm chín thức ăn bằng luồng khí nóng. Món ăn giòn ngon mà ít dầu mỡ hơn nhiều so với chiên rán thông thường. Giỏ chiên tháo rời nên rất dễ vệ sinh.', 'noichien.jpg', 2490000),
 (16, 'Laptop Lenovo IdeaPad Flex 14', 'Laptop 2 trong 1 có màn hình cảm ứng 14 inch xoay gập được 360 độ. Có thể dùng như laptop, máy tính bảng hoặc dựng lên để xem phim. Cấu hình phù hợp cho học tập và làm việc văn phòng.', 'laptop.jpg', 14990000),
 (17, 'Bàn phím cơ Keychron K8', 'Bàn phím cơ không dây layout TKL 87 phím, kết nối Bluetooth với nhiều thiết bị cùng lúc. Dùng được với cả Windows lẫn macOS và có cảm giác gõ rất rõ phím. Pin dùng được nhiều tuần mới cần sạc.', 'keychron.jpg', 2290000),
 (18, 'Loa Bluetooth JBL Charge 4', 'Loa Bluetooth di động của JBL với âm bass mạnh mẽ và chuẩn chống nước IP67. Pin 7500 mAh cho thời gian phát nhạc liên tục khoảng 20 giờ. Có cổng USB để sạc ngược cho điện thoại khi cần.', 'jbl.jpg', 3290000),
 (19, 'Máy lọc không khí Xiaomi Smart Air Purifier 2S', 'Máy lọc không khí của Xiaomi lọc bụi mịn PM2.5 hiệu quả cho phòng rộng khoảng 30 m². Có màn hình OLED hiển thị chất lượng không khí và điều khiển được qua ứng dụng. Máy chạy khá êm nên có thể đặt trong phòng ngủ.', 'maylockk.jpg', 2990000),
 (20, 'Máy đọc sách Amazon Kindle Paperwhite', 'Máy đọc sách của Amazon với màn hình E-ink đọc dễ chịu như trên giấy thật. Chống nước chuẩn IPX8 nên có thể đọc cả ở bể bơi hoặc bãi biển. Pin dùng được nhiều tuần chỉ với một lần sạc.', 'kindle.jpg', 4490000);
