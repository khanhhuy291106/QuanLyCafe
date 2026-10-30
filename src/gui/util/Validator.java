package gui.util;

import java.util.regex.Pattern;

/** Kiểm tra dữ liệu nhập bằng biểu thức chính quy (regex). */
public final class Validator {
    private Validator() {
    }

    private static final Pattern SDT = Pattern.compile("^0\\d{9}$");
    private static final Pattern HO_TEN = Pattern.compile("^[\\p{L}]+([ '][\\p{L}]+)*$");
    private static final Pattern TEN_DN = Pattern.compile("^[a-zA-Z0-9._]{4,20}$");

    public static boolean sdt(String s) {
        return s != null && SDT.matcher(s.trim()).matches();
    }

    /** Họ tên chỉ gồm chữ (có dấu) và khoảng trắng, dài 2 đến 50 ký tự. */
    public static boolean hoTen(String s) {
        if (s == null) return false;
        String t = s.trim().replaceAll("\\s+", " ");
        return t.length() >= 2 && t.length() <= 50 && HO_TEN.matcher(t).matches();
    }

    public static boolean tenDangNhap(String s) {
        return s != null && TEN_DN.matcher(s.trim()).matches();
    }

    public static boolean matKhau(String s) {
        return s != null && s.length() >= 6 && s.length() <= 30;
    }

    public static boolean khongRong(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
