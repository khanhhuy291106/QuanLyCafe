package gui.dialog;

import dao.TaiKhoanDAO;
import gui.component.BoGocButton;
import gui.component.FormPanel;
import gui.component.UI;
import gui.util.AppColor;
import gui.util.HashUtil;
import gui.util.SessionManager;
import gui.util.Validator;

import javax.swing.*;
import java.awt.*;

/** Hộp thoại đổi mật khẩu cho tài khoản đang đăng nhập. */
public class DlgDoiMatKhau extends JDialog {
    private final JPasswordField txtCu = UI.password(16);
    private final JPasswordField txtMoi = UI.password(16);
    private final JPasswordField txtLai = UI.password(16);

    public DlgDoiMatKhau(Window owner) {
        super(owner, "Đổi mật khẩu", ModalityType.APPLICATION_MODAL);
        setIconImage(UI.appIcon());
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        root.setBackground(AppColor.BG);
        root.add(UI.title("Đổi mật khẩu"), BorderLayout.NORTH);
        FormPanel f = new FormPanel().labelWidth(130);
        f.row("Mật khẩu hiện tại", txtCu);
        f.row("Mật khẩu mới", txtMoi);
        f.row("Nhập lại mật khẩu mới", txtLai);
        root.add(f, BorderLayout.CENTER);
        JPanel bt = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bt.setOpaque(false);
        BoGocButton ok = UI.button("Lưu", AppColor.BROWN);
        BoGocButton huy = new BoGocButton("Hủy", AppColor.BROWN).outline(AppColor.BROWN);
        bt.add(huy);
        bt.add(ok);
        root.add(bt, BorderLayout.SOUTH);
        setContentPane(root);
        ok.addActionListener(UI.on(this::luu));
        huy.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(ok);
        setSize(440, 280);
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void luu() {
        String cu = new String(txtCu.getPassword());
        String moi = new String(txtMoi.getPassword());
        String lai = new String(txtLai.getPassword());
        if (cu.isEmpty()) {
            UI.warn("Hãy nhập mật khẩu hiện tại.");
            return;
        }
        if (!Validator.matKhau(moi)) {
            UI.warn("Mật khẩu mới phải từ 6 đến 30 ký tự.");
            return;
        }
        if (!moi.equals(lai)) {
            UI.warn("Mật khẩu nhập lại không khớp.");
            return;
        }
        if (moi.equals(cu)) {
            UI.warn("Mật khẩu mới phải khác mật khẩu hiện tại.");
            return;
        }
        TaiKhoanDAO dao = new TaiKhoanDAO();
        String maTK = SessionManager.getTaiKhoan().getMaTK();
        if (!dao.kiemTraMatKhau(maTK, HashUtil.sha256(cu))) {
            UI.error("Mật khẩu hiện tại không đúng.");
            return;
        }
        dao.doiMatKhau(maTK, HashUtil.sha256(moi));
        UI.info("Đổi mật khẩu thành công.");
        dispose();
    }
}
