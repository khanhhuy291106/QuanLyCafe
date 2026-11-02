package dao;

import java.sql.*;
import java.util.ArrayList;
import connectDB.ConnectDB;
import entity.LoaiMon;
import entity.Mon;

public class Mon_DAO {
    private static final String SELECT = "SELECT m.maMon, m.tenMon, m.giaCoBan, m.hinhAnh, m.trangThai, l.maLoai, l.tenLoai "
            + "FROM Mon m JOIN LoaiMon l ON m.maLoai = l.maLoai ";

    public static Mon taoMon(ResultSet rs) throws SQLException {
        LoaiMon loai = new LoaiMon(rs.getString("maLoai"), rs.getString("tenLoai"));
        return new Mon(rs.getString("maMon"), rs.getString("tenMon"), rs.getDouble("giaCoBan"),
                rs.getString("hinhAnh"), loai, rs.getBoolean("trangThai"));
    }

    // maLoai = null nghĩa là tất cả loại
    public ArrayList<Mon> timKiem(String keyword, String maLoai) {
        ArrayList<Mon> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String k = "%" + keyword + "%";
            PreparedStatement stmt;
            if (maLoai == null) {
                stmt = con.prepareStatement(SELECT + "WHERE (m.tenMon LIKE ? OR m.maMon LIKE ?) ORDER BY m.maMon");
                stmt.setString(1, k);
                stmt.setString(2, k);
            } else {
                stmt = con.prepareStatement(SELECT + "WHERE (m.tenMon LIKE ? OR m.maMon LIKE ?) AND m.maLoai = ? ORDER BY m.maMon");
                stmt.setString(1, k);
                stmt.setString(2, k);
                stmt.setString(3, maLoai);
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ds.add(taoMon(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    // Chỉ các món đang bán (dùng ở màn hình Bán hàng)
    public ArrayList<Mon> getDangBan() {
        ArrayList<Mon> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(SELECT + "WHERE m.trangThai = 1 ORDER BY l.maLoai, m.maMon");
            while (rs.next()) {
                ds.add(taoMon(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public boolean them(Mon m) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            m.setMaMon(TaoMa.tiepTheo(con, "Mon", "maMon", "MON", 3));
            PreparedStatement stmt = con.prepareStatement("INSERT INTO Mon(maMon, tenMon, giaCoBan, hinhAnh, maLoai, trangThai) VALUES (?,?,?,?,?,?)");
            stmt.setString(1, m.getMaMon());
            stmt.setString(2, m.getTenMon());
            stmt.setDouble(3, m.getGiaCoBan());
            stmt.setString(4, m.getHinhAnh());
            stmt.setString(5, m.getLoaiMon().getMaLoai());
            stmt.setBoolean(6, m.isTrangThai());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean sua(Mon m) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("UPDATE Mon SET tenMon=?, giaCoBan=?, hinhAnh=?, maLoai=?, trangThai=? WHERE maMon=?");
            stmt.setString(1, m.getTenMon());
            stmt.setDouble(2, m.getGiaCoBan());
            stmt.setString(3, m.getHinhAnh());
            stmt.setString(4, m.getLoaiMon().getMaLoai());
            stmt.setBoolean(5, m.isTrangThai());
            stmt.setString(6, m.getMaMon());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean xoa(String maMon) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("DELETE FROM Mon WHERE maMon = ?");
            stmt.setString(1, maMon);
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }
}
