package entity;

public class ChiTietPhieuNhap {
    private PhieuNhap phieuNhap;
    private NguyenLieu nguyenLieu;
    private double soLuong;
    private double donGia;

    public ChiTietPhieuNhap() {
    }

    public ChiTietPhieuNhap(PhieuNhap phieuNhap, NguyenLieu nguyenLieu, double soLuong, double donGia) {
        this.phieuNhap = phieuNhap;
        this.nguyenLieu = nguyenLieu;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public PhieuNhap getPhieuNhap() {
        return phieuNhap;
    }

    public void setPhieuNhap(PhieuNhap phieuNhap) {
        this.phieuNhap = phieuNhap;
    }

    public NguyenLieu getNguyenLieu() {
        return nguyenLieu;
    }

    public void setNguyenLieu(NguyenLieu nguyenLieu) {
        this.nguyenLieu = nguyenLieu;
    }

    public double getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(double soLuong) {
        this.soLuong = soLuong;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    public double thanhTien() {
        return soLuong * donGia;
    }

    @Override
    public String toString() {
        return nguyenLieu + " x" + soLuong;
    }
}
