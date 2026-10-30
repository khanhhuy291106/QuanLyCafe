package entity;

public class NguyenLieu {
    private String maNL;
    private String tenNL;
    private String donViTinh;
    private double soLuongTon;
    private double mucToiThieu;

    public NguyenLieu() {
    }

    public NguyenLieu(String maNL, String tenNL, String donViTinh, double soLuongTon, double mucToiThieu) {
        this.maNL = maNL;
        this.tenNL = tenNL;
        this.donViTinh = donViTinh;
        this.soLuongTon = soLuongTon;
        this.mucToiThieu = mucToiThieu;
    }

    public String getMaNL() {
        return maNL;
    }

    public void setMaNL(String maNL) {
        this.maNL = maNL;
    }

    public String getTenNL() {
        return tenNL;
    }

    public void setTenNL(String tenNL) {
        this.tenNL = tenNL;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public double getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(double soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    public double getMucToiThieu() {
        return mucToiThieu;
    }

    public void setMucToiThieu(double mucToiThieu) {
        this.mucToiThieu = mucToiThieu;
    }

    public boolean sapHet() {
        return soLuongTon < mucToiThieu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NguyenLieu other = (NguyenLieu) o;
        return maNL != null && maNL.equals(other.maNL);
    }

    @Override
    public int hashCode() {
        return maNL == null ? 0 : maNL.hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(tenNL);
    }
}
