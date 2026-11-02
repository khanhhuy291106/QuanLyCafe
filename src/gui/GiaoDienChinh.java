package gui;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.swing.*;
import connectDB.ConnectDB;
import entity.NhanVien;
import entity.TaiKhoan;

// Khung chính: menu bên trái (chỉ hiện chức năng đúng quyền), nội dung đổi bằng CardLayout.
// Quản lý: tất cả. Thu ngân: Trang chủ, Bán hàng, Quản lý đơn, xem Tồn kho. Pha chế: Trang chủ, Quản lý đơn, xem Tồn kho.
public class GiaoDienChinh extends JFrame {
    private static final String[][] MENU = { { "Trang chủ", "home" }, { "Bán hàng", "sell" }, { "Quản lý đơn", "orders" },
            { "Thực đơn", "menu" }, { "Nhân viên", "staff" }, { "Kho", "stock" }, { "Khuyến mãi", "promo" },
            { "Thống kê", "stats" } };

    private TaiKhoan tk;
    private NhanVien nv;
    private CardLayout card;
    private JPanel pnlNoiDung;
    private HashMap<String, JPanel> cacheManHinh = new HashMap<>();
    private LinkedHashMap<String, JButton> dsNut = new LinkedHashMap<>();
    private JLabel lblTieuDe, lblNguoiDung;

