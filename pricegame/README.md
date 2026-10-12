# Hãy Chọn Giá Đúng – game đối kháng 2 người (TCP Socket + JSON)

## 1. Cơ sở dữ liệu (MySQL 8)
```
mysql -uroot -p --default-character-set=utf8mb4 < sql/schema.sql
mysql -uroot -p --default-character-set=utf8mb4 < sql/seed.sql
```
Tài khoản mẫu: `alice`, `bob`, `carol` (mật khẩu `123456`); client cũng có nút Đăng ký.

## 2. Chạy server
Đặt biến môi trường nếu khác mặc định: `DB_URL`, `DB_USER` (root), `DB_PASS` (rỗng).
```
set DB_PASS=matkhau
mvn package
java -cp target/pricegame-1.0.jar server.network.ServerListener 9999
```

## 3. Chạy client (mở 2 cửa sổ, hoặc ở máy khác trong LAN, nhập IP server)
```
java -cp target/pricegame-1.0.jar client.controller.ClientController
```

## Không có Maven?
```
javac -encoding UTF-8 -cp lib/gson-2.10.1.jar -d out/classes (danh sách file trong src/main/java)
java -cp "out/classes;lib/gson-2.10.1.jar;lib/mysql-connector-j-8.3.0.jar;src/main/resources" server.network.ServerListener
```
Ảnh sản phẩm: đặt file theo cột `image` của bảng `product` vào `src/main/resources/images/` (thiếu ảnh thì hiện tên sản phẩm).

## Giao thức
Mỗi message là 1 dòng JSON `{"type":"GUESS","data":{"price":1000000}}`; danh sách type ở `common/protocol/MessageType.java` (Bảng 3 báo cáo, thêm REGISTER_REQ/RES).
