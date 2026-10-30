# Quản lý quán cafe take away (Java Swing + SQL Server)

Ứng dụng desktop cho **nhân viên nội bộ** của một quán cafe chỉ bán mang đi (không có bàn).
Giao diện viết hoàn toàn bằng code Java (JFrame, JPanel...), không dùng kéo thả. Dữ liệu lưu ở SQL Server bằng JDBC (SQL Authentication).

Hình minh họa giao diện, sơ đồ lớp, cấu trúc project: xem `docs/Minh_hoa_du_an_Quan_ly_quan_cafe.pdf`.

---------------------------------------------------------------------

## 1. Phân quyền theo vai trò

Sau khi đăng nhập, menu bên trái **chỉ hiện các chức năng mà vai trò được dùng**. Ngoài việc ẩn menu, mỗi màn hình quản lý còn tự kiểm tra quyền khi được mở (không thể vào bằng cách khác).

| Chức năng | Quản lý | Thu ngân | Pha chế |
|---|:---:|:---:|:---:|
| Trang chủ (chỉ số trong ngày, đơn chờ, cảnh báo kho) | có (xem doanh thu) | có (không xem doanh thu) | có (không xem doanh thu) |
| Bán hàng (chọn món, thanh toán, in hóa đơn) | có | có | không |
| Quản lý đơn (xem, đổi trạng thái) | có | có (kèm hủy đơn, in lại hóa đơn) | có (chỉ đổi trạng thái) |
| **Thực đơn** (tạo/sửa/xóa món, loại, size, topping) | **có** | **không** | **không** |
| **Khuyến mãi** (cài đặt chương trình giảm giá) | **có** | **không** | **không** |
| Nhân viên và tài khoản | có | không | không |
| Kho: tồn kho (chỉ xem) | có | có | có |
| Kho: sửa tồn, phiếu nhập, phiếu xuất, nhà cung cấp | có | không | không |
| Thống kê, xuất báo cáo CSV | có | không | không |

Thu ngân và Quản lý khi bán hàng vẫn **chọn được** khuyến mãi có sẵn, nhưng chỉ Quản lý mới được tạo/sửa khuyến mãi.

---------------------------------------------------------------------

## 2. Yêu cầu

* JDK 11 trở lên (khuyến nghị JDK 17 hoặc 21).
* SQL Server (bản Express miễn phí cũng được) + SQL Server Management Studio (SSMS).
* Thư viện trong thư mục `lib/` (đã kèm sẵn): `mssql-jdbc-13.4.0.jre11.jar`, `jfreechart-1.5.4.jar` (biểu đồ thống kê), `jdatepicker-1.3.4.jar` (chọn ngày bằng lịch).
  Thư viện `xchart` bạn gửi kèm **không dùng** (đã dùng JFreeChart).

---------------------------------------------------------------------

## 3. Chuẩn bị SQL Server (chỉ làm một lần)

1. **Bật đăng nhập SQL Authentication**: mở SSMS, đăng nhập bằng Windows Authentication, bấm chuột phải vào tên server → *Properties* → *Security* → chọn **SQL Server and Windows Authentication mode** → OK.
2. **Bật tài khoản `sa`**: *Security* → *Logins* → bấm đôi `sa` → thẻ *General* đặt mật khẩu → thẻ *Status* chọn *Login: Enabled*.
3. **Bật TCP/IP**: mở *SQL Server Configuration Manager* → *SQL Server Network Configuration* → *Protocols for ...* → bật **TCP/IP** → bấm đôi TCP/IP → thẻ *IP Addresses* → kéo xuống *IPAll* đặt *TCP Port = 1433*.
4. **Khởi động lại dịch vụ SQL Server** (trong *SQL Server Services*).
5. Dùng bản **Express có tên instance `SQLEXPRESS`**: ngoài các bước trên, bật dịch vụ *SQL Server Browser*, rồi điền `db.instance=SQLEXPRESS` trong file cấu hình (mục 5).

## 4. Tạo cơ sở dữ liệu

Mở SSMS, lần lượt mở và chạy (F5) hai file trong thư mục `database/`:

1. `tao_bang.sql`: tạo CSDL `QuanLyCafe`, bảng, ràng buộc, view. **Có lệnh DROP, chạy lại sẽ xóa dữ liệu cũ.**
2. `du_lieu_mau.sql`: nạp dữ liệu mẫu (nhân viên, 13 món, size, topping, khuyến mãi, kho, 7 ngày hóa đơn để màn hình Thống kê có dữ liệu).

## 5. Cấu hình kết nối

Sửa file `config/config.properties` cho đúng máy của bạn:

```
db.host=localhost
db.port=1433
db.instance=          # chỉ điền nếu dùng instance có tên, ví dụ SQLEXPRESS
db.name=QuanLyCafe
db.user=sa
db.password=MẬT_KHẨU_CỦA_BẠN
db.encrypt=true       # đổi false nếu SQL Server quá cũ báo lỗi TLS/SSL
```

## 6. Chạy chương trình

**Dòng lệnh** (đứng ở thư mục gốc của project):

* Windows: chạy `build.bat` rồi `run.bat`.
* Linux/macOS: `./build.sh` rồi `./run.sh`.

**NetBeans**: *File → New Project → Java with Ant → Java Project with Existing Sources*, chọn thư mục project, thêm thư mục `src` làm *Source Package Folder*. Chuột phải project → *Properties* → *Libraries* → *Add JAR/Folder* thêm 3 file trong `lib/`. Ở *Sources* đặt *Encoding = UTF-8*. *Run → Main Class* chọn `gui.Main`. Working directory phải là thư mục gốc của project.

