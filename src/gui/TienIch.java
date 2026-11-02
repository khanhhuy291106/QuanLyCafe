package gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.IntPredicate;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

// Màu sắc, font, định dạng tiền/ngày và vài hàm dựng giao diện dùng chung
public class TienIch {
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
    public static final Color SELECT = new Color(0xF6DDB0);
    public static final Color WARN_BG = new Color(0xFBE3E1);

    public static final String FONT_NAME = "Segoe UI";
    public static final Font FONT = new Font(FONT_NAME, Font.PLAIN, 13);
    public static final Font FONT_B = new Font(FONT_NAME, Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font(FONT_NAME, Font.PLAIN, 11);
    public static final Font FONT_H2 = new Font(FONT_NAME, Font.BOLD, 14);
    public static final Font FONT_TITLE = new Font(FONT_NAME, Font.BOLD, 18);

    // Màu chữ của trạng thái (đơn hàng, món, nhân viên, khuyến mãi, kho)
    public static Color mauTrangThai(String text) {
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

    // ------------------------------------------------------------ định dạng
    private static DecimalFormatSymbols kyHieu() {
        DecimalFormatSymbols s = new DecimalFormatSymbols();
        s.setGroupingSeparator('.');
        s.setDecimalSeparator(',');
        return s;
    }

    // 58000 -> "58.000"
    public static String tien(double v) {
        return new DecimalFormat("#,##0", kyHieu()).format(Math.round(v));
    }

    // 58000 -> "58.000đ"
    public static String tienD(double v) {
        return tien(v) + "đ";
    }

    // 3.5 -> "3,5"; 12 -> "12"
    public static String soThuc(double v) {
        return new DecimalFormat("#,##0.##", kyHieu()).format(v);
    }

    public static String ngay(LocalDate d) {
        return d == null ? "" : d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public static String ngayGio(LocalDateTime d) {
        return d == null ? "" : d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public static String gio(LocalDateTime d) {
        return d == null ? "" : d.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    // "150.000" -> 150000. Trả -1 nếu không hợp lệ
    public static double parseTien(String s) {
        String t = s.trim().replace(".", "").replace(",", "").replace("đ", "");
        if (!t.matches("\\d{1,12}")) return -1;
        return Double.parseDouble(t);
    }

    // "3,5" hoặc "3.5" -> 3.5. Trả -1 nếu không hợp lệ
    public static double parseSoThuc(String s) {
        String t = s.trim().replace(',', '.');
        if (!t.matches("\\d{1,9}(\\.\\d{1,2})?")) return -1;
        return Double.parseDouble(t);
    }

    // ------------------------------------------------------------ giao diện
    public static JButton nut(String text, Color nen) {
        JButton b = new JButton(text);
        b.setBackground(nen);
        b.setForeground(Color.WHITE);
        b.setFont(FONT_B);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    // Nút phụ: nền kem, chữ nâu
    public static JButton nutPhu(String text) {
        JButton b = nut(text, LATTE);
        b.setForeground(BROWN);
        return b;
    }

    public static JLabel tieuDe(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_H2);
        l.setForeground(BROWN);
        return l;
    }

    public static JLabel ghiChu(String text) {
        JLabel l = new JLabel("<html>" + text + "</html>");
        l.setFont(FONT_SMALL);
        l.setForeground(GRAY);
        return l;
    }

    // Thêm một dòng "nhãn + ô nhập" vào form dùng GridBagLayout
    public static void themDong(JPanel form, int dong, String nhan, JComponent o) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = dong;
        g.anchor = GridBagConstraints.WEST;
        JLabel l = new JLabel(nhan);
        l.setPreferredSize(new Dimension(110, 28));
        g.gridx = 0;
        g.insets = new Insets(4, 0, 4, 6);
        form.add(l, g);
        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 0);
        form.add(o, g);
    }

    // Đẩy các dòng của form lên trên (gọi sau dòng cuối cùng)
    public static void themKhoangTrong(JPanel form, int dong) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = dong;
        g.weighty = 1;
        form.add(Box.createVerticalGlue(), g);
    }

    public static DefaultTableModel moHinh(String... cot) {
        return new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    public static JTable bang(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(28);
        t.setSelectionBackground(SELECT);
        t.setSelectionForeground(INK);
        t.setFillsViewportHeight(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setBackground(BROWN);
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setFont(FONT_B);
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    public static JScrollPane cuon(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(LINE));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    public static void doRong(JTable t, int... w) {
        for (int i = 0; i < w.length && i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
    }

    // Renderer cho một cột: căn lề, và tô nền đỏ nhạt cho dòng cảnh báo (nếu có)
    public static class CotRenderer extends DefaultTableCellRenderer {
        private int canLe;
        private IntPredicate canhBao;

        public CotRenderer(int canLe, IntPredicate canhBao) {
            this.canLe = canLe;
            this.canhBao = canhBao;
        }

        public int getCanLe() {
            return canLe;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, false, r, c);
            setHorizontalAlignment(canLe);
            setForeground(INK);
            if (sel) setBackground(SELECT);
            else if (canhBao != null && canhBao.test(r)) setBackground(WARN_BG);
            else setBackground(Color.WHITE);
            return this;
        }
    }

    public static void canPhai(JTable t, int... cot) {
        for (int c : cot) t.getColumnModel().getColumn(c).setCellRenderer(new CotRenderer(SwingConstants.RIGHT, null));
    }

    public static void canGiua(JTable t, int... cot) {
        for (int c : cot) t.getColumnModel().getColumn(c).setCellRenderer(new CotRenderer(SwingConstants.CENTER, null));
    }

    // Tô nền đỏ nhạt cho các dòng cảnh báo (ví dụ nguyên liệu sắp hết). Gọi SAU khi đã căn lề các cột.
    public static void tomauCanhBao(JTable t, IntPredicate laCanhBao) {
        for (int c = 0; c < t.getColumnCount(); c++) {
            int le = SwingConstants.LEFT;
            if (t.getColumnModel().getColumn(c).getCellRenderer() instanceof CotRenderer) {
                le = ((CotRenderer) t.getColumnModel().getColumn(c).getCellRenderer()).getCanLe();
            }
            t.getColumnModel().getColumn(c).setCellRenderer(new CotRenderer(le, laCanhBao));
        }
    }

    // Chữ trạng thái trong bảng có màu theo trạng thái
    public static void tomauTrangThai(JTable t, int cot) {
        t.getColumnModel().getColumn(cot).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(tb, v, sel, false, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(FONT_B);
                setForeground(mauTrangThai(v == null ? null : v.toString()));
                setBackground(sel ? SELECT : Color.WHITE);
                return this;
            }
        });
    }

    // Vẽ ly cà phê đơn giản (dùng cho logo và biểu tượng cửa sổ)
    public static void veLyCafe(Graphics g, int x, int y, int size, Color mau) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(mau);
        g2.fillRoundRect(x + size / 6, y + size / 3, size / 2, size / 3, size / 8, size / 8);
        g2.setStroke(new BasicStroke(Math.max(1.5f, size * 0.06f)));
        g2.drawArc(x + size / 2 + size / 12, y + size * 2 / 5, size / 5, size / 5, -90, 180);
        g2.fillRoundRect(x + size / 12, y + size * 2 / 3 + 2, size * 2 / 3, size / 14 + 1, 4, 4);
        for (int i = 0; i < 3; i++) {
            int sx = x + size / 4 + i * size / 6;
            g2.drawArc(sx, y + size / 10, size / 10, size / 5, 90, 180);
        }
        g2.dispose();
    }

    public static Image bieuTuong() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(BROWN);
        g.fillRoundRect(0, 0, 64, 64, 16, 16);
        veLyCafe(g, 2, 2, 60, CREAM);
        g.dispose();
        return img;
    }

    public static ImageIcon bieuTuongLy(int size, Color mau) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        veLyCafe(g, 0, 0, size, mau);
        g.dispose();
        return new ImageIcon(img);
    }

