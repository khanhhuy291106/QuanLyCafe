package entity;

import java.util.ArrayList;
import java.util.List;

public class ChiTietHoaDon {
    private HoaDon hoaDon;
    private Mon mon;
    private Size size;
    private int soLuong;
    private double donGia;
    private String mucDuong;
    private String mucDa;
    private String ghiChu;
    private List<Topping> dsTopping = new ArrayList<>();

    public ChiTietHoaDon() {
    }

    public ChiTietHoaDon(HoaDon hoaDon, Mon mon, Size size, int soLuong, double donGia, String mucDuong, String mucDa, String ghiChu) {
        this.hoaDon = hoaDon;
        this.mon = mon;
        this.size = size;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.mucDuong = mucDuong;
        this.mucDa = mucDa;
        this.ghiChu = ghiChu;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public Mon getMon() {
        return mon;
    }

    public void setMon(Mon mon) {
        this.mon = mon;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    public String getMucDuong() {
        return mucDuong;
    }

    public void setMucDuong(String mucDuong) {
        this.mucDuong = mucDuong;
    }

    public String getMucDa() {
        return mucDa;
    }

    public void setMucDa(String mucDa) {
        this.mucDa = mucDa;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public List<Topping> getDsTopping() {
        return dsTopping;
    }

    public void setDsTopping(List<Topping> dsTopping) {
        this.dsTopping = dsTopping;
    }

    /** Giá một ly gồm cả topping. */
    public double tinhGiaMotLy() {
        double s = donGia;
        for (Topping t : dsTopping) s += t.getGia();
        return s;
    }

    public double tinhThanhTien() {
        return tinhGiaMotLy() * soLuong;
    }

    /** Mô tả ngắn các tùy chọn, ví dụ: M, 70% đường, đá vừa, +Trân châu. */
    public String moTaTuyChon() {
        StringBuilder sb = new StringBuilder();
        if (size != null) sb.append(size.getTenSize());
        if (mucDuong != null && !mucDuong.isEmpty()) sb.append(sb.length() > 0 ? ", " : "").append(mucDuong).append(" đường");
        if (mucDa != null && !mucDa.isEmpty()) sb.append(sb.length() > 0 ? ", " : "").append("đá ").append(mucDa.toLowerCase());
        for (Topping t : dsTopping) sb.append(sb.length() > 0 ? ", +" : "+").append(t.getTenTopping());
        return sb.toString();
    }

    @Override
    public String toString() {
        return mon + " x" + soLuong;
    }
}
