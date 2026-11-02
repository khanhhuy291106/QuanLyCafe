package dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import connectDB.ConnectDB;
import entity.KhuyenMai;

public class KhuyenMai_DAO {
    private KhuyenMai taoKhuyenMai(ResultSet rs) throws SQLException {
        return new KhuyenMai(rs.getString("maKM"), rs.getString("tenKM"), rs.getString("loaiGiam"), rs.getDouble("giaTri"),
                rs.getDouble("donToiThieu"), rs.getDate("ngayBD").toLocalDate(), rs.getDate("ngayKT").toLocalDate(),
                rs.getBoolean("trangThai"));
    }

    public ArrayList<KhuyenMai> getAll() {
        return timKiem("");
    }

    public ArrayList<KhuyenMai> timKiem(String keyword) {
        ArrayList<KhuyenMai> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM KhuyenMai WHERE maKM LIKE ? OR tenKM LIKE ? ORDER BY maKM");
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ds.add(taoKhuyenMai(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    // Các khuyến mãi dùng được cho đơn có tạm tính này vào ngày này
    public ArrayList<KhuyenMai> getApDung(double tamTinh, LocalDate ngay) {
        ArrayList<KhuyenMai> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM KhuyenMai WHERE trangThai = 1 AND ? BETWEEN ngayBD AND ngayKT AND donToiThieu <= ? ORDER BY maKM");
            stmt.setDate(1, Date.valueOf(ngay));
            stmt.setDouble(2, tamTinh);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ds.add(taoKhuyenMai(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public boolean them(KhuyenMai k) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            k.setMaKM(TaoMa.tiepTheo(con, "KhuyenMai", "maKM", "KM", 3));
            PreparedStatement stmt = con.prepareStatement("INSERT INTO KhuyenMai(maKM, tenKM, loaiGiam, giaTri, donToiThieu, ngayBD, ngayKT, trangThai) VALUES (?,?,?,?,?,?,?,?)");
            stmt.setString(1, k.getMaKM());
            stmt.setString(2, k.getTenKM());
            stmt.setString(3, k.getLoaiGiam());
            stmt.setDouble(4, k.getGiaTri());
            stmt.setDouble(5, k.getDonToiThieu());
            stmt.setDate(6, Date.valueOf(k.getNgayBD()));
            stmt.setDate(7, Date.valueOf(k.getNgayKT()));
            stmt.setBoolean(8, k.isTrangThai());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean sua(KhuyenMai k) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("UPDATE KhuyenMai SET tenKM=?, loaiGiam=?, giaTri=?, donToiThieu=?, ngayBD=?, ngayKT=?, trangThai=? WHERE maKM=?");
            stmt.setString(1, k.getTenKM());
            stmt.setString(2, k.getLoaiGiam());
            stmt.setDouble(3, k.getGiaTri());
            stmt.setDouble(4, k.getDonToiThieu());
            stmt.setDate(5, Date.valueOf(k.getNgayBD()));
            stmt.setDate(6, Date.valueOf(k.getNgayKT()));
            stmt.setBoolean(7, k.isTrangThai());
            stmt.setString(8, k.getMaKM());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean xoa(String maKM) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("DELETE FROM KhuyenMai WHERE maKM = ?");
            stmt.setString(1, maKM);
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }
}
