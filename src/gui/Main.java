package gui;

import java.util.Enumeration;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import connectDB.ConnectDB;

public class Main {
    public static void main(String[] args) {
        try {
            ConnectDB.getInstance().connect();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Không kết nối được SQL Server.\nHãy kiểm tra thông tin trong connectDB/ConnectDB.java và chắc chắn SQL Server đã chạy.\n(" + e.getMessage() + ")",
                    "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        // Font chung, màu nền và chữ tiếng Việt cho hộp thoại
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object k = keys.nextElement();
            if (UIManager.get(k) instanceof FontUIResource) {
                UIManager.put(k, new FontUIResource(TienIch.FONT));
            }
        }
        UIManager.put("Panel.background", TienIch.BG);
        UIManager.put("OptionPane.background", TienIch.BG);
        UIManager.put("CheckBox.background", TienIch.BG);
        UIManager.put("RadioButton.background", TienIch.BG);
        UIManager.put("OptionPane.yesButtonText", "Có");
        UIManager.put("OptionPane.noButtonText", "Không");
        UIManager.put("OptionPane.cancelButtonText", "Hủy");
        UIManager.put("OptionPane.okButtonText", "Đồng ý");

        SwingUtilities.invokeLater(() -> new DangNhap().setVisible(true));
    }
}