    // Đọc ảnh món trong thư mục image/mon, thu nhỏ vừa khung rong x cao. Không có ảnh thì trả null.
    public static ImageIcon docAnhMon(String hinhAnh, int rong, int cao) {
        if (hinhAnh == null || hinhAnh.trim().isEmpty()) return null;
        String ten = new java.io.File(hinhAnh.trim()).getName();
        if (!ten.contains(".")) ten += ".png";
        java.io.File f = new java.io.File("image/mon/" + ten);
        if (!f.exists()) return null;
        Image img = new ImageIcon(f.getPath()).getImage();
        int w = img.getWidth(null), h = img.getHeight(null);
        if (w <= 0 || h <= 0) return null;
        double tiLe = Math.min((double) rong / w, (double) cao / h);
        return new ImageIcon(img.getScaledInstance((int) (w * tiLe), (int) (h * tiLe), Image.SCALE_SMOOTH));
    }

    // ------------------------------------------------------------ khung màn hình "form bên trái + bảng bên phải"
    // Thẻ form bên trái: tiêu đề, form, các nút
    public static JPanel theForm(String tieuDe, int rong, JPanel form, JPanel pnlNut) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(12, 14, 14, 14)));
        card.setPreferredSize(new Dimension(rong, 100));
        card.add(tieuDe(tieuDe), BorderLayout.NORTH);
        form.setOpaque(false);
        card.add(form, BorderLayout.CENTER);
        pnlNut.setOpaque(false);
        card.add(pnlNut, BorderLayout.SOUTH);
        return card;
    }

    // Các nút Thêm / Sửa / Xóa / Làm mới xếp 2 cột
    public static JPanel pnlNutCrud(JButton... nut) {
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        for (JButton b : nut) {
            p.add(b);
        }
        return p;
    }

    // Thanh tìm kiếm: ô nhập + nút Tìm
    public static JPanel thanhTim(JTextField txtTim, JButton btnTim) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setOpaque(false);
        p.add(txtTim);
        p.add(btnTim);
        return p;
    }

    // Bên phải: thanh tìm kiếm (trên), bảng (giữa), ghi chú (dưới, có thể null)
    public static JPanel phaiBang(JPanel thanhTim, JTable tbl, JComponent ghiChu) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        if (thanhTim != null) p.add(thanhTim, BorderLayout.NORTH);
        p.add(cuon(tbl), BorderLayout.CENTER);
        if (ghiChu != null) p.add(ghiChu, BorderLayout.SOUTH);
        return p;
    }

    // Đặt khung chuẩn cho một màn hình CRUD: thẻ form ở WEST, phần bảng ở CENTER
    public static void xepCrud(JPanel man, JPanel theForm, JPanel phai) {
        man.setLayout(new BorderLayout(12, 0));
        man.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        man.setBackground(BG);
        man.add(theForm, BorderLayout.WEST);
        man.add(phai, BorderLayout.CENTER);
    }

    public static final String CHUA_CO_MA = "(tự sinh khi thêm)";

    public static JTextField oMa() {
        JTextField t = new JTextField(10);
        t.setEditable(false);
        t.setBackground(new Color(0xF0EBE3));
        return t;
    }
}
