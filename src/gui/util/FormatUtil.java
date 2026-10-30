package gui.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/** Định dạng tiền, số, ngày giờ theo kiểu Việt Nam. */
public final class FormatUtil {
    private FormatUtil() {
    }

    private static final DecimalFormatSymbols SYM = new DecimalFormatSymbols();

    static {
        SYM.setGroupingSeparator('.');
        SYM.setDecimalSeparator(',');
    }

    public static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter GIO = DateTimeFormatter.ofPattern("HH:mm");

    /** 58000 -> "58.000" */
    public static String tien(double v) {
        return new DecimalFormat("#,##0", SYM).format(Math.round(v));
    }

    /** 58000 -> "58.000đ" */
    public static String tienD(double v) {
        return tien(v) + "đ";
    }

    /** 3.5 -> "3,5"; 12 -> "12" */
    public static String soThuc(double v) {
        return new DecimalFormat("#,##0.##", SYM).format(v);
    }

    public static String ngay(LocalDate d) {
        return d == null ? "" : d.format(NGAY);
    }

    public static String ngayGio(LocalDateTime d) {
        return d == null ? "" : d.format(NGAY_GIO);
    }

    public static String gio(LocalDateTime d) {
        return d == null ? "" : d.format(GIO);
    }

    /** "150.000" -> 150000. Trả -1 nếu không hợp lệ. */
    public static double parseTien(String s) {
        if (s == null) return -1;
        String t = s.trim().replace(".", "").replace(",", "").replace("đ", "");
        if (!t.matches("\\d{1,12}")) return -1;
        return Double.parseDouble(t);
    }

    /** "3,5" hoặc "3.5" -> 3.5. Trả -1 nếu không hợp lệ. */
    public static double parseSoThuc(String s) {
        if (s == null) return -1;
        String t = s.trim().replace(',', '.');
        if (!t.matches("\\d{1,9}(\\.\\d{1,2})?")) return -1;
        return Double.parseDouble(t);
    }

    /** "21/10/2026" -> LocalDate, sai định dạng trả null. */
    public static LocalDate parseNgay(String s) {
        try {
            return LocalDate.parse(s.trim(), NGAY);
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }
}
