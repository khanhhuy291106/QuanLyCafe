package gui;

import gui.component.UI;
import gui.frame.FrmDangNhap;

import javax.swing.*;

/** Điểm khởi chạy chương trình: mở màn hình đăng nhập. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UI.installTheme();
            new FrmDangNhap().setVisible(true);
        });
    }
}
