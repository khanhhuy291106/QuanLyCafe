package gui.component;

import dao.DaoException;
import gui.util.AppColor;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Enumeration;

/** Các hàm tiện ích dựng giao diện: nút, ô nhập, bảng, hộp thoại thông báo, chạy lệnh an toàn. */
public final class UI {
    private UI() {
    }

    // ------------------------------------------------------------ giao diện chung
    public static void installTheme() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        UIManager.put("nimbusBase", AppColor.BROWN);
        UIManager.put("nimbusFocus", AppColor.ACCENT);
        UIManager.put("nimbusSelectionBackground", AppColor.ACCENT);
        UIManager.put("control", AppColor.BG);
        UIManager.put("OptionPane.background", AppColor.BG);
        UIManager.put("Panel.background", AppColor.BG);
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object k = keys.nextElement();
            if (UIManager.get(k) instanceof FontUIResource) UIManager.put(k, new FontUIResource(AppColor.FONT));
        }
        UIManager.put("OptionPane.yesButtonText", "Có");
        UIManager.put("OptionPane.noButtonText", "Không");
        UIManager.put("OptionPane.cancelButtonText", "Hủy");
        UIManager.put("OptionPane.okButtonText", "Đồng ý");
    }

    /** Biểu tượng cửa sổ: ly cà phê trên nền nâu. */
    public static Image appIcon() {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(64, 64, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(AppColor.BROWN);
        g.fillRoundRect(0, 0, 64, 64, 16, 16);
        new CupIcon(64, AppColor.CREAM, true).paintIcon(null, g, 0, 0);
        g.dispose();
        return img;
    }

    // ------------------------------------------------------------ thành phần cơ bản
    public static BoGocButton button(String text, Color bg) {
        return new BoGocButton(text, bg);
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppColor.FONT);
        l.setForeground(AppColor.INK);
        return l;
    }

    public static JLabel bold(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppColor.FONT_B);
        l.setForeground(AppColor.INK);
        return l;
    }

    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppColor.FONT_H2);
        l.setForeground(AppColor.BROWN);
        return l;
    }

    public static JLabel muted(String text) {
        JLabel l = new JLabel(text.length() > 70 && !text.startsWith("<html>") ? "<html>" + text + "</html>" : text);
        l.setFont(AppColor.FONT_SMALL);
        l.setForeground(AppColor.GRAY);
        return l;
    }

    public static Border fieldBorder() {
        return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(AppColor.LINE),
                BorderFactory.createEmptyBorder(3, 6, 3, 6));
    }

    public static JTextField field(int columns) {
        JTextField f = new JTextField(columns);
        f.setFont(AppColor.FONT);
        f.setBorder(fieldBorder());
        f.setPreferredSize(new Dimension(f.getPreferredSize().width, 30));
        return f;
    }

    public static JPasswordField password(int columns) {
        JPasswordField f = new JPasswordField(columns);
        f.setFont(AppColor.FONT);
        f.setBorder(fieldBorder());
        f.setPreferredSize(new Dimension(f.getPreferredSize().width, 30));
        return f;
    }

    public static JTextField readOnly(int columns) {
        JTextField f = field(columns);
        f.setEditable(false);
        f.setBackground(new Color(0xF0EBE3));
        return f;
    }

    public static <T> JComboBox<T> combo(T[] items) {
        JComboBox<T> c = new JComboBox<>(items);
        c.setFont(AppColor.FONT);
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, 30));
        return c;
    }

    public static JCheckBox check(String text) {
        JCheckBox c = new JCheckBox(text);
        c.setOpaque(false);
        c.setFont(AppColor.FONT);
        return c;
    }

    public static JRadioButton radio(String text) {
        JRadioButton r = new JRadioButton(text);
        r.setOpaque(false);
        r.setFont(AppColor.FONT);
        return r;
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(AppColor.LINE));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    // ------------------------------------------------------------ bảng
    public static DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    /** Dòng so le trắng/kem, chọn dòng thì tô vàng nhạt. */
    public static class ZebraRenderer extends DefaultTableCellRenderer {
        private final int align;
        private final boolean warnRows;

        public ZebraRenderer(int align) {
            this(align, false);
        }

        public ZebraRenderer(int align, boolean warnRows) {
            this.align = align;
            this.warnRows = warnRows;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, false, r, c);
            setHorizontalAlignment(align);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setForeground(AppColor.INK);
            setBackground(sel ? AppColor.SELECT : (r % 2 == 0 ? Color.WHITE : AppColor.ZEBRA));
            return this;
        }
    }

    public static JTable table(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(28);
        t.setShowGrid(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(0xEDE4D6));
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFont(AppColor.FONT);
        t.setSelectionBackground(AppColor.SELECT);
        t.setSelectionForeground(AppColor.INK);
        t.setFillsViewportHeight(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setDefaultRenderer(Object.class, new ZebraRenderer(SwingConstants.LEFT));
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 30));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, false, false, r, c);
                l.setOpaque(true);
                l.setBackground(AppColor.BROWN);
                l.setForeground(Color.WHITE);
                l.setFont(AppColor.FONT_B.deriveFont(12f));
                l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                l.setHorizontalAlignment(tb.getColumnModel().getColumn(c).getCellRenderer() instanceof ZebraRenderer
                        ? ((ZebraRenderer) tb.getColumnModel().getColumn(c).getCellRenderer()).align : SwingConstants.LEFT);
                return l;
            }
        });
        return t;
    }

    public static void alignRight(JTable t, int... cols) {
        for (int c : cols) t.getColumnModel().getColumn(c).setCellRenderer(new ZebraRenderer(SwingConstants.RIGHT));
    }

    public static void alignCenter(JTable t, int... cols) {
        for (int c : cols) t.getColumnModel().getColumn(c).setCellRenderer(new ZebraRenderer(SwingConstants.CENTER));
    }

    public static void badge(JTable t, int... cols) {
        for (int c : cols) t.getColumnModel().getColumn(c).setCellRenderer(new BadgeRenderer());
    }

    public static void widths(JTable t, int... w) {
        for (int i = 0; i < w.length && i < t.getColumnCount(); i++) t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
    }

    /** Tô nền đỏ nhạt cho các dòng cảnh báo (ví dụ nguyên liệu sắp hết). Truyền hàm cho biết dòng nào cần cảnh báo. */
    public static void warnRows(JTable t, java.util.function.IntPredicate isWarn) {
        for (int c = 0; c < t.getColumnCount(); c++) {
            final int align = t.getColumnModel().getColumn(c).getCellRenderer() instanceof ZebraRenderer
                    ? ((ZebraRenderer) t.getColumnModel().getColumn(c).getCellRenderer()).align : SwingConstants.LEFT;
            t.getColumnModel().getColumn(c).setCellRenderer(new ZebraRenderer(align) {
                @Override
                public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foc, int r, int cc) {
                    Component comp = super.getTableCellRendererComponent(tb, v, sel, foc, r, cc);
                    if (!sel && isWarn.test(r)) comp.setBackground(AppColor.WARN_BG);
                    return comp;
                }
            });
        }
    }

    // ------------------------------------------------------------ hộp thoại
    public static Component activeWindow() {
        return java.awt.GraphicsEnvironment.isHeadless() ? null : KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    }

    public static void info(String msg) {
        show(msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void warn(String msg) {
        show(msg, "Chú ý", JOptionPane.WARNING_MESSAGE);
    }

    public static void error(String msg) {
        show(msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    /** Chế độ không hiện hộp thoại (dùng khi chạy kiểm thử tự động: -Dcafe.silent=true). */
    private static boolean silent() {
        return GraphicsEnvironment.isHeadless() || Boolean.getBoolean("cafe.silent");
    }

    private static void show(String msg, String title, int type) {
        if (silent()) {
            System.err.println("[" + title + "] " + msg);
            return;
        }
        JOptionPane.showMessageDialog(activeWindow(), msg, title, type);
    }

    public static boolean confirm(String msg) {
        if (silent()) return true;
        return JOptionPane.showConfirmDialog(activeWindow(), msg, "Xác nhận", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Chạy một việc, nếu có lỗi CSDL thì hiện thông báo thay vì làm treo chương trình. Trả true nếu chạy xong không lỗi. */
    public static boolean run(Runnable r) {
        try {
            r.run();
            return true;
        } catch (DaoException e) {
            error(e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            error("Có lỗi xảy ra: " + e);
        }
        return false;
    }

    public static ActionListener on(Runnable r) {
        return e -> run(r);
    }
}
