package dao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import connectDB.ConnectDB;
import entity.NhanVien;
import entity.TaiKhoan;

public class TaiKhoan_DAO {
    private static final String SELECT = "SELECT t.maTK, t.tenDangNhap, t.matKhau, t.vaiTro, t.trangThai AS tkTrangThai, n.* "
            + "FROM TaiKhoan t JOIN NhanVien n ON t.maNV = n.maNV ";

    // Băm mật khẩu bằng SHA-256 (CSDL không lưu mật khẩu chữ thường)
    public static String bamMatKhau(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            String kq = "";
            for (byte b : d) {
                kq += String.format("%02x", b);
            }
            return kq;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private TaiKhoan taoTaiKhoan(ResultSet rs) throws SQLException {
        NhanVien nv = NhanVien_DAO.taoNhanVien(rs);
        return new TaiKhoan(rs.getString("maTK"), rs.getString("tenDangNhap"), rs.getString("matKhau"),
                rs.getString("vaiTro"), nv, rs.getBoolean("tkTrangThai"));
    }

    // Trả về tài khoản nếu đúng tên đăng nhập và mật khẩu, ngược lại trả null
    public TaiKhoan dangNhap(String tenDangNhap, String matKhau) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(SELECT + "WHERE t.tenDangNhap = ? AND t.matKhau = ?");
            stmt.setString(1, tenDangNhap);
            stmt.setString(2, bamMatKhau(matKhau));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return taoTaiKhoan(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public TaiKhoan getByNhanVien(String maNV) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(SELECT + "WHERE t.maNV = ?");
            stmt.setString(1, maNV);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return taoTaiKhoan(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean tonTaiTenDangNhap(String tenDangNhap) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT 1 FROM TaiKhoan WHERE tenDangNhap = ?");
            stmt.setString(1, tenDangNhap);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean kiemTraMatKhau(String maTK, String matKhau) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT 1 FROM TaiKhoan WHERE maTK = ? AND matKhau = ?");
            stmt.setString(1, maTK);
            stmt.setString(2, bamMatKhau(matKhau));
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean doiMatKhau(String maTK, String matKhauMoi) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("UPDATE TaiKhoan SET matKhau = ? WHERE maTK = ?");
            stmt.setString(1, bamMatKhau(matKhauMoi));
            stmt.setString(2, maTK);
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean datLaiMatKhau(String maNV, String matKhauMoi) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("UPDATE TaiKhoan SET matKhau = ? WHERE maNV = ?");
            stmt.setString(1, bamMatKhau(matKhauMoi));
            stmt.setString(2, maNV);
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }
}
