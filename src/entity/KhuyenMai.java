package entity;

import java.time.LocalDate;

public class KhuyenMai {
    private String maKM;
    private String tenKM;
    private String loaiGiam;
    private double giaTri;
    private double donToiThieu;
    private LocalDate ngayBD;
    private LocalDate ngayKT;
    private boolean trangThai;

    public KhuyenMai() {
    }

    public KhuyenMai(String maKM, String tenKM, String loaiGiam, double giaTri, double donToiThieu, LocalDate ngayBD, LocalDate ngayKT, boolean trangThai) {
        this.maKM = maKM;
        this.tenKM = tenKM;
        this.loaiGiam = loaiGiam;
        this.giaTri = giaTri;
        this.donToiThieu = donToiThieu;
        this.ngayBD = ngayBD;
        this.ngayKT = ngayKT;
        this.trangThai = trangThai;
    }

    public String getMaKM() {
        return maKM;
    }

    public void setMaKM(String maKM) {
        this.maKM = maKM;
    }

    public String getTenKM() {
        return tenKM;
    }

    public void setTenKM(String tenKM) {
        this.tenKM = tenKM;
    }

    public String getLoaiGiam() {
        return loaiGiam;
    }

    public void setLoaiGiam(String loaiGiam) {
        this.loaiGiam = loaiGiam;
    }

    public double getGiaTri() {
        return giaTri;
    }

    public void setGiaTri(double giaTri) {
        this.giaTri = giaTri;
    }

    public double getDonToiThieu() {
        return donToiThieu;
    }

    public void setDonToiThieu(double donToiThieu) {
        this.donToiThieu = donToiThieu;
    }

    public LocalDate getNgayBD() {
        return ngayBD;
    }

    public void setNgayBD(LocalDate ngayBD) {
        this.ngayBD = ngayBD;
    }

    public LocalDate getNgayKT() {
        return ngayKT;
    }

    public void setNgayKT(LocalDate ngayKT) {
        this.ngayKT = ngayKT;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public static final String PHAN_TRAM = "PHAN_TRAM";
    public static final String SO_TIEN = "SO_TIEN";

    /** Số tiền được giảm cho một đơn có tạm tính cho trước. */
    public double tinhGiam(double tamTinh) {
        if (PHAN_TRAM.equals(loaiGiam)) return Math.round(tamTinh * giaTri / 100.0);
        return Math.min(giaTri, tamTinh);
    }

    /** Khuyến mãi có dùng được cho đơn này vào ngày này không. */
    public boolean apDungDuoc(double tamTinh, LocalDate ngay) {
        return trangThai && !ngay.isBefore(ngayBD) && !ngay.isAfter(ngayKT) && tamTinh >= donToiThieu;
    }

    /** Trạng thái hiển thị trong bảng. */
    public String tenHieuLuc(LocalDate ngay) {
        if (!trangThai) return "Hết hạn";
        if (ngay.isBefore(ngayBD)) return "Sắp diễn ra";
        if (ngay.isAfter(ngayKT)) return "Hết hạn";
        return "Đang áp dụng";
    }

    public String getGiaTriText() {
        return PHAN_TRAM.equals(loaiGiam) ? ((long) giaTri) + "%" : String.format("%,.0f", giaTri).replace(',', '.') + "đ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KhuyenMai other = (KhuyenMai) o;
        return maKM != null && maKM.equals(other.maKM);
    }

    @Override
    public int hashCode() {
        return maKM == null ? 0 : maKM.hashCode();
    }

    @Override
    public String toString() {
        return maKM + " - " + tenKM;
    }
}
