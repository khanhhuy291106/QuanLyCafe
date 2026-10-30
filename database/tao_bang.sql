/* =====================================================================
   QUẢN LÝ QUÁN CAFE TAKE AWAY - SQL Server
   Script 1/2: tạo cơ sở dữ liệu, bảng, ràng buộc và view.
   Chạy bằng SQL Server Management Studio (SSMS): mở file -> Execute (F5).
   CẢNH BÁO: script có lệnh DROP, chạy lại sẽ xóa dữ liệu cũ.
   ===================================================================== */
USE master;
GO
IF DB_ID(N'QuanLyCafe') IS NULL
    CREATE DATABASE QuanLyCafe;
GO
USE QuanLyCafe;
GO

-- ---------- Xóa theo thứ tự ngược khóa ngoại để chạy lại được ----------
IF OBJECT_ID('vw_HoaDonTong', 'V') IS NOT NULL DROP VIEW vw_HoaDonTong;
IF OBJECT_ID('ChiTietPhieuXuat', 'U') IS NOT NULL DROP TABLE ChiTietPhieuXuat;
IF OBJECT_ID('PhieuXuat', 'U') IS NOT NULL DROP TABLE PhieuXuat;
IF OBJECT_ID('ChiTietPhieuNhap', 'U') IS NOT NULL DROP TABLE ChiTietPhieuNhap;
IF OBJECT_ID('PhieuNhap', 'U') IS NOT NULL DROP TABLE PhieuNhap;
IF OBJECT_ID('NguyenLieu', 'U') IS NOT NULL DROP TABLE NguyenLieu;
IF OBJECT_ID('NhaCungCap', 'U') IS NOT NULL DROP TABLE NhaCungCap;
IF OBJECT_ID('ChiTietHoaDon_Topping', 'U') IS NOT NULL DROP TABLE ChiTietHoaDon_Topping;
IF OBJECT_ID('ChiTietHoaDon', 'U') IS NOT NULL DROP TABLE ChiTietHoaDon;
IF OBJECT_ID('HoaDon', 'U') IS NOT NULL DROP TABLE HoaDon;
IF OBJECT_ID('KhuyenMai', 'U') IS NOT NULL DROP TABLE KhuyenMai;
IF OBJECT_ID('Topping', 'U') IS NOT NULL DROP TABLE Topping;
IF OBJECT_ID('[Size]', 'U') IS NOT NULL DROP TABLE [Size];
IF OBJECT_ID('Mon', 'U') IS NOT NULL DROP TABLE Mon;
IF OBJECT_ID('LoaiMon', 'U') IS NOT NULL DROP TABLE LoaiMon;
IF OBJECT_ID('TaiKhoan', 'U') IS NOT NULL DROP TABLE TaiKhoan;
IF OBJECT_ID('NhanVien', 'U') IS NOT NULL DROP TABLE NhanVien;
GO

-- ---------- Nhân sự ----------
CREATE TABLE NhanVien (
    maNV      VARCHAR(10)   NOT NULL PRIMARY KEY,
    hoTen     NVARCHAR(100) NOT NULL,
    gioiTinh  BIT           NOT NULL DEFAULT 1,          -- 1 = Nam, 0 = Nữ
    ngaySinh  DATE          NOT NULL,
    sdt       VARCHAR(10)   NOT NULL,
    diaChi    NVARCHAR(200) NULL,
    chucVu    VARCHAR(20)   NOT NULL CHECK (chucVu IN ('QUAN_LY', 'THU_NGAN', 'PHA_CHE')),
    trangThai BIT           NOT NULL DEFAULT 1           -- 1 = đang làm, 0 = nghỉ việc
);

CREATE TABLE TaiKhoan (
    maTK        VARCHAR(10) NOT NULL PRIMARY KEY,
    tenDangNhap VARCHAR(30) NOT NULL UNIQUE,
    matKhau     VARCHAR(64) NOT NULL,                    -- SHA-256 dạng hex
    vaiTro      VARCHAR(20) NOT NULL CHECK (vaiTro IN ('QUAN_LY', 'THU_NGAN', 'PHA_CHE')),
    maNV        VARCHAR(10) NOT NULL UNIQUE REFERENCES NhanVien(maNV),
    trangThai   BIT         NOT NULL DEFAULT 1
);

