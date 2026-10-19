# Hãy Chọn Giá Đúng – game đối kháng 2 người (TCP Socket + JSON)

## 1. Cơ sở dữ liệu (MySQL 8)
```
mysql -uroot -p --default-character-set=utf8mb4 < sql/schema.sql
mysql -uroot -p --default-character-set=utf8mb4 < sql/seed.sql
```
Tài khoản mẫu: `alice`, `bob`, `carol` (mật khẩu `123456`); client cũng có nút Đăng ký.

Nếu DB đã có dữ liệu cũ, chỉ cần cập nhật tên/mô tả/ảnh sản phẩm (giữ nguyên lịch sử, tài khoản):
```
mysql -uroot -p --default-character-set=utf8mb4 < sql/update_products.sql
```
Ảnh sản phẩm nằm ở `src/main/resources/images/` (nguồn và giấy phép trong `CREDITS.md` cùng thư mục).

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

## Âm thanh
File WAV nằm trong `src/main/resources/sounds/` (phải có thư mục này trong `-cp` khi chạy client). Có nút "Âm thanh: Bật/Tắt" ở sảnh chờ và màn chơi. Muốn đổi nhạc, thay file cùng tên (WAV PCM 16-bit; Java không phát được MP3).

| File | Dùng khi | Nguồn (đều CC0 / public domain, OpenGameArt) |
|---|---|---|
| bgm.wav | nhạc nền lặp lại | "Swinging Sweet" – gói [Short loops background music pack](https://opengameart.org/content/short-loops-background-music-pack) (hernandack) |
| correct.wav, wrong.wav | thắng / thua một lượt | "Coin01", "Downer01" – [Level up, power up, coin get (13 sounds)](https://opengameart.org/content/level-up-power-up-coin-get-13-sounds) |
| win.wav | thắng cả trận | [win sound effect](https://opengameart.org/content/win-sound-effect) (Listener) |
| lose.wav | thua cả trận | "letstryagain" – [Win / Lose Voice Speech](https://opengameart.org/content/win-lose-voice-speech) (ShiniHaize) |
| challenge.wav, tick.wav | có lời thách đấu / 5 giây cuối | [Interface beeps](https://opengameart.org/content/interface-beeps) |

## Giao thức
Mỗi message là 1 dòng JSON `{"type":"GUESS","data":{"price":1000000}}`; danh sách type ở `common/protocol/MessageType.java` (Bảng 3 báo cáo, thêm REGISTER_REQ/RES).
