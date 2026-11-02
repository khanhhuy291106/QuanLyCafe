package dao;

import java.sql.*;
import java.util.ArrayList;
import connectDB.ConnectDB;
import entity.NguyenLieu;

public class NguyenLieu_DAO {
    public static NguyenLieu taoNguyenLieu(ResultSet rs) throws SQLException {
        return new NguyenLieu(rs.getString("maNL"), rs.getString("tenNL"), rs.getString("donViTinh"),
                rs.getDouble("soLuongTon"), rs.getDouble("mucToiThieu"));
    }

    public ArrayList<NguyenLieu> getAll() {
        return timKiem("");
    }

    public ArrayList<NguyenLieu> timKiem(String keyword) {
        ArrayList<NguyenLieu> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM NguyenLieu WHERE maNL LIKE ? OR tenNL LIKE ? ORDER BY maNL");
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ds.add(taoNguyenLieu(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    // Nguyên liệu có tồn kho dưới mức tối thiểu
    public ArrayList<NguyenLieu> getSapHet() {
        ArrayList<NguyenLieu> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM NguyenLieu WHERE soLuongTon < mucToiThieu ORDER BY maNL");
            while (rs.next()) {
                ds.add(taoNguyenLieu(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public boolean them(NguyenLieu nl) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            nl.setMaNL(TaoMa.tiepTheo(con, "NguyenLieu", "maNL", "NL", 2));
            PreparedStatement stmt = con.prepareStatement("INSERT INTO NguyenLieu(maNL, tenNL, donViTinh, soLuongTon, mucToiThieu) VALUES (?,?,?,?,?)");
            stmt.setString(1, nl.getMaNL());
            stmt.setString(2, nl.getTenNL());
            stmt.setString(3, nl.getDonViTinh());
            stmt.setDouble(4, nl.getSoLuongTon());
            stmt.setDouble(5, nl.getMucToiThieu());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean sua(NguyenLieu nl) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("UPDATE NguyenLieu SET tenNL=?, donViTinh=?, soLuongTon=?, mucToiThieu=? WHERE maNL=?");
            stmt.setString(1, nl.getTenNL());
            stmt.setString(2, nl.getDonViTinh());
            stmt.setDouble(3, nl.getSoLuongTon());
            stmt.setDouble(4, nl.getMucToiThieu());
            stmt.setString(5, nl.getMaNL());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean xoa(String maNL) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("DELETE FROM NguyenLieu WHERE maNL = ?");
            stmt.setString(1, maNL);
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }
}
