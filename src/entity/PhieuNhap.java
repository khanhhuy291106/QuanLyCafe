package entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PhieuNhap {
    private String maPN;
    private LocalDate ngayNhap;
    private NhanVien nhanVien;
    private NhaCungCap nhaCungCap;
    private double tongTien;
    private List<ChiTietPhieuNhap> dsChiTiet = new ArrayList<>();

    public PhieuNhap() {
    }

    public PhieuNhap(String maPN, LocalDate ngayNhap, NhanVien nhanVien, NhaCungCap nhaCungCap, double tongTien) {
        this.maPN = maPN;
        this.ngayNhap = ngayNhap;
        this.nhanVien = nhanVien;
        this.nhaCungCap = nhaCungCap;
        this.tongTien = tongTien;
    }

    public String getMaPN() {
        return maPN;
    }

    public void setMaPN(String maPN) {
        this.maPN = maPN;
    }

    public LocalDate getNgayNhap() {
        return ngayNhap;
    }

    public void setNgayNhap(LocalDate ngayNhap) {
        this.ngayNhap = ngayNhap;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public NhaCungCap getNhaCungCap() {
        return nhaCungCap;
    }

    public void setNhaCungCap(NhaCungCap nhaCungCap) {
        this.nhaCungCap = nhaCungCap;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public List<ChiTietPhieuNhap> getDsChiTiet() {
        return dsChiTiet;
    }

    public void setDsChiTiet(List<ChiTietPhieuNhap> dsChiTiet) {
        this.dsChiTiet = dsChiTiet;
    }

    public double tinhTong() {
        double s = 0;
        for (ChiTietPhieuNhap ct : dsChiTiet) s += ct.thanhTien();
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhieuNhap other = (PhieuNhap) o;
        return maPN != null && maPN.equals(other.maPN);
    }

    @Override
    public int hashCode() {
        return maPN == null ? 0 : maPN.hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(maPN);
    }
}
