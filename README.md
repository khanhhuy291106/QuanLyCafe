# Quản lý quán cafe (Java Swing + SQL Server)

## Cấu trúc thư mục
```
QuanLyCafe
├── src
│   ├── connectDB   ConnectDB.java        (kết nối SQL Server)
│   ├── entity      các lớp thực thể (Mon, HoaDon, NhanVien, ...)
│   ├── dao         X_DAO.java            (truy vấn CSDL)
│   └── gui         Main.java + các màn hình (Tab..., QL..., hộp thoại)
├── sql             tao_bang.sql, du_lieu_mau.sql
├── image/mon       ảnh món
└── lib             jdatepicker, jfreechart, mssql-jdbc
```

## Chạy chương trình
1. Chạy `sql/tao_bang.sql` rồi `sql/du_lieu_mau.sql` trong SQL Server.
2. Sửa tài khoản/mật khẩu SQL Server trong `src/connectDB/ConnectDB.java` (mặc định `sa` / `123456`, cổng 1433).
3. Mở project bằng Eclipse (đã có sẵn `.classpath` gồm 3 thư viện trong `lib`), chạy `gui.Main`.
   Chạy từ thư mục gốc của project để chương trình tìm thấy thư mục `image/mon`.

Tài khoản mẫu (mật khẩu 123456): `quan.le` (Quản lý), `van.a` / `bich.tran` / `trang.ly` (Thu ngân), `nam.pham` / `han.vo` (Pha chế).
