package gui;

import java.awt.*;
import javax.swing.*;
import dao.TaiKhoan_DAO;
import entity.TaiKhoan;

// Hộp thoại đổi mật khẩu cho tài khoản đang đăng nhập
public class DoiMatKhau extends JDialog {
    private JPasswordField txtCu, txtMoi, txtLai;
    private TaiKhoan tk;

    public DoiMatKhau(Window owner, TaiKhoan tk) {
        super(owner, "Đổi mật khẩu", ModalityType.APPLICATION_MODAL);
        this.tk = tk;
        setSize(440, 280);
        setResizable(false);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(0, 12));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        add(TienIch.tieuDe("Đổi mật khẩu"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        txtCu = new JPasswordField(16);
        txtMoi = new JPasswordField(16);
        txtLai = new JPasswordField(16);
        TienIch.themDong(form, 0, "Mật khẩu hiện tại", txtCu);
        TienIch.themDong(form, 1, "Mật khẩu mới", txtMoi);
        TienIch.themDong(form, 2, "Nhập lại mật khẩu", txtLai);
        add(form, BorderLayout.CENTER);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnHuy = TienIch.nutPhu("Hủy");
        JButton btnLuu = TienIch.nut("Lưu", TienIch.BROWN);
        pnlNut.add(btnHuy);
        pnlNut.add(btnLuu);
        add(pnlNut, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnLuu);
        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> luu());
    }

    private void luu() {
        String cu = new String(txtCu.getPassword());
        String moi = new String(txtMoi.getPassword());
        String lai = new String(txtLai.getPassword());
        if (cu.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hãy nhập mật khẩu hiện tại.");
            return;
        }
        if (moi.length() < 6 || moi.length() > 30) {
            JOptionPane.showMessageDialog(this, "Mật khẩu mới phải từ 6 đến 30 ký tự.");
            return;
        }
        if (!moi.equals(lai)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu nhập lại không khớp.");
            return;
        }
        if (moi.equals(cu)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu mới phải khác mật khẩu hiện tại.");
            return;
        }
        TaiKhoan_DAO dao = new TaiKhoan_DAO();
        if (!dao.kiemTraMatKhau(tk.getMaTK(), cu)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu hiện tại không đúng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (dao.doiMatKhau(tk.getMaTK(), moi)) {
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
