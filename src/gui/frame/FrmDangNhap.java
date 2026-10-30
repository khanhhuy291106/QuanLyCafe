package gui.frame;

import gui.component.UI;

import javax.swing.*;

/** Cửa sổ đăng nhập. Nội dung nằm trong {@link PnlDangNhap}. */
public class FrmDangNhap extends JFrame {
    public FrmDangNhap() {
        super("Đăng nhập - Quản lý quán cafe");
        setIconImage(UI.appIcon());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(new PnlDangNhap());
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }
}
