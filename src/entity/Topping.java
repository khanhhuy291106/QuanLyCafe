package entity;

public class Topping {
    private String maTopping;
    private String tenTopping;
    private double gia;
    private boolean trangThai;

    public Topping() {
    }

    public Topping(String maTopping, String tenTopping, double gia, boolean trangThai) {
        this.maTopping = maTopping;
        this.tenTopping = tenTopping;
        this.gia = gia;
        this.trangThai = trangThai;
    }

    public String getMaTopping() {
        return maTopping;
    }

    public void setMaTopping(String maTopping) {
        this.maTopping = maTopping;
    }

    public String getTenTopping() {
        return tenTopping;
    }

    public void setTenTopping(String tenTopping) {
        this.tenTopping = tenTopping;
    }

    public double getGia() {
        return gia;
    }

    public void setGia(double gia) {
        this.gia = gia;
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
        Topping other = (Topping) o;
        return maTopping != null && maTopping.equals(other.maTopping);
    }

    @Override
    public int hashCode() {
        return maTopping == null ? 0 : maTopping.hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(tenTopping);
    }
}
