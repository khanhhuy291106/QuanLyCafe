package dao;

import java.sql.*;
import java.util.ArrayList;
import connectDB.ConnectDB;
import entity.NhanVien;

public class NhanVien_DAO {
    public static NhanVien taoNhanVien(ResultSet rs) throws SQLException {
        return new NhanVien(rs.getString("maNV"), rs.getString("hoTen"), rs.getBoolean("gioiTinh"),
                rs.getDate("ngaySinh").toLocalDate(), rs.getString("sdt"), rs.getString("diaChi"),
                rs.getString("chucVu"), rs.getBoolean("trangThai"));
    }

    public ArrayList<NhanVien> getAll() {
        return timKiem("");
    }

    public ArrayList<NhanVien> timKiem(String keyword) {
        ArrayList<NhanVien> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String k = "%" + keyword + "%";
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM NhanVien WHERE maNV LIKE ? OR hoTen LIKE ? OR sdt LIKE ? ORDER BY maNV");
            stmt.setString(1, k);
            stmt.setString(2, k);
            stmt.setString(3, k);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ds.add(taoNhanVien(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    // Thêm nhân viên và tài khoản đăng nhập trong cùng một transaction
    public boolean themVoiTaiKhoan(NhanVien nv, String tenDangNhap, String matKhauHash) {
        Connection con = ConnectDB.getConnection();
        try {
            con.setAutoCommit(false);
            nv.setMaNV(TaoMa.tiepTheo(con, "NhanVien", "maNV", "NV", 3));
            PreparedStatement stmt = con.prepareStatement("INSERT INTO NhanVien(maNV, hoTen, gioiTinh, ngaySinh, sdt, diaChi, chucVu, trangThai) VALUES (?,?,?,?,?,?,?,?)");
            stmt.setString(1, nv.getMaNV());
            stmt.setString(2, nv.getHoTen());
            stmt.setBoolean(3, nv.isGioiTinh());
            stmt.setDate(4, Date.valueOf(nv.getNgaySinh()));
            stmt.setString(5, nv.getSdt());
            stmt.setString(6, nv.getDiaChi());
            stmt.setString(7, nv.getChucVu());
            stmt.setBoolean(8, nv.isTrangThai());
            stmt.executeUpdate();

            String maTK = TaoMa.tiepTheo(con, "TaiKhoan", "maTK", "TK", 3);
            stmt = con.prepareStatement("INSERT INTO TaiKhoan(maTK, tenDangNhap, matKhau, vaiTro, maNV, trangThai) VALUES (?,?,?,?,?,?)");
            stmt.setString(1, maTK);
            stmt.setString(2, tenDangNhap);
            stmt.setString(3, matKhauHash);
            stmt.setString(4, nv.getChucVu());
            stmt.setString(5, nv.getMaNV());
            stmt.setBoolean(6, nv.isTrangThai());
            stmt.executeUpdate();
            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // Sửa nhân viên; vai trò và trạng thái của tài khoản đi theo chức vụ và trạng thái nhân viên
    public boolean capNhat(NhanVien nv) {
        Connection con = ConnectDB.getConnection();
        try {
            con.setAutoCommit(false);
            PreparedStatement stmt = con.prepareStatement("UPDATE NhanVien SET hoTen=?, gioiTinh=?, ngaySinh=?, sdt=?, diaChi=?, chucVu=?, trangThai=? WHERE maNV=?");
            stmt.setString(1, nv.getHoTen());
            stmt.setBoolean(2, nv.isGioiTinh());
            stmt.setDate(3, Date.valueOf(nv.getNgaySinh()));
            stmt.setString(4, nv.getSdt());
            stmt.setString(5, nv.getDiaChi());
            stmt.setString(6, nv.getChucVu());
            stmt.setBoolean(7, nv.isTrangThai());
            stmt.setString(8, nv.getMaNV());
            stmt.executeUpdate();

            stmt = con.prepareStatement("UPDATE TaiKhoan SET vaiTro=?, trangThai=? WHERE maNV=?");
            stmt.setString(1, nv.getChucVu());
            stmt.setBoolean(2, nv.isTrangThai());
            stmt.setString(3, nv.getMaNV());
            stmt.executeUpdate();
            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // Cho nghỉ việc: không xóa dữ liệu, chỉ khóa nhân viên và tài khoản
    public boolean choNghiViec(String maNV) {
        Connection con = ConnectDB.getConnection();
        try {
            con.setAutoCommit(false);
            PreparedStatement stmt = con.prepareStatement("UPDATE NhanVien SET trangThai = 0 WHERE maNV = ?");
            stmt.setString(1, maNV);
            stmt.executeUpdate();
            stmt = con.prepareStatement("UPDATE TaiKhoan SET trangThai = 0 WHERE maNV = ?");
            stmt.setString(1, maNV);
            stmt.executeUpdate();
            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
