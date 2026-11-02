package entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HoaDon {
    private String maHD;
    private int soThuTu;
    private LocalDateTime ngayLap;
    private String trangThai;
    private String phuongThucTT;
    private double giamGia;
    private NhanVien nhanVien;
    private KhuyenMai khuyenMai;
    private double tamTinh;
    private List<ChiTietHoaDon> dsChiTiet = new ArrayList<>();

    public HoaDon() {}

    public HoaDon(String maHD, int soThuTu, LocalDateTime ngayLap, String trangThai, String phuongThucTT, double giamGia, NhanVien nhanVien, KhuyenMai khuyenMai, double tamTinh) {
        this.maHD = maHD;
        this.soThuTu = soThuTu;
        this.ngayLap = ngayLap;
        this.trangThai = trangThai;
        this.phuongThucTT = phuongThucTT;
        this.giamGia = giamGia;
        this.nhanVien = nhanVien;
        this.khuyenMai = khuyenMai;
        this.tamTinh = tamTinh;
    }

    public String getMaHD() { return maHD; }
    public void setMaHD(String maHD) { this.maHD = maHD; }
    public int getSoThuTu() { return soThuTu; }
    public void setSoThuTu(int soThuTu) { this.soThuTu = soThuTu; }
    public LocalDateTime getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDateTime ngayLap) { this.ngayLap = ngayLap; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getPhuongThucTT() { return phuongThucTT; }
    public void setPhuongThucTT(String phuongThucTT) { this.phuongThucTT = phuongThucTT; }
    public double getGiamGia() { return giamGia; }
    public void setGiamGia(double giamGia) { this.giamGia = giamGia; }
    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }
    public KhuyenMai getKhuyenMai() { return khuyenMai; }
    public void setKhuyenMai(KhuyenMai khuyenMai) { this.khuyenMai = khuyenMai; }
    public double getTamTinh() { return tamTinh; }
    public void setTamTinh(double tamTinh) { this.tamTinh = tamTinh; }
    public List<ChiTietHoaDon> getDsChiTiet() { return dsChiTiet; }
    public void setDsChiTiet(List<ChiTietHoaDon> dsChiTiet) { this.dsChiTiet = dsChiTiet; }

    public static final String MOI_TAO = "MOI_TAO";
    public static final String DANG_PHA_CHE = "DANG_PHA_CHE";
    public static final String HOAN_THANH = "HOAN_THANH";
    public static final String DA_HUY = "DA_HUY";
    public static final String TIEN_MAT = "TIEN_MAT";
    public static final String CHUYEN_KHOAN = "CHUYEN_KHOAN";

    public static String tenTrangThai(String ma) {
        if (MOI_TAO.equals(ma)) return "Mới tạo";
        if (DANG_PHA_CHE.equals(ma)) return "Đang pha chế";
        if (HOAN_THANH.equals(ma)) return "Hoàn thành";
        if (DA_HUY.equals(ma)) return "Đã hủy";
        return ma;
    }

    public static String maTrangThai(String ten) {
        if ("Mới tạo".equals(ten)) return MOI_TAO;
        if ("Đang pha chế".equals(ten)) return DANG_PHA_CHE;
        if ("Hoàn thành".equals(ten)) return HOAN_THANH;
        if ("Đã hủy".equals(ten)) return DA_HUY;
        return null;
    }

    public static String tenPhuongThuc(String ma) {
        return CHUYEN_KHOAN.equals(ma) ? "Chuyển khoản" : "Tiền mặt";
    }

    // Tổng tiền các dòng (chưa trừ khuyến mãi). Khi chưa nạp chi tiết thì dùng tạm tính lấy từ CSDL.
    public double tinhTamTinh() {
        if (dsChiTiet.isEmpty()) return tamTinh;
        double s = 0;
        for (ChiTietHoaDon ct : dsChiTiet) s += ct.tinhThanhTien();
        return s;
    }

    // Số tiền khách phải trả
    public double tinhTongTien() {
        return Math.max(0, tinhTamTinh() - giamGia);
    }
}
