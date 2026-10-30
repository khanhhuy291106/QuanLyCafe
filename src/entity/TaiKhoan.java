package entity;

public class TaiKhoan {
    private String maTK;
    private String tenDangNhap;
    private String matKhau;
    private String vaiTro;
    private NhanVien nhanVien;
    private boolean trangThai;

    public TaiKhoan() {
    }

    public TaiKhoan(String maTK, String tenDangNhap, String matKhau, String vaiTro, NhanVien nhanVien, boolean trangThai) {
        this.maTK = maTK;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.nhanVien = nhanVien;
        this.trangThai = trangThai;
    }

    public String getMaTK() {
        return maTK;
    }

    public void setMaTK(String maTK) {
        this.maTK = maTK;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaiKhoan other = (TaiKhoan) o;
        return maTK != null && maTK.equals(other.maTK);
    }

    @Override
    public int hashCode() {
        return maTK == null ? 0 : maTK.hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(tenDangNhap);
    }
}
