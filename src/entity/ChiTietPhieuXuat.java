package entity;

public class ChiTietPhieuXuat {
    private PhieuXuat phieuXuat;
    private NguyenLieu nguyenLieu;
    private double soLuong;

    public ChiTietPhieuXuat() {
    }

    public ChiTietPhieuXuat(PhieuXuat phieuXuat, NguyenLieu nguyenLieu, double soLuong) {
        this.phieuXuat = phieuXuat;
        this.nguyenLieu = nguyenLieu;
        this.soLuong = soLuong;
    }

    public PhieuXuat getPhieuXuat() {
        return phieuXuat;
    }

    public void setPhieuXuat(PhieuXuat phieuXuat) {
        this.phieuXuat = phieuXuat;
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

    @Override
    public String toString() {
        return nguyenLieu + " x" + soLuong;
    }
}
