package entity;

import java.time.LocalDate;

public class NhanVien {
    private String maNV;
    private String hoTen;
    private boolean gioiTinh;
    private LocalDate ngaySinh;
    private String sdt;
    private String diaChi;
    private String chucVu;
    private boolean trangThai;

    public NhanVien() {
    }

    public NhanVien(String maNV, String hoTen, boolean gioiTinh, LocalDate ngaySinh, String sdt, String diaChi, String chucVu, boolean trangThai) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.gioiTinh = gioiTinh;
        this.ngaySinh = ngaySinh;
        this.sdt = sdt;
        this.diaChi = diaChi;
        this.chucVu = chucVu;
        this.trangThai = trangThai;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public boolean isGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(boolean gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public static final String QUAN_LY = "QUAN_LY";
    public static final String THU_NGAN = "THU_NGAN";
    public static final String PHA_CHE = "PHA_CHE";

    /** Tên hiển thị của chức vụ. */
    public static String tenChucVu(String ma) {
        if (QUAN_LY.equals(ma)) return "Quản lý";
        if (THU_NGAN.equals(ma)) return "Thu ngân";
        if (PHA_CHE.equals(ma)) return "Pha chế";
        return ma;
    }

    /** Đổi tên hiển thị về mã chức vụ. */
    public static String maChucVu(String ten) {
        if ("Quản lý".equals(ten)) return QUAN_LY;
        if ("Thu ngân".equals(ten)) return THU_NGAN;
        if ("Pha chế".equals(ten)) return PHA_CHE;
        return ten;
    }

    public String getGioiTinhText() {
        return gioiTinh ? "Nam" : "Nữ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NhanVien other = (NhanVien) o;
        return maNV != null && maNV.equals(other.maNV);
    }

    @Override
    public int hashCode() {
        return maNV == null ? 0 : maNV.hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(hoTen);
    }
}
