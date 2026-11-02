package gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import entity.HoaDon;

// Hộp thoại thanh toán: chọn phương thức, nhập tiền khách đưa, tính tiền thừa
public class PhieuThanhToan extends JDialog {
    private double tong;
    private JRadioButton rdTienMat, rdChuyenKhoan;
    private JTextField txtKhachDua, txtTienThua;
    private JCheckBox chkIn;
    private JButton btnOk, btnHuy;
    private boolean daThanhToan = false;

    public PhieuThanhToan(Window owner, HoaDon hd, int soThuTu) {
        super(owner, "Thanh toán", ModalityType.APPLICATION_MODAL);
        this.tong = hd.tinhTongTien();
        setSize(460, 560);
        setResizable(false);
        setLocationRelativeTo(owner);
        setLayout(new GridBagLayout());
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 0);

        JLabel lblDon = new JLabel("Đơn #" + String.format("%03d", soThuTu));
        lblDon.setFont(TienIch.FONT_TITLE);
        lblDon.setForeground(TienIch.BROWN);
        g.gridy = 0;
        add(lblDon, g);

        g.gridy = 1;
        add(taoDong("Tạm tính", TienIch.tienD(hd.tinhTamTinh()), TienIch.INK), g);
        String tenKM = hd.getKhuyenMai() == null ? "Khuyến mãi" : "Khuyến mãi " + hd.getKhuyenMai().getMaKM();
        g.gridy = 2;
        add(taoDong(tenKM, "-" + TienIch.tienD(hd.getGiamGia()), TienIch.RED), g);

        JPanel pnlTong = new JPanel(new BorderLayout());
        pnlTong.setBackground(new Color(0xEAF4EE));
        pnlTong.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(TienIch.GREEN),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        JLabel lblNhan = new JLabel("Tổng thanh toán");
        lblNhan.setFont(TienIch.FONT_B);
        pnlTong.add(lblNhan, BorderLayout.WEST);
        JLabel lblTong = new JLabel(TienIch.tienD(tong));
        lblTong.setFont(new Font(TienIch.FONT_NAME, Font.BOLD, 22));
        lblTong.setForeground(TienIch.GREEN);
        pnlTong.add(lblTong, BorderLayout.EAST);
        g.gridy = 3;
        g.insets = new Insets(10, 0, 10, 0);
        add(pnlTong, g);
        g.insets = new Insets(4, 0, 4, 0);

        JLabel lblPT = new JLabel("Phương thức thanh toán");
        lblPT.setFont(TienIch.FONT_B);
        g.gridy = 4;
        add(lblPT, g);
        rdTienMat = new JRadioButton("Tiền mặt", true);
        rdChuyenKhoan = new JRadioButton("Chuyển khoản / QR");
        ButtonGroup nhom = new ButtonGroup();
        nhom.add(rdTienMat);
        nhom.add(rdChuyenKhoan);
        JPanel pnlPT = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        pnlPT.setOpaque(false);
        pnlPT.add(rdTienMat);
        pnlPT.add(rdChuyenKhoan);
        g.gridy = 5;
        add(pnlPT, g);

        txtKhachDua = new JTextField(12);
        txtTienThua = new JTextField(12);
        txtTienThua.setEditable(false);
        txtTienThua.setForeground(TienIch.GREEN);
        txtTienThua.setFont(TienIch.FONT_B);
        g.gridy = 6;
        add(taoDongNhap("Khách đưa (đ)", txtKhachDua), g);
        g.gridy = 7;
        add(taoDongNhap("Tiền thừa", txtTienThua), g);

        chkIn = new JCheckBox("In hóa đơn sau khi thanh toán", true);
        g.gridy = 8;
        add(chkIn, g);

        JPanel pnlNut = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlNut.setOpaque(false);
        btnOk = TienIch.nut("Xác nhận thanh toán", TienIch.GREEN);
        btnOk.setPreferredSize(new Dimension(200, 40));
        btnHuy = TienIch.nutPhu("Hủy");
        pnlNut.add(btnOk);
        pnlNut.add(btnHuy);
        g.gridy = 9;
        g.insets = new Insets(14, 0, 0, 0);
        add(pnlNut, g);

        txtKhachDua.setText(TienIch.tien(tong));
        capNhat();
        txtKhachDua.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                capNhat();
            }

            public void removeUpdate(DocumentEvent e) {
                capNhat();
            }

            public void changedUpdate(DocumentEvent e) {
                capNhat();
            }
        });
        rdTienMat.addActionListener(e -> capNhat());
        rdChuyenKhoan.addActionListener(e -> capNhat());
        btnOk.addActionListener(e -> {
            daThanhToan = true;
            dispose();
        });
        btnHuy.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnOk);
    }

    private JPanel taoDong(String ten, String giaTri, Color mau) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(new JLabel(ten), BorderLayout.WEST);
        JLabel v = new JLabel(giaTri);
        v.setForeground(mau);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    private JPanel taoDongNhap(String ten, JComponent o) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(ten);
        l.setPreferredSize(new Dimension(110, 30));
        p.add(l, BorderLayout.WEST);
        p.add(o, BorderLayout.CENTER);
        return p;
    }

    // Tính tiền thừa; chưa đủ tiền thì khóa nút xác nhận
    private void capNhat() {
        boolean tienMat = rdTienMat.isSelected();
        txtKhachDua.setEnabled(tienMat);
        if (!tienMat) {
            txtTienThua.setText("0đ");
            btnOk.setEnabled(true);
            return;
        }
        double kd = TienIch.parseTien(txtKhachDua.getText());
        if (kd < tong) {
            txtTienThua.setText(kd < 0 ? "Số tiền không hợp lệ" : "Chưa đủ tiền");
            btnOk.setEnabled(false);
        } else {
            txtTienThua.setText(TienIch.tienD(kd - tong));
            btnOk.setEnabled(true);
        }
    }

    public boolean isDaThanhToan() {
        return daThanhToan;
    }

    public String getPhuongThuc() {
        return rdChuyenKhoan.isSelected() ? HoaDon.CHUYEN_KHOAN : HoaDon.TIEN_MAT;
    }

    public boolean isInHoaDon() {
        return chkIn.isSelected();
    }

    // Chuyển khoản thì không có khách đưa/tiền thừa: trả -1
    public double getKhachDua() {
        return rdTienMat.isSelected() ? TienIch.parseTien(txtKhachDua.getText()) : -1;
    }

    public double getTienThua() {
        return rdTienMat.isSelected() ? Math.max(0, getKhachDua() - tong) : -1;
    }
}