    public GiaoDienChinh(TaiKhoan tk) {
        this.tk = tk;
        this.nv = tk.getNhanVien();
        setTitle("Quản Lý Quán Cafe — " + nv.getHoTen() + " (" + NhanVien.tenChucVu(nv.getChucVu()) + ")");
        setIconImage(TienIch.bieuTuong());
        setSize(1320, 780);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        add(taoSidebar(), BorderLayout.WEST);

        JPanel pnlPhai = new JPanel(new BorderLayout());
        pnlPhai.add(taoThanhTren(), BorderLayout.NORTH);
        card = new CardLayout();
        pnlNoiDung = new JPanel(card);
        pnlNoiDung.setBackground(TienIch.BG);
        pnlPhai.add(pnlNoiDung, BorderLayout.CENTER);
        add(pnlPhai, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(GiaoDienChinh.this, "Thoát chương trình?", "Xác nhận",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    ConnectDB.getInstance().disconnect();
                    System.exit(0);
                }
            }
        });
        hienThi("home");
    }

    // Quyền truy cập từng chức năng theo vai trò
    private boolean duocPhep(String khoa) {
        if (khoa.equals("sell")) return !nv.laPhaChe();
        if (khoa.equals("menu") || khoa.equals("staff") || khoa.equals("promo") || khoa.equals("stats")) return nv.laQuanLy();
        return true; // home, orders, stock (kho: nhân viên chỉ xem, đã xử lý trong TabKho)
    }

    private JPanel taoSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(TienIch.SIDEBAR);
        sb.setPreferredSize(new Dimension(200, 100));

        // Logo
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        logo.setOpaque(false);
        logo.setMaximumSize(new Dimension(200, 70));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(14, 8, 0, 0));
        logo.add(new JLabel(TienIch.bieuTuongLy(40, new Color(0xF1E6D6))));
        JPanel pnlTen = new JPanel(new GridLayout(2, 1));
        pnlTen.setOpaque(false);
        JLabel l1 = new JLabel("CAFÉ ABC");
        l1.setFont(new Font(TienIch.FONT_NAME, Font.BOLD, 16));
        l1.setForeground(Color.WHITE);
        JLabel l2 = new JLabel("Take away");
        l2.setFont(TienIch.FONT_SMALL);
        l2.setForeground(new Color(0xD8C3A8));
        pnlTen.add(l1);
        pnlTen.add(l2);
        logo.add(pnlTen);
        sb.add(logo);
        sb.add(Box.createVerticalStrut(24));

        // Các nút menu (ẩn chức năng không đủ quyền)
        for (String[] m : MENU) {
            if (!duocPhep(m[1])) continue;
            JButton b = new JButton(m[0]);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setFont(new Font(TienIch.FONT_NAME, Font.PLAIN, 14));
            b.setForeground(new Color(0xEADBC6));
            b.setBackground(TienIch.SIDEBAR);
            b.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 10));
            b.setFocusPainted(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.setMaximumSize(new Dimension(200, 42));
            b.setPreferredSize(new Dimension(200, 42));
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (!daiDienNhanVien(m[1])) {
                // Chức năng chỉ dành cho Quản lý: gắn nhãn QL
                b.setLayout(new BorderLayout());
                JLabel tag = new JLabel("QL  ");
                tag.setFont(TienIch.FONT_SMALL);
                tag.setForeground(new Color(0xA98F72));
                b.add(tag, BorderLayout.EAST);
            }
            b.addActionListener(e -> hienThi(m[1]));
            dsNut.put(m[1], b);
            sb.add(b);
            sb.add(Box.createVerticalStrut(2));
        }
        sb.add(Box.createVerticalGlue());

        // Phía dưới: người dùng, đổi mật khẩu, đăng xuất
        JPanel duoi = new JPanel(new GridLayout(0, 1, 0, 2));
        duoi.setOpaque(false);
        duoi.setBorder(BorderFactory.createEmptyBorder(8, 12, 14, 12));
        duoi.setMaximumSize(new Dimension(200, 140));
        duoi.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblTen = new JLabel(nv.getHoTen());
        lblTen.setFont(TienIch.FONT_B);
        lblTen.setForeground(Color.WHITE);
        JLabel lblVaiTro = new JLabel(NhanVien.tenChucVu(nv.getChucVu()));
        lblVaiTro.setFont(TienIch.FONT_SMALL);
        lblVaiTro.setForeground(new Color(0xFFE2B5));
        duoi.add(lblTen);
        duoi.add(lblVaiTro);
        duoi.add(taoLink("Đổi mật khẩu"));
        duoi.add(taoLink("Đăng xuất"));
        sb.add(duoi);
        return sb;
    }

    // Chức năng mà Thu ngân hoặc Pha chế cũng được dùng (để biết chức năng nào chỉ dành cho Quản lý)
    private boolean daiDienNhanVien(String khoa) {
        return !(khoa.equals("menu") || khoa.equals("staff") || khoa.equals("promo") || khoa.equals("stats"));
    }

    private JLabel taoLink(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(0xEADBC6));
        l.setCursor(new Cursor(Cursor.HAND_CURSOR));
        l.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (text.equals("Đổi mật khẩu")) new DoiMatKhau(GiaoDienChinh.this, tk).setVisible(true);
                else dangXuat();
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

    private JPanel taoThanhTren() {
        JPanel tb = new JPanel(new BorderLayout());
        tb.setBackground(TienIch.CREAM);
        tb.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TienIch.LINE),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));
        lblTieuDe = new JLabel();
        lblTieuDe.setFont(TienIch.FONT_TITLE);
        lblTieuDe.setForeground(TienIch.BROWN);
        tb.add(lblTieuDe, BorderLayout.WEST);
        lblNguoiDung = new JLabel();
        lblNguoiDung.setForeground(TienIch.GRAY);
        tb.add(lblNguoiDung, BorderLayout.EAST);
        capNhatDongHo();
        new Timer(30000, e -> capNhatDongHo()).start();
        return tb;
    }

    private void capNhatDongHo() {
        lblNguoiDung.setText(nv.getHoTen() + " (" + NhanVien.tenChucVu(nv.getChucVu()) + ")   |   " + TienIch.ngayGio(LocalDateTime.now()));
    }

    private JPanel taoManHinh(String khoa) {
        switch (khoa) {
            case "home":
                return new TabTrangChu(nv, this);
            case "sell":
                return new TabBanHang(nv);
            case "orders":
                return new TabQuanLyDon(nv);
            case "menu":
                return new TabThucDon();
            case "staff":
                return new TabNhanVien(nv);
            case "stock":
                return new TabKho(nv);
            case "promo":
                return new TabKhuyenMai();
            default:
                return new TabThongKe();
        }
    }

    // Mở một màn hình theo khóa (home, sell, orders, menu, staff, stock, promo, stats).
    // "stock:nhap" mở thẳng thẻ Phiếu nhập.
    public void hienThi(String khoaDayDu) {
        String khoa = khoaDayDu.contains(":") ? khoaDayDu.substring(0, khoaDayDu.indexOf(':')) : khoaDayDu;
        if (!duocPhep(khoa)) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền truy cập chức năng này.");
            return;
        }
        JPanel p = cacheManHinh.get(khoa);
        if (p == null) {
            p = taoManHinh(khoa);
            cacheManHinh.put(khoa, p);
            pnlNoiDung.add(p, khoa);
        }
        card.show(pnlNoiDung, khoa);

        for (String k : dsNut.keySet()) {
            JButton b = dsNut.get(k);
            boolean chon = k.equals(khoa);
            b.setBackground(chon ? TienIch.ACCENT : TienIch.SIDEBAR);
            b.setForeground(chon ? Color.WHITE : new Color(0xEADBC6));
            b.setFont(new Font(TienIch.FONT_NAME, chon ? Font.BOLD : Font.PLAIN, 14));
        }
        lblTieuDe.setText(dsNut.containsKey(khoa) ? dsNut.get(khoa).getText() : "");
        if (khoaDayDu.equals("stock:nhap") && p instanceof TabKho) ((TabKho) p).chonThe("Phiếu nhập");
        if (p instanceof TaiLai) ((TaiLai) p).taiLai();
    }

    private void dangXuat() {
        if (JOptionPane.showConfirmDialog(this, "Đăng xuất khỏi tài khoản " + nv.getHoTen() + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        dispose();
        new DangNhap().setVisible(true);
    }
}
