package entity;

public class LoaiMon {
    private String maLoai;
    private String tenLoai;

    public LoaiMon() {
    }

    public LoaiMon(String maLoai, String tenLoai) {
        this.maLoai = maLoai;
        this.tenLoai = tenLoai;
    }

    public String getMaLoai() {
        return maLoai;
    }

    public void setMaLoai(String maLoai) {
        this.maLoai = maLoai;
    }

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoaiMon other = (LoaiMon) o;
        return maLoai != null && maLoai.equals(other.maLoai);
    }

    @Override
    public int hashCode() {
        return maLoai == null ? 0 : maLoai.hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(tenLoai);
    }
}
