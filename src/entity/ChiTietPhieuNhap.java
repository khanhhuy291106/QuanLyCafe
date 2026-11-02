package entity;

public class ChiTietPhieuNhap {
    private NguyenLieu nguyenLieu;
    private double soLuong;
    private double donGia;

    public ChiTietPhieuNhap() {}

    public ChiTietPhieuNhap(NguyenLieu nguyenLieu, double soLuong, double donGia) {
        this.nguyenLieu = nguyenLieu;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public NguyenLieu getNguyenLieu() { return nguyenLieu; }
    public void setNguyenLieu(NguyenLieu nguyenLieu) { this.nguyenLieu = nguyenLieu; }
    public double getSoLuong() { return soLuong; }
    public void setSoLuong(double soLuong) { this.soLuong = soLuong; }
    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public double thanhTien() {
        return soLuong * donGia;
    }
}
