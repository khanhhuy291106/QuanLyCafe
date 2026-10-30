package gui.frame;

import entity.NhanVien;
import gui.component.CupIcon;
import gui.component.Refreshable;
import gui.component.UI;
import gui.dialog.DlgDoiMatKhau;
import gui.panel.*;
import gui.util.AppColor;
import gui.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Nội dung khung chính của ứng dụng: menu bên trái, nội dung đổi bằng CardLayout.
 * PHÂN QUYỀN: menu chỉ hiện các chức năng mà vai trò đang đăng nhập được dùng.
 * <ul>
 *   <li>Quản lý: tất cả (thực đơn, nhân viên, khuyến mãi, kho đầy đủ, thống kê...)</li>
 *   <li>Thu ngân: Trang chủ, Bán hàng, Quản lý đơn, xem Tồn kho</li>
 *   <li>Pha chế: Trang chủ, Quản lý đơn, xem Tồn kho</li>
 * </ul>
 * Ngoài việc ẩn menu, mỗi màn hình quản lý còn tự kiểm tra quyền khi được tạo.
 */
public class MainShell extends JPanel {
    private static final String[][] MENU = {
            {"Trang chủ", "home"}, {"Bán hàng", "sell"}, {"Quản lý đơn", "orders"}, {"Thực đơn", "menu"},
            {"Nhân viên", "staff"}, {"Kho", "stock"}, {"Khuyến mãi", "promo"}, {"Thống kê", "stats"}};

    private final String vaiTro = SessionManager.getVaiTro();
    private final CardLayout card = new CardLayout();
    private final JPanel noiDung = new JPanel(card);
    private final Map<String, JPanel> cacheManHinh = new HashMap<>();
    private final Map<String, MenuBtn> nut = new LinkedHashMap<>();
    private final JLabel lblTieuDe = new JLabel();
    private final JLabel lblNguoiDung = new JLabel();
    private String dangMo;

    /** Quyền truy cập từng chức năng theo vai trò. */
    public static boolean duocPhep(String khoa, String vaiTro) {
        switch (khoa) {
            case "sell":
                return !NhanVien.PHA_CHE.equals(vaiTro);
            case "menu":
            case "staff":
            case "promo":
            case "stats":
                return NhanVien.QUAN_LY.equals(vaiTro);
            default:
                return true; // home, orders, stock (stock: nhân viên chỉ xem, đã xử lý trong PnlKho)
        }
    }

    private static boolean chiQuanLy(String khoa) {
        return !duocPhep(khoa, NhanVien.THU_NGAN) && !duocPhep(khoa, NhanVien.PHA_CHE);
    }

    /** Nút menu tự vẽ. */
    private static class MenuBtn extends JButton {
        private boolean active;
        private final boolean tagQL;

        MenuBtn(String ten, boolean tagQL) {
            super(ten);
            this.tagQL = tagQL;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setHorizontalAlignment(LEFT);
            setPreferredSize(new Dimension(200, 42));
            setMaximumSize(new Dimension(200, 42));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setFont(new Font(AppColor.FONT_NAME, Font.PLAIN, 14));
        }

