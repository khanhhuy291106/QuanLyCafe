package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import dao.TaiKhoan_DAO;
import entity.TaiKhoan;

public class DangNhap extends JFrame implements ActionListener {
    private JTextField txtTenDN;
    private JPasswordField txtMatKhau;
    private JCheckBox chkHien;
    private JLabel lblLoi;
    private JButton btnDangNhap, btnThoat;

    public DangNhap() {
        setTitle("Đăng nhập - Quản lý quán cafe");
        setIconImage(TienIch.bieuTuong());
        setSize(790, 440);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Bên trái: nền nâu, ly cà phê và tên quán
        JPanel pnlTrai = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(TienIch.SIDEBAR);
                g.fillOval(60, 70, 170, 170);
                TienIch.veLyCafe(g, 85, 95, 120, new Color(0xF1E6D6));
            }
        };
        pnlTrai.setBackground(TienIch.DARK);
        pnlTrai.setPreferredSize(new Dimension(290, 100));
        JLabel lblQuan = new JLabel("CAFÉ ABC", SwingConstants.CENTER);
        lblQuan.setFont(new Font(TienIch.FONT_NAME, Font.BOLD, 26));
        lblQuan.setForeground(Color.WHITE);
        lblQuan.setBounds(0, 270, 290, 36);
        pnlTrai.add(lblQuan);
        JLabel lblMoTa = new JLabel("<html><center>Hệ thống quản lý<br>quán cafe take away</center></html>", SwingConstants.CENTER);
        lblMoTa.setForeground(new Color(0xD8C3A8));
        lblMoTa.setBounds(0, 310, 290, 40);
        pnlTrai.add(lblMoTa);
        add(pnlTrai, BorderLayout.WEST);

        // Bên phải: form đăng nhập
        JPanel pnlPhai = new JPanel(new GridBagLayout());
        pnlPhai.setBackground(TienIch.BG);
        pnlPhai.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 0, 3, 0);

        JLabel lblTieuDe = new JLabel("ĐĂNG NHẬP");
        lblTieuDe.setFont(new Font(TienIch.FONT_NAME, Font.BOLD, 26));
        lblTieuDe.setForeground(TienIch.BROWN);
        gbc.gridy = 0;
        pnlPhai.add(lblTieuDe, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 16, 0);
        pnlPhai.add(TienIch.ghiChu("Vui lòng nhập tài khoản được quản lý cấp"), gbc);
        gbc.insets = new Insets(3, 0, 3, 0);

        JLabel lblUser = new JLabel("Tên đăng nhập");
        lblUser.setFont(TienIch.FONT_B);
        gbc.gridy = 2;
        pnlPhai.add(lblUser, gbc);
        txtTenDN = new JTextField();
        txtTenDN.setPreferredSize(new Dimension(100, 36));
        gbc.gridy = 3;
        pnlPhai.add(txtTenDN, gbc);

        JLabel lblPass = new JLabel("Mật khẩu");
        lblPass.setFont(TienIch.FONT_B);
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 0, 3, 0);
        pnlPhai.add(lblPass, gbc);
        gbc.insets = new Insets(3, 0, 3, 0);
        txtMatKhau = new JPasswordField();
        txtMatKhau.setPreferredSize(new Dimension(100, 36));
        gbc.gridy = 5;
        pnlPhai.add(txtMatKhau, gbc);

        chkHien = new JCheckBox("Hiện mật khẩu");
        gbc.gridy = 6;
        pnlPhai.add(chkHien, gbc);

        lblLoi = new JLabel(" ");
        lblLoi.setForeground(TienIch.RED);
        lblLoi.setFont(TienIch.FONT_B);
        gbc.gridy = 7;
        pnlPhai.add(lblLoi, gbc);

        JPanel pnlNut = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlNut.setOpaque(false);
        btnDangNhap = TienIch.nut("Đăng nhập", TienIch.BROWN);
        btnThoat = TienIch.nut("Thoát", TienIch.GRAY);
        btnDangNhap.setPreferredSize(new Dimension(100, 40));
        pnlNut.add(btnDangNhap);
        pnlNut.add(btnThoat);
        gbc.gridy = 8;
        gbc.insets = new Insets(10, 0, 0, 0);
        pnlPhai.add(pnlNut, gbc);
        add(pnlPhai, BorderLayout.CENTER);

        getRootPane().setDefaultButton(btnDangNhap);
        btnDangNhap.addActionListener(this);
        btnThoat.addActionListener(this);
        chkHien.addActionListener(e -> {
            if (chkHien.isSelected()) txtMatKhau.setEchoChar((char) 0);
            else txtMatKhau.setEchoChar('•');
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnThoat) {
            System.exit(0);
            return;
        }
        lblLoi.setText(" ");
        String user = txtTenDN.getText().trim();
        String pass = new String(txtMatKhau.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            lblLoi.setText("Vui lòng nhập tên đăng nhập và mật khẩu.");
            return;
        }
        if (!user.matches("^[a-zA-Z0-9._]{4,20}$")) {
            lblLoi.setText("Tên đăng nhập gồm 4-20 ký tự: chữ, số, dấu . hoặc _");
            return;
        }
        TaiKhoan tk = new TaiKhoan_DAO().dangNhap(user, pass);
        if (tk == null) {
            lblLoi.setText("Sai tên đăng nhập hoặc mật khẩu!");
            txtMatKhau.setText("");
            txtMatKhau.requestFocus();
            return;
        }
        if (!tk.isTrangThai() || !tk.getNhanVien().isTrangThai()) {
            lblLoi.setText("Tài khoản đã bị khóa. Hãy liên hệ quản lý.");
            return;
        }
        dispose();
        new GiaoDienChinh(tk).setVisible(true);
    }
}
