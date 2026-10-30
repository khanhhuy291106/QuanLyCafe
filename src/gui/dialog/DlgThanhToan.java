package gui.dialog;

import entity.HoaDon;
import gui.component.BoGocButton;
import gui.component.UI;
import gui.util.AppColor;
import gui.util.FormatUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/** Hộp thoại thanh toán: chọn phương thức, nhập tiền khách đưa, tính tiền thừa. */
public class DlgThanhToan extends JDialog {
    private final double tong;
    private final JRadioButton rdTienMat = UI.radio("Tiền mặt");
    private final JRadioButton rdChuyenKhoan = UI.radio("Chuyển khoản / QR");
    private final JTextField txtKhachDua = UI.field(12);
    private final JTextField txtTienThua = UI.readOnly(12);
    private final JCheckBox chkIn = UI.check("In hóa đơn sau khi thanh toán");
    private final BoGocButton btnOk = UI.button("Xác nhận thanh toán", AppColor.GREEN);
    private boolean daThanhToan;

    public DlgThanhToan(Window owner, HoaDon hd, int soThuTu) {
        super(owner, "Thanh toán", ModalityType.APPLICATION_MODAL);
        setIconImage(UI.appIcon());
        this.tong = hd.tinhTongTien();
        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        root.setBackground(AppColor.BG);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 0);

        JLabel lblDon = new JLabel("Đơn #" + String.format("%03d", soThuTu));
        lblDon.setFont(AppColor.FONT_TITLE);
        lblDon.setForeground(AppColor.BROWN);
        g.gridy = 0;
        root.add(lblDon, g);

        g.gridy = 1;
        root.add(dong("Tạm tính", FormatUtil.tienD(hd.tinhTamTinh()), AppColor.INK), g);
        g.gridy = 2;
        String tenKM = hd.getKhuyenMai() == null ? "Khuyến mãi" : "Khuyến mãi " + hd.getKhuyenMai().getMaKM();
        root.add(dong(tenKM, "-" + FormatUtil.tienD(hd.getGiamGia()), AppColor.RED), g);

        JPanel tongBox = new JPanel(new BorderLayout());
        tongBox.setBackground(new Color(0xEAF4EE));
        tongBox.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(AppColor.GREEN),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        tongBox.add(UI.bold("Tổng thanh toán"), BorderLayout.WEST);
        JLabel lt = new JLabel(FormatUtil.tienD(tong));
        lt.setFont(new Font(AppColor.FONT_NAME, Font.BOLD, 22));
        lt.setForeground(AppColor.GREEN);
        tongBox.add(lt, BorderLayout.EAST);
        g.gridy = 3;
        g.insets = new Insets(10, 0, 10, 0);
        root.add(tongBox, g);
        g.insets = new Insets(4, 0, 4, 0);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rdTienMat);
        bg.add(rdChuyenKhoan);
        rdTienMat.setSelected(true);
        JPanel pt = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        pt.setOpaque(false);
        pt.add(rdTienMat);
        pt.add(rdChuyenKhoan);
        g.gridy = 4;
        root.add(UI.bold("Phương thức thanh toán"), g);
        g.gridy = 5;
        root.add(pt, g);

        g.gridy = 6;
        root.add(dongNhap("Khách đưa (đ)", txtKhachDua), g);
        g.gridy = 7;
        root.add(dongNhap("Tiền thừa", txtTienThua), g);
        txtTienThua.setForeground(AppColor.GREEN);
        txtTienThua.setFont(AppColor.FONT_B);

        chkIn.setSelected(true);
        g.gridy = 8;
        root.add(chkIn, g);

        JPanel bt = new JPanel(new GridLayout(1, 2, 10, 0));
        bt.setOpaque(false);
        BoGocButton huy = new BoGocButton("Hủy", AppColor.BROWN).outline(AppColor.BROWN);
        btnOk.setPreferredSize(new Dimension(200, 40));
        bt.add(btnOk);
        bt.add(huy);
        g.gridy = 9;
        g.insets = new Insets(14, 0, 0, 0);
        root.add(bt, g);

        setContentPane(root);
        txtKhachDua.setText(FormatUtil.tien(tong));
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
        huy.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnOk);
        setSize(460, 560);
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private JPanel dong(String ten, String giaTri, Color mau) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(UI.label(ten), BorderLayout.WEST);
        JLabel v = UI.label(giaTri);
        v.setForeground(mau);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    private JPanel dongNhap(String ten, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        JLabel l = UI.label(ten);
        l.setPreferredSize(new Dimension(110, 30));
        p.add(l, BorderLayout.WEST);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private void capNhat() {
        boolean tienMat = rdTienMat.isSelected();
        txtKhachDua.setEnabled(tienMat);
        if (!tienMat) {
            txtTienThua.setText("0đ");
            btnOk.setEnabled(true);
            return;
        }
        double kd = FormatUtil.parseTien(txtKhachDua.getText());
        if (kd < tong) {
            txtTienThua.setText(kd < 0 ? "Số tiền không hợp lệ" : "Chưa đủ tiền");
            btnOk.setEnabled(false);
        } else {
            txtTienThua.setText(FormatUtil.tienD(kd - tong));
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

    /** Chuyển khoản thì không có khách đưa/tiền thừa: trả -1. */
    public double getKhachDua() {
        return rdTienMat.isSelected() ? FormatUtil.parseTien(txtKhachDua.getText()) : -1;
    }

    public double getTienThua() {
        return rdTienMat.isSelected() ? Math.max(0, getKhachDua() - tong) : -1;
    }
}
