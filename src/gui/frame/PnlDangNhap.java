package gui.frame;

import dao.TaiKhoanDAO;
import entity.TaiKhoan;
import gui.component.BoGocButton;
import gui.component.CupIcon;
import gui.component.UI;
import gui.util.AppColor;
import gui.util.HashUtil;
import gui.util.SessionManager;
import gui.util.Validator;

import javax.swing.*;
import java.awt.*;

/** Nội dung màn hình đăng nhập. Sau khi đăng nhập, FrmMain hiển thị chức năng theo vai trò. */
public class PnlDangNhap extends JPanel {
    private final JTextField txtTenDN = UI.field(20);
    private final JPasswordField txtMatKhau = UI.password(20);
    private final JCheckBox chkHien = UI.check("Hiện mật khẩu");
    private final JLabel lblLoi = new JLabel(" ");
    private final BoGocButton btnDangNhap = UI.button("Đăng nhập", AppColor.BROWN);
    private final char echo;

    /** Panel bên trái: nền nâu, ly cà phê và tên quán. */
    private static class BenTrai extends JPanel {
        BenTrai() {
            setBackground(AppColor.DARK);
            setPreferredSize(new Dimension(290, 100));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int cx = getWidth() / 2;
            g2.setColor(AppColor.SIDEBAR);
            g2.fillOval(cx - 85, 70, 170, 170);
            new CupIcon(120, new Color(0xF1E6D6), true).paintIcon(this, g2, cx - 60, 95);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font(AppColor.FONT_NAME, Font.BOLD, 26));
            String t = "CAFÉ ABC";
            g2.drawString(t, cx - g2.getFontMetrics().stringWidth(t) / 2, 295);
            g2.setColor(new Color(0xD8C3A8));
            g2.setFont(new Font(AppColor.FONT_NAME, Font.PLAIN, 13));
            String a = "Hệ thống quản lý";
            String b = "quán cafe take away";
            g2.drawString(a, cx - g2.getFontMetrics().stringWidth(a) / 2, 325);
            g2.drawString(b, cx - g2.getFontMetrics().stringWidth(b) / 2, 345);
            g2.dispose();
        }
    }

    public PnlDangNhap() {
        super(new BorderLayout());
        echo = txtMatKhau.getEchoChar();

        JPanel phai = new JPanel(new GridBagLayout());
        phai.setBackground(AppColor.BG);
        phai.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(3, 0, 3, 0);

        JLabel tieuDe = new JLabel("ĐĂNG NHẬP");
        tieuDe.setFont(new Font(AppColor.FONT_NAME, Font.BOLD, 26));
        tieuDe.setForeground(AppColor.BROWN);
        g.gridy = 0;
        phai.add(tieuDe, g);
        g.gridy = 1;
        g.insets = new Insets(0, 0, 16, 0);
        phai.add(UI.muted("Vui lòng nhập tài khoản được quản lý cấp"), g);
        g.insets = new Insets(3, 0, 3, 0);

        g.gridy = 2;
        phai.add(UI.bold("Tên đăng nhập"), g);
        txtTenDN.setPreferredSize(new Dimension(100, 36));
        g.gridy = 3;
        phai.add(txtTenDN, g);
        g.gridy = 4;
        g.insets = new Insets(10, 0, 3, 0);
        phai.add(UI.bold("Mật khẩu"), g);
        g.insets = new Insets(3, 0, 3, 0);
        txtMatKhau.setPreferredSize(new Dimension(100, 36));
        g.gridy = 5;
        phai.add(txtMatKhau, g);
        g.gridy = 6;
        phai.add(chkHien, g);
        lblLoi.setForeground(AppColor.RED);
        lblLoi.setFont(AppColor.FONT_B);
        g.gridy = 7;
        phai.add(lblLoi, g);

        JPanel bt = new JPanel(new GridLayout(1, 2, 10, 0));
        bt.setOpaque(false);
        btnDangNhap.setFont(new Font(AppColor.FONT_NAME, Font.BOLD, 14));
        BoGocButton thoat = new BoGocButton("Thoát", AppColor.BROWN).outline(AppColor.BROWN);
        thoat.setFont(new Font(AppColor.FONT_NAME, Font.BOLD, 14));
        btnDangNhap.setPreferredSize(new Dimension(100, 40));
        bt.add(btnDangNhap);
        bt.add(thoat);
        g.gridy = 8;
        g.insets = new Insets(10, 0, 0, 0);
        phai.add(bt, g);

        add(new BenTrai(), BorderLayout.WEST);
        add(phai, BorderLayout.CENTER);

        chkHien.addActionListener(e -> txtMatKhau.setEchoChar(chkHien.isSelected() ? (char) 0 : echo));
        btnDangNhap.addActionListener(e -> dangNhap());
        thoat.addActionListener(e -> System.exit(0));
        setPreferredSize(new Dimension(790, 440));
    }

    /** Khi panel được gắn vào cửa sổ thì đặt nút Đăng nhập làm nút mặc định (nhấn Enter). */
    @Override
    public void addNotify() {
        super.addNotify();
        JRootPane rp = SwingUtilities.getRootPane(this);
        if (rp != null) rp.setDefaultButton(btnDangNhap);
    }

    private void loi(String s) {
        lblLoi.setText(s);
    }

    private void dangNhap() {
        loi(" ");
        String u = txtTenDN.getText().trim();
        String p = new String(txtMatKhau.getPassword());
        if (u.isEmpty() || p.isEmpty()) {
            loi("Vui lòng nhập tên đăng nhập và mật khẩu.");
            return;
        }
        if (!Validator.tenDangNhap(u)) {
            loi("Tên đăng nhập gồm 4-20 ký tự: chữ, số, dấu . hoặc _");
            return;
        }
        UI.run(() -> {
            TaiKhoan tk = new TaiKhoanDAO().dangNhap(u, HashUtil.sha256(p));
            if (tk == null) {
                loi("Sai tên đăng nhập hoặc mật khẩu!");
                txtMatKhau.setText("");
                txtMatKhau.requestFocus();
                return;
            }
            if (!tk.isTrangThai() || !tk.getNhanVien().isTrangThai()) {
                loi("Tài khoản đã bị khóa. Hãy liên hệ quản lý.");
                return;
            }
            SessionManager.login(tk);
            new FrmMain().setVisible(true);
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) w.dispose();
        });
    }
}
