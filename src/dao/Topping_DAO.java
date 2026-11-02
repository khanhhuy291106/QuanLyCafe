package dao;

import java.sql.*;
import java.util.ArrayList;
import connectDB.ConnectDB;
import entity.Topping;

public class Topping_DAO {
    private Topping taoTopping(ResultSet rs) throws SQLException {
        return new Topping(rs.getString("maTopping"), rs.getString("tenTopping"), rs.getDouble("gia"), rs.getBoolean("trangThai"));
    }

    public ArrayList<Topping> getAll() {
        ArrayList<Topping> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Topping ORDER BY maTopping");
            while (rs.next()) {
                ds.add(taoTopping(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    // Chỉ các topping đang bán (dùng ở màn hình Bán hàng)
    public ArrayList<Topping> getDangBan() {
        ArrayList<Topping> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Topping WHERE trangThai = 1 ORDER BY maTopping");
            while (rs.next()) {
                ds.add(taoTopping(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public boolean them(Topping t) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            t.setMaTopping(TaoMa.tiepTheo(con, "Topping", "maTopping", "TP", 2));
            PreparedStatement stmt = con.prepareStatement("INSERT INTO Topping(maTopping, tenTopping, gia, trangThai) VALUES (?,?,?,?)");
            stmt.setString(1, t.getMaTopping());
            stmt.setString(2, t.getTenTopping());
            stmt.setDouble(3, t.getGia());
            stmt.setBoolean(4, t.isTrangThai());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean sua(Topping t) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("UPDATE Topping SET tenTopping = ?, gia = ?, trangThai = ? WHERE maTopping = ?");
            stmt.setString(1, t.getTenTopping());
            stmt.setDouble(2, t.getGia());
            stmt.setBoolean(3, t.isTrangThai());
            stmt.setString(4, t.getMaTopping());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean xoa(String ma) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("DELETE FROM Topping WHERE maTopping = ?");
            stmt.setString(1, ma);
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }
}
