package entity;

public class Size {
    private String maSize;
    private String tenSize;
    private double phuThu;

    public Size() {
    }

    public Size(String maSize, String tenSize, double phuThu) {
        this.maSize = maSize;
        this.tenSize = tenSize;
        this.phuThu = phuThu;
    }

    public String getMaSize() {
        return maSize;
    }

    public void setMaSize(String maSize) {
        this.maSize = maSize;
    }

    public String getTenSize() {
        return tenSize;
    }

    public void setTenSize(String tenSize) {
        this.tenSize = tenSize;
    }

    public double getPhuThu() {
        return phuThu;
    }

    public void setPhuThu(double phuThu) {
        this.phuThu = phuThu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Size other = (Size) o;
        return maSize != null && maSize.equals(other.maSize);
    }

    @Override
    public int hashCode() {
        return maSize == null ? 0 : maSize.hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(tenSize);
    }
}
