package gui.frame;

import connect.ConnectDB;
import gui.component.UI;
import gui.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Khung chính (cửa sổ) của ứng dụng. Toàn bộ giao diện và phân quyền nằm trong {@link MainShell}:
 * menu bên trái chỉ hiện các chức năng mà vai trò đang đăng nhập được dùng.
 */
public class FrmMain extends JFrame {
    private final MainShell shell = new MainShell();

    public FrmMain() {
        super("Quản Lý Quán Cafe — " + SessionManager.moTa());
        setIconImage(UI.appIcon());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (UI.confirm("Thoát chương trình?")) {
                    ConnectDB.getInstance().close();
                    System.exit(0);
                }
            }
        });
        setContentPane(shell);
        setMinimumSize(new Dimension(1100, 680));
        setSize(1320, 780);
        setLocationRelativeTo(null);
    }

    /** Mở một màn hình theo khóa (home, sell, orders, menu, staff, stock, promo, stats). */
    public void hienThi(String khoa) {
        shell.hienThi(khoa);
    }
}
