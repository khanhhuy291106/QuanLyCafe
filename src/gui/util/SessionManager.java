package gui.util;

import entity.NhanVien;
import entity.TaiKhoan;

/** Lưu người đang đăng nhập để mọi màn hình cùng dùng. */
public final class SessionManager {
    private SessionManager() {
    }

    private static TaiKhoan current;

    public static void login(TaiKhoan tk) {
        current = tk;
    }

    public static void logout() {
        current = null;
    }

    public static TaiKhoan getTaiKhoan() {
        return current;
    }

    public static NhanVien getNhanVien() {
        return current == null ? null : current.getNhanVien();
    }

    public static String getVaiTro() {
        return current == null ? null : current.getVaiTro();
    }

    public static boolean laQuanLy() {
        return NhanVien.QUAN_LY.equals(getVaiTro());
    }

    public static boolean laPhaChe() {
        return NhanVien.PHA_CHE.equals(getVaiTro());
    }

    /** "Nguyễn Văn A (Thu ngân)" */
    public static String moTa() {
        NhanVien nv = getNhanVien();
        return nv == null ? "" : nv.getHoTen() + " (" + NhanVien.tenChucVu(getVaiTro()) + ")";
    }
}
