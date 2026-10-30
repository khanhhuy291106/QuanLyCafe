package entity;

public class Mon {
    private String maMon;
    private String tenMon;
    private double giaCoBan;
    private String hinhAnh;
    private LoaiMon loaiMon;
    private boolean trangThai;

    public Mon() {
    }

    public Mon(String maMon, String tenMon, double giaCoBan, String hinhAnh, LoaiMon loaiMon, boolean trangThai) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.giaCoBan = giaCoBan;
        this.hinhAnh = hinhAnh;
        this.loaiMon = loaiMon;
        this.trangThai = trangThai;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public double getGiaCoBan() {
        return giaCoBan;
    }

    public void setGiaCoBan(double giaCoBan) {
        this.giaCoBan = giaCoBan;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public LoaiMon getLoaiMon() {
        return loaiMon;
    }

    public void setLoaiMon(LoaiMon loaiMon) {
        this.loaiMon = loaiMon;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    /** Đồ uống thì có size, đường, đá, topping. Loại "Bánh" thì không. */
    public boolean laDoUong() {
        return loaiMon == null || !"Bánh".equalsIgnoreCase(loaiMon.getTenLoai());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mon other = (Mon) o;
        return maMon != null && maMon.equals(other.maMon);
    }

    @Override
    public int hashCode() {
        return maMon == null ? 0 : maMon.hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(tenMon);
    }
}
