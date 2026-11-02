package dao;

import java.sql.*;
import java.util.ArrayList;
import connectDB.ConnectDB;
import entity.LoaiMon;

public class LoaiMon_DAO {
    public ArrayList<LoaiMon> getAll() {
        ArrayList<LoaiMon> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM LoaiMon ORDER BY maLoai");
            while (rs.next()) {
                ds.add(new LoaiMon(rs.getString("maLoai"), rs.getString("tenLoai")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public boolean them(LoaiMon l) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            l.setMaLoai(TaoMa.tiepTheo(con, "LoaiMon", "maLoai", "L", 2));
            PreparedStatement stmt = con.prepareStatement("INSERT INTO LoaiMon(maLoai, tenLoai) VALUES (?,?)");
            stmt.setString(1, l.getMaLoai());
            stmt.setString(2, l.getTenLoai());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean sua(LoaiMon l) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("UPDATE LoaiMon SET tenLoai = ? WHERE maLoai = ?");
            stmt.setString(1, l.getTenLoai());
            stmt.setString(2, l.getMaLoai());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean xoa(String maLoai) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("DELETE FROM LoaiMon WHERE maLoai = ?");
            stmt.setString(1, maLoai);
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }
}
