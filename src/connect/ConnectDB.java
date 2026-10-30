package connect;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Quản lý kết nối SQL Server (mẫu Singleton).
 * Thông tin kết nối đọc từ config/config.properties (SQL Authentication).
 */
public class ConnectDB {
    private static ConnectDB instance;
    private Connection con;
    private final Properties cfg = new Properties();

    private ConnectDB() {
        // Cấu hình mặc định phòng khi không đọc được file
        cfg.setProperty("db.host", "localhost");
        cfg.setProperty("db.port", "1433");
        cfg.setProperty("db.instance", "");
        cfg.setProperty("db.name", "QuanLyCafe");
        cfg.setProperty("db.user", "sa");
        
        // ==> ĐIỀN ĐÚNG MẬT KHẨU 6 KÝ TỰ BẠN ĐÃ ĐẶT VÀO ĐÂY <==
        cfg.setProperty("db.password", "123456"); 
        
        cfg.setProperty("db.encrypt", "true");

        // Cách đọc file hỗ trợ cả ClassLoader lẫn FileInputStream thông thường
        boolean loaded = false;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                try (InputStreamReader in = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    cfg.load(in);
                    loaded = true;
                }
            }
        } catch (Exception ignored) {}

        if (!loaded) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("config/config.properties")) {
                if (is != null) {
                    try (InputStreamReader in = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                        cfg.load(in);
                        loaded = true;
                    }
                }
            } catch (Exception ignored) {}
        }

        if (!loaded) {
            try (InputStreamReader in = new InputStreamReader(new FileInputStream("config/config.properties"), StandardCharsets.UTF_8)) {
                cfg.load(in);
            } catch (IOException e) {
                System.err.println("Dùng cấu hình mặc định trong code: " + e.getMessage());
            }
        }
    }

    public static synchronized ConnectDB getInstance() {
        if (instance == null) instance = new ConnectDB();
        return instance;
    }

    private String buildUrl() {
        String host = cfg.getProperty("db.host").trim();
        String instanceName = cfg.getProperty("db.instance").trim();
        String server = instanceName.isEmpty() ? host + ":" + cfg.getProperty("db.port").trim() : host + "\\" + instanceName;
        return "jdbc:sqlserver://" + server + ";databaseName=" + cfg.getProperty("db.name").trim()
                + ";encrypt=" + cfg.getProperty("db.encrypt").trim() + ";trustServerCertificate=true;loginTimeout=10";
    }

    /** Trả về kết nối đang dùng, tự mở lại nếu đã bị đóng. */
    public synchronized Connection getConnection() throws SQLException {
        if (con == null || con.isClosed() || !con.isValid(2)) {
            con = DriverManager.getConnection(buildUrl(), cfg.getProperty("db.user"), cfg.getProperty("db.password"));
        }
        return con;
    }

    public synchronized void close() {
        try {
            if (con != null && !con.isClosed()) con.close();
        } catch (SQLException ignored) {
        }
        con = null;
    }
}