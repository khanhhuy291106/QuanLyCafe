package dao;

import java.sql.*;
import java.util.ArrayList;
import connectDB.ConnectDB;
import entity.Size;

public class Size_DAO {
    public static Size taoSize(ResultSet rs) throws SQLException {
        return new Size(rs.getString("maSize"), rs.getString("tenSize"), rs.getDouble("phuThu"));
    }

    public ArrayList<Size> getAll() {
        ArrayList<Size> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM [Size] ORDER BY phuThu, maSize");
            while (rs.next()) {
                ds.add(taoSize(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public boolean them(Size s) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            s.setMaSize(TaoMa.tiepTheo(con, "[Size]", "maSize", "S", 2));
            PreparedStatement stmt = con.prepareStatement("INSERT INTO [Size](maSize, tenSize, phuThu) VALUES (?,?,?)");
            stmt.setString(1, s.getMaSize());
            stmt.setString(2, s.getTenSize());
            stmt.setDouble(3, s.getPhuThu());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean sua(Size s) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("UPDATE [Size] SET tenSize = ?, phuThu = ? WHERE maSize = ?");
            stmt.setString(1, s.getTenSize());
            stmt.setDouble(2, s.getPhuThu());
            stmt.setString(3, s.getMaSize());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean xoa(String maSize) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("DELETE FROM [Size] WHERE maSize = ?");
            stmt.setString(1, maSize);
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }
}
