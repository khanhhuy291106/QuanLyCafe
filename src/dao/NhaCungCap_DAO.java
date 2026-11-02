package dao;

import java.sql.*;
import java.util.ArrayList;
import connectDB.ConnectDB;
import entity.NhaCungCap;

public class NhaCungCap_DAO {
    public ArrayList<NhaCungCap> getAll() {
        return timKiem("");
    }

    public ArrayList<NhaCungCap> timKiem(String keyword) {
        ArrayList<NhaCungCap> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String k = "%" + keyword + "%";
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM NhaCungCap WHERE maNCC LIKE ? OR tenNCC LIKE ? OR sdt LIKE ? ORDER BY maNCC");
            stmt.setString(1, k);
            stmt.setString(2, k);
            stmt.setString(3, k);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ds.add(new NhaCungCap(rs.getString("maNCC"), rs.getString("tenNCC"), rs.getString("sdt"), rs.getString("diaChi")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public boolean them(NhaCungCap ncc) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            ncc.setMaNCC(TaoMa.tiepTheo(con, "NhaCungCap", "maNCC", "NCC", 2));
            PreparedStatement stmt = con.prepareStatement("INSERT INTO NhaCungCap(maNCC, tenNCC, sdt, diaChi) VALUES (?,?,?,?)");
            stmt.setString(1, ncc.getMaNCC());
            stmt.setString(2, ncc.getTenNCC());
            stmt.setString(3, ncc.getSdt());
            stmt.setString(4, ncc.getDiaChi());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean sua(NhaCungCap ncc) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("UPDATE NhaCungCap SET tenNCC=?, sdt=?, diaChi=? WHERE maNCC=?");
            stmt.setString(1, ncc.getTenNCC());
            stmt.setString(2, ncc.getSdt());
            stmt.setString(3, ncc.getDiaChi());
            stmt.setString(4, ncc.getMaNCC());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean xoa(String maNCC) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("DELETE FROM NhaCungCap WHERE maNCC = ?");
            stmt.setString(1, maNCC);
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }
}
