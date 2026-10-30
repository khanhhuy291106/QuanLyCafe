package entity;

public class NhaCungCap {
    private String maNCC;
    private String tenNCC;
    private String sdt;
    private String diaChi;

    public NhaCungCap() {
    }

    public NhaCungCap(String maNCC, String tenNCC, String sdt, String diaChi) {
        this.maNCC = maNCC;
        this.tenNCC = tenNCC;
        this.sdt = sdt;
        this.diaChi = diaChi;
    }

    public String getMaNCC() {
        return maNCC;
    }

    public void setMaNCC(String maNCC) {
        this.maNCC = maNCC;
    }

    public String getTenNCC() {
        return tenNCC;
    }

    public void setTenNCC(String tenNCC) {
        this.tenNCC = tenNCC;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NhaCungCap other = (NhaCungCap) o;
        return maNCC != null && maNCC.equals(other.maNCC);
    }

    @Override
    public int hashCode() {
        return maNCC == null ? 0 : maNCC.hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(tenNCC);
    }
}