        void setActive(boolean a) {
            active = a;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (active) {
                g2.setColor(AppColor.ACCENT);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0xFFE2B5));
                g2.fillRect(0, 0, 4, getHeight());
            } else if (getModel().isRollover()) {
                g2.setColor(new Color(0x5A4333));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.setFont(active ? getFont().deriveFont(Font.BOLD) : getFont());
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(active ? Color.WHITE : new Color(0xEADBC6));
            g2.drawString(getText(), 20, y);
            if (tagQL) {
                g2.setFont(getFont().deriveFont(Font.BOLD, 10f));
                g2.setColor(active ? new Color(0xFFE2B5) : new Color(0xA98F72));
                g2.drawString("QL", getWidth() - 28, y);
            }
            g2.dispose();
        }
    }

    public MainShell() {
        super(new BorderLayout());
        add(dungSidebar(), BorderLayout.WEST);
        JPanel phai = new JPanel(new BorderLayout());
        phai.add(dungTopBar(), BorderLayout.NORTH);
        noiDung.setBackground(AppColor.BG);
        phai.add(noiDung, BorderLayout.CENTER);
        add(phai, BorderLayout.CENTER);
        hienThi("home");
    }

    private JComponent dungSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(AppColor.SIDEBAR);
        sb.setPreferredSize(new Dimension(200, 100));

        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        logo.setOpaque(false);
        logo.setMaximumSize(new Dimension(200, 70));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(14, 8, 0, 0));
        logo.add(new JLabel(new CupIcon(40, new Color(0xF1E6D6), true)));
        JPanel ten = new JPanel(new GridLayout(2, 1));
        ten.setOpaque(false);
        JLabel l1 = new JLabel("CAFÉ ABC");
        l1.setFont(new Font(AppColor.FONT_NAME, Font.BOLD, 16));
        l1.setForeground(Color.WHITE);
        JLabel l2 = new JLabel("Take away");
        l2.setFont(AppColor.FONT_SMALL);
        l2.setForeground(new Color(0xD8C3A8));
        ten.add(l1);
        ten.add(l2);
        logo.add(ten);
        sb.add(logo);
        sb.add(Box.createVerticalStrut(24));

        for (String[] m : MENU) {
            if (!duocPhep(m[1], vaiTro)) continue; // ẩn chức năng không đủ quyền
            MenuBtn b = new MenuBtn(m[0], chiQuanLy(m[1]));
            b.addActionListener(e -> hienThi(m[1]));
            nut.put(m[1], b);
            sb.add(b);
            sb.add(Box.createVerticalStrut(2));
        }
        sb.add(Box.createVerticalGlue());

        JPanel duoi = new JPanel(new GridLayout(0, 1, 0, 2));
        duoi.setOpaque(false);
        duoi.setBorder(BorderFactory.createEmptyBorder(8, 12, 14, 12));
        duoi.setMaximumSize(new Dimension(200, 140));
        duoi.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel nd = new JLabel(SessionManager.getNhanVien().getHoTen());
        nd.setFont(AppColor.FONT_B);
        nd.setForeground(Color.WHITE);
        JLabel vt = new JLabel(NhanVien.tenChucVu(vaiTro));
        vt.setFont(AppColor.FONT_SMALL);
        vt.setForeground(new Color(0xFFE2B5));
        duoi.add(nd);
        duoi.add(vt);
        duoi.add(link("Đổi mật khẩu", () -> new DlgDoiMatKhau(SwingUtilities.getWindowAncestor(this)).setVisible(true)));
        duoi.add(link("Đăng xuất", this::dangXuat));
        sb.add(duoi);
        return sb;
    }

    private JLabel link(String text, Runnable r) {
        JLabel l = new JLabel(text);
        l.setFont(AppColor.FONT);
        l.setForeground(new Color(0xEADBC6));
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        l.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                r.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                l.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                l.setForeground(new Color(0xEADBC6));
            }
        });
        return l;
    }

    private JComponent dungTopBar() {
        JPanel tb = new JPanel(new BorderLayout());
        tb.setBackground(AppColor.CREAM);
        tb.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppColor.LINE),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));
        lblTieuDe.setFont(new Font(AppColor.FONT_NAME, Font.BOLD, 18));
        lblTieuDe.setForeground(AppColor.BROWN);
        tb.add(lblTieuDe, BorderLayout.WEST);
        lblNguoiDung.setFont(AppColor.FONT);
        lblNguoiDung.setForeground(AppColor.GRAY);
        tb.add(lblNguoiDung, BorderLayout.EAST);
        capNhatDongHo();
        new Timer(30_000, e -> capNhatDongHo()).start();
        return tb;
    }

    private void capNhatDongHo() {
        lblNguoiDung.setText(SessionManager.moTa() + "   |   " + gui.util.FormatUtil.ngayGio(LocalDateTime.now()));
    }

    private JPanel taoManHinh(String khoa) {
        switch (khoa) {
            case "home":
                return new PnlTrangChu(this::hienThi);
            case "sell":
                return new PnlBanHang();
            case "orders":
                return new PnlQuanLyDon();
            case "menu":
                return new PnlThucDon();
            case "staff":
                return new PnlNhanVien();
            case "stock":
                return new PnlKho();
            case "promo":
                return new PnlKhuyenMai();
            case "stats":
                return new PnlThongKe();
            default:
                throw new IllegalArgumentException(khoa);
        }
    }

    /** Mở một màn hình theo khóa; kiểm tra quyền trước khi mở. "stock:nhap" mở thẳng thẻ Phiếu nhập. */
    public void hienThi(String khoaDayDu) {
        String khoa = khoaDayDu.contains(":") ? khoaDayDu.substring(0, khoaDayDu.indexOf(':')) : khoaDayDu;
        if (!duocPhep(khoa, vaiTro)) {
            UI.warn("Bạn không có quyền truy cập chức năng này.");
            return;
        }
        JPanel p = cacheManHinh.get(khoa);
        if (p == null) {
            try {
                p = taoManHinh(khoa);
            } catch (SecurityException e) {
                UI.warn(e.getMessage());
                return;
            }
            cacheManHinh.put(khoa, p);
            noiDung.add(p, khoa);
        }
        card.show(noiDung, khoa);
        dangMo = khoa;
        for (Map.Entry<String, MenuBtn> en : nut.entrySet()) en.getValue().setActive(en.getKey().equals(khoa));
        lblTieuDe.setText(nut.containsKey(khoa) ? nut.get(khoa).getText() : "");
        if (khoaDayDu.equals("stock:nhap") && p instanceof PnlKho) ((PnlKho) p).chonThe("Phiếu nhập");
        if (p instanceof Refreshable) ((Refreshable) p).refresh();
    }

    private void dangXuat() {
        if (!UI.confirm("Đăng xuất khỏi tài khoản " + SessionManager.getNhanVien().getHoTen() + "?")) return;
        SessionManager.logout();
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null) w.dispose();
        new FrmDangNhap().setVisible(true);
    }
}