**Eclipse**: *File → Import → Existing Projects into Workspace* (project đã có sẵn `.project`, `.classpath`, mã hóa UTF-8). Chuột phải `gui/Main.java` → *Run As → Java Application*.

**IntelliJ IDEA**: *Open* thư mục project, *File → Project Structure → Modules → Sources* đánh dấu `src` là *Sources*, *Dependencies → +* thêm thư mục `lib` (Jars). *Settings → Editor → File Encodings* chọn UTF-8. Run `gui.Main`, *Working directory* = thư mục gốc project.

## 7. Tài khoản mẫu

Mật khẩu của mọi tài khoản mẫu: **123456**

| Tên đăng nhập | Vai trò |
|---|---|
| `quan.le` | Quản lý |
| `van.a`, `bich.tran`, `trang.ly` | Thu ngân |
| `nam.pham`, `han.vo` | Pha chế |
| `bao.dang` | Pha chế (đã nghỉ việc, đăng nhập sẽ báo tài khoản bị khóa) |

Nên đổi mật khẩu sau khi đăng nhập (menu dưới cùng bên trái: *Đổi mật khẩu*). Tài khoản mới do Quản lý tạo có mật khẩu mặc định 123456.

---------------------------------------------------------------------

## 8. Cấu trúc project

```
QuanLyCafe/
├── src/
│   ├── connect/    ConnectDB (Singleton, đọc config.properties)
│   ├── entity/     15 lớp thực thể (NhanVien, Mon, HoaDon, ChiTietHoaDon, KhuyenMai, ...)
│   ├── dao/        truy xuất SQL Server: mỗi bảng chính một DAO + BaseDAO (transaction, sinh mã)
│   └── gui/
│       ├── Main.java
│       ├── frame/      FrmDangNhap, FrmMain (+ PnlDangNhap, MainShell chứa giao diện)
│       ├── panel/      PnlTrangChu, PnlBanHang, PnlQuanLyDon, PnlThucDon, PnlNhanVien,
│       │               PnlKho, PnlKhuyenMai, PnlThongKe
│       ├── dialog/     DlgThanhToan, DlgHoaDon, DlgDoiMatKhau, DlgLichSuPhieu
│       ├── component/  BoGocButton, CrudPanel, FormPanel, DateField, UI ... (thành phần dùng chung)
│       └── util/       AppColor, FormatUtil, Validator (regex), HashUtil, SessionManager, ExportUtil
├── lib/            thư viện .jar
├── database/       tao_bang.sql, du_lieu_mau.sql
├── config/         config.properties
├── resources/      images/mon (ảnh món khi Quản lý chọn ảnh), icons, fonts
├── reports/        nơi bạn lưu báo cáo xuất ra
└── docs/           PDF minh họa
```

Luồng xử lý: `gui` gọi `dao`, `dao` dùng `connect` để nói chuyện với SQL Server, `entity` dùng chung để mang dữ liệu. `gui` không viết SQL.

## 9. Một số điểm kỹ thuật

* **Transaction** (`BaseDAO.inTransaction`): lưu hóa đơn + chi tiết + topping, thêm nhân viên + tài khoản, phiếu nhập/xuất + cập nhật tồn kho đều chạy trọn gói, lỗi thì rollback.
* **Mật khẩu** băm SHA-256, không lưu chữ thường trong CSDL.
* **Kiểm tra dữ liệu** bằng regex (`Validator`): họ tên, số điện thoại 10 số, tên đăng nhập, ngày sinh đủ 16 tuổi...
* **Giá một ly** = giá cơ bản của món (size M) + phụ thu size + giá topping. Bánh không có size, đường, đá, topping.
* **Doanh thu** tính các đơn chưa bị hủy (khách trả tiền ngay khi tạo đơn).
* **Số thứ tự đơn** (#001, #002...) tự reset mỗi ngày.
* **Kho** làm ở mức nhập/xuất cơ bản: phiếu nhập cộng tồn, phiếu xuất (hao hụt, đổ bỏ, kiểm kê) trừ tồn và không cho xuất quá số tồn; nguyên liệu dưới mức tối thiểu tô đỏ và cảnh báo ở Trang chủ.
* **Xuất báo cáo**: file CSV (mở được bằng Excel, đúng tiếng Việt). **In hóa đơn**: hộp thoại in của hệ thống, muốn ra file PDF thì chọn máy in "Microsoft Print to PDF". Thông tin quán in trên hóa đơn sửa trong `gui/util/ExportUtil.java`.

## 10. Lỗi thường gặp

| Thông báo | Cách xử lý |
|---|---|
| Không kết nối được SQL Server (TCP/IP connection failed) | SQL Server chưa chạy, chưa bật TCP/IP/cổng 1433, hoặc sai `db.host`/`db.instance`. |
| Login failed for user 'sa' | Sai mật khẩu, chưa bật chế độ SQL Authentication, hoặc `sa` đang bị Disabled. |
| Cannot open database "QuanLyCafe" | Chưa chạy `tao_bang.sql`. |
| Lỗi SSL/TLS khi kết nối | Đặt `db.encrypt=false` trong `config.properties`. |
| Chữ tiếng Việt bị lỗi font trong code | IDE chưa đặt mã hóa UTF-8. |
| Sai tên đăng nhập hoặc mật khẩu dù đúng | Chưa chạy `du_lieu_mau.sql`, hoặc đã đổi mật khẩu. |