-- ---------- Thực đơn ----------
CREATE TABLE LoaiMon (
    maLoai  VARCHAR(10) NOT NULL PRIMARY KEY,
    tenLoai NVARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE Mon (
    maMon     VARCHAR(10)   NOT NULL PRIMARY KEY,
    tenMon    NVARCHAR(100) NOT NULL,
    giaCoBan  DECIMAL(12,0) NOT NULL CHECK (giaCoBan >= 0),   -- giá của size M
    hinhAnh   NVARCHAR(260) NULL,
    maLoai    VARCHAR(10)   NOT NULL REFERENCES LoaiMon(maLoai),
    trangThai BIT           NOT NULL DEFAULT 1                -- 1 = đang bán
);

CREATE TABLE [Size] (
    maSize  VARCHAR(10)   NOT NULL PRIMARY KEY,
    tenSize NVARCHAR(10)  NOT NULL UNIQUE,
    phuThu  DECIMAL(12,0) NOT NULL DEFAULT 0                  -- cộng/trừ vào giá cơ bản
);

CREATE TABLE Topping (
    maTopping  VARCHAR(10)   NOT NULL PRIMARY KEY,
    tenTopping NVARCHAR(50)  NOT NULL UNIQUE,
    gia        DECIMAL(12,0) NOT NULL CHECK (gia >= 0),
    trangThai  BIT           NOT NULL DEFAULT 1
);

CREATE TABLE KhuyenMai (
    maKM        VARCHAR(10)   NOT NULL PRIMARY KEY,
    tenKM       NVARCHAR(100) NOT NULL,
    loaiGiam    VARCHAR(10)   NOT NULL CHECK (loaiGiam IN ('PHAN_TRAM', 'SO_TIEN')),
    giaTri      DECIMAL(12,0) NOT NULL CHECK (giaTri > 0),
    donToiThieu DECIMAL(12,0) NOT NULL DEFAULT 0,
    ngayBD      DATE          NOT NULL,
    ngayKT      DATE          NOT NULL,
    trangThai   BIT           NOT NULL DEFAULT 1,
    CONSTRAINT CK_KhuyenMai_Ngay CHECK (ngayKT >= ngayBD)
);

-- ---------- Bán hàng ----------
CREATE TABLE HoaDon (
    maHD         VARCHAR(10)   NOT NULL PRIMARY KEY,
    soThuTu      INT           NOT NULL,                      -- số gọi khách, reset mỗi ngày
    ngayLap      DATETIME      NOT NULL DEFAULT GETDATE(),
    trangThai    VARCHAR(15)   NOT NULL DEFAULT 'MOI_TAO'
                 CHECK (trangThai IN ('MOI_TAO', 'DANG_PHA_CHE', 'HOAN_THANH', 'DA_HUY')),
    phuongThucTT VARCHAR(15)   NOT NULL DEFAULT 'TIEN_MAT'
                 CHECK (phuongThucTT IN ('TIEN_MAT', 'CHUYEN_KHOAN')),
    giamGia      DECIMAL(12,0) NOT NULL DEFAULT 0,
    maNV         VARCHAR(10)   NOT NULL REFERENCES NhanVien(maNV),
    maKM         VARCHAR(10)   NULL REFERENCES KhuyenMai(maKM)
);
CREATE INDEX IX_HoaDon_NgayLap ON HoaDon(ngayLap);

CREATE TABLE ChiTietHoaDon (
    maCT     INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    maHD     VARCHAR(10)   NOT NULL REFERENCES HoaDon(maHD) ON DELETE CASCADE,
    maMon    VARCHAR(10)   NOT NULL REFERENCES Mon(maMon),
    maSize   VARCHAR(10)   NULL REFERENCES [Size](maSize),   -- NULL với bánh
    soLuong  INT           NOT NULL CHECK (soLuong > 0),
    donGia   DECIMAL(12,0) NOT NULL,                          -- giá cơ bản + phụ thu size, chưa gồm topping
    mucDuong NVARCHAR(10)  NULL,
    mucDa    NVARCHAR(10)  NULL,
    ghiChu   NVARCHAR(200) NULL
);

CREATE TABLE ChiTietHoaDon_Topping (
    maCT      INT           NOT NULL REFERENCES ChiTietHoaDon(maCT) ON DELETE CASCADE,
    maTopping VARCHAR(10)   NOT NULL REFERENCES Topping(maTopping),
    gia       DECIMAL(12,0) NOT NULL,                         -- giá topping tại thời điểm bán
    PRIMARY KEY (maCT, maTopping)
);

-- ---------- Kho ----------
CREATE TABLE NhaCungCap (
    maNCC  VARCHAR(10)   NOT NULL PRIMARY KEY,
    tenNCC NVARCHAR(100) NOT NULL,
    sdt    VARCHAR(15)   NULL,
    diaChi NVARCHAR(200) NULL
);

CREATE TABLE NguyenLieu (
    maNL        VARCHAR(10)   NOT NULL PRIMARY KEY,
    tenNL       NVARCHAR(100) NOT NULL,
    donViTinh   NVARCHAR(20)  NOT NULL,
    soLuongTon  DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (soLuongTon >= 0),
    mucToiThieu DECIMAL(12,2) NOT NULL DEFAULT 0
);

CREATE TABLE PhieuNhap (
    maPN     VARCHAR(10)   NOT NULL PRIMARY KEY,
    ngayNhap DATE          NOT NULL,
    maNV     VARCHAR(10)   NOT NULL REFERENCES NhanVien(maNV),
    maNCC    VARCHAR(10)   NOT NULL REFERENCES NhaCungCap(maNCC),
    tongTien DECIMAL(14,0) NOT NULL DEFAULT 0
);

CREATE TABLE ChiTietPhieuNhap (
    maPN    VARCHAR(10)   NOT NULL REFERENCES PhieuNhap(maPN) ON DELETE CASCADE,
    maNL    VARCHAR(10)   NOT NULL REFERENCES NguyenLieu(maNL),
    soLuong DECIMAL(12,2) NOT NULL CHECK (soLuong > 0),
    donGia  DECIMAL(12,0) NOT NULL CHECK (donGia >= 0),
    PRIMARY KEY (maPN, maNL)
);

CREATE TABLE PhieuXuat (
    maPX     VARCHAR(10)   NOT NULL PRIMARY KEY,
    ngayXuat DATE          NOT NULL,
    maNV     VARCHAR(10)   NOT NULL REFERENCES NhanVien(maNV),
    lyDo     NVARCHAR(200) NULL
);

CREATE TABLE ChiTietPhieuXuat (
    maPX    VARCHAR(10)   NOT NULL REFERENCES PhieuXuat(maPX) ON DELETE CASCADE,
    maNL    VARCHAR(10)   NOT NULL REFERENCES NguyenLieu(maNL),
    soLuong DECIMAL(12,2) NOT NULL CHECK (soLuong > 0),
    PRIMARY KEY (maPX, maNL)
);
GO

-- ---------- View: hóa đơn kèm tạm tính (đã cộng topping) ----------
CREATE VIEW vw_HoaDonTong AS
SELECT h.maHD, h.soThuTu, h.ngayLap, h.trangThai, h.phuongThucTT, h.giamGia, h.maNV, h.maKM,
       ISNULL(SUM((ct.donGia + ISNULL(tp.tongTopping, 0)) * ct.soLuong), 0) AS tamTinh
FROM HoaDon h
LEFT JOIN ChiTietHoaDon ct ON ct.maHD = h.maHD
LEFT JOIN (SELECT maCT, SUM(gia) AS tongTopping
           FROM ChiTietHoaDon_Topping
           GROUP BY maCT) tp ON tp.maCT = ct.maCT
GROUP BY h.maHD, h.soThuTu, h.ngayLap, h.trangThai, h.phuongThucTT, h.giamGia, h.maNV, h.maKM;
GO
PRINT N'Đã tạo xong cơ sở dữ liệu QuanLyCafe.';
