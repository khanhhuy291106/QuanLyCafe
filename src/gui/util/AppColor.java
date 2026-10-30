package gui.util;

import java.awt.Color;
import java.awt.Font;

/** Bảng màu và font dùng chung để toàn bộ giao diện đồng nhất. */
public final class AppColor {
    private AppColor() {
    }

    public static final Color INK = new Color(0x2B2118);
    public static final Color BROWN = new Color(0x6F4E37);
    public static final Color DARK = new Color(0x3E2A1E);
    public static final Color SIDEBAR = new Color(0x4A3426);
    public static final Color CREAM = new Color(0xF7F0E6);
    public static final Color LATTE = new Color(0xE8D9C5);
    public static final Color ACCENT = new Color(0xC8873A);
    public static final Color GREEN = new Color(0x4C9A6A);
    public static final Color RED = new Color(0xC0504D);
    public static final Color BLUE = new Color(0x4A7FB5);
    public static final Color ORANGE = new Color(0xE0902B);
    public static final Color GRAY = new Color(0x8A8178);
    public static final Color LINE = new Color(0xD9CBB8);
    public static final Color BG = new Color(0xFBF8F3);
    public static final Color WHITE = Color.WHITE;
    public static final Color SELECT = new Color(0xF6DDB0);
    public static final Color ZEBRA = new Color(0xFAF5EC);
    public static final Color WARN_BG = new Color(0xFBE3E1);

    public static final String FONT_NAME = "Segoe UI";
    public static final Font FONT = new Font(FONT_NAME, Font.PLAIN, 13);
    public static final Font FONT_B = new Font(FONT_NAME, Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font(FONT_NAME, Font.PLAIN, 11);
    public static final Font FONT_TITLE = new Font(FONT_NAME, Font.BOLD, 18);
    public static final Font FONT_H2 = new Font(FONT_NAME, Font.BOLD, 14);

    /** Màu nhãn trạng thái (đơn hàng, món, nhân viên, khuyến mãi). */
    public static Color statusColor(String text) {
        if (text == null) return GRAY;
        switch (text) {
            case "Mới tạo":
            case "Sắp diễn ra":
                return BLUE;
            case "Đang pha chế":
                return ORANGE;
            case "Hoàn thành":
            case "Đang bán":
            case "Đang làm":
            case "Đang áp dụng":
            case "Đủ":
                return GREEN;
            case "Đã hủy":
            case "Sắp hết":
                return RED;
            default:
                return GRAY;
        }
    }
}
