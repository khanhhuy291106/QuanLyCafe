package entity;

public class ChiTietPhieuXuat {
    private NguyenLieu nguyenLieu;
    private double soLuong;

    public ChiTietPhieuXuat() {}

    public ChiTietPhieuXuat(NguyenLieu nguyenLieu, double soLuong) {
        this.nguyenLieu = nguyenLieu;
        this.soLuong = soLuong;
    }

    public NguyenLieu getNguyenLieu() { return nguyenLieu; }
    public void setNguyenLieu(NguyenLieu nguyenLieu) { this.nguyenLieu = nguyenLieu; }
    public double getSoLuong() { return soLuong; }
    public void setSoLuong(double soLuong) { this.soLuong = soLuong; }
}
