package entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PhieuXuat {
    private String maPX;
    private LocalDate ngayXuat;
    private NhanVien nhanVien;
    private String lyDo;
    private List<ChiTietPhieuXuat> dsChiTiet = new ArrayList<>();

    public PhieuXuat() {
    }

    public PhieuXuat(String maPX, LocalDate ngayXuat, NhanVien nhanVien, String lyDo) {
        this.maPX = maPX;
        this.ngayXuat = ngayXuat;
        this.nhanVien = nhanVien;
        this.lyDo = lyDo;
    }

    public String getMaPX() {
        return maPX;
    }

    public void setMaPX(String maPX) {
        this.maPX = maPX;
    }

    public LocalDate getNgayXuat() {
        return ngayXuat;
    }

    public void setNgayXuat(LocalDate ngayXuat) {
        this.ngayXuat = ngayXuat;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public List<ChiTietPhieuXuat> getDsChiTiet() {
        return dsChiTiet;
    }

    public void setDsChiTiet(List<ChiTietPhieuXuat> dsChiTiet) {
        this.dsChiTiet = dsChiTiet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhieuXuat other = (PhieuXuat) o;
        return maPX != null && maPX.equals(other.maPX);
    }

    @Override
    public int hashCode() {
        return maPX == null ? 0 : maPX.hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(maPX);
    }
}
