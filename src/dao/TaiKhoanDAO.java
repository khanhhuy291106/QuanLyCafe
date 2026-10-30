package dao;

import entity.NhanVien;
import entity.TaiKhoan;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TaiKhoanDAO extends BaseDAO {

    private static final String SELECT = "SELECT t.maTK, t.tenDangNhap, t.matKhau, t.vaiTro, t.trangThai AS tkTrangThai, n.* "
            + "FROM TaiKhoan t JOIN NhanVien n ON t.maNV = n.maNV ";

    private static TaiKhoan map(ResultSet rs) throws SQLException {
        NhanVien nv = NhanVienDAO.map(rs);
        return new TaiKhoan(rs.getString("maTK"), rs.getString("tenDangNhap"), rs.getString("matKhau"),
                rs.getString("vaiTro"), nv, rs.getBoolean("tkTrangThai"));
    }

    /** Trả về tài khoản nếu đúng tên đăng nhập và mật khẩu (đã băm), ngược lại trả null. */
    public TaiKhoan dangNhap(String tenDangNhap, String matKhauHash) {
        return queryOne(SELECT + "WHERE t.tenDangNhap = ? AND t.matKhau = ?", TaiKhoanDAO::map, tenDangNhap, matKhauHash);
    }

    public TaiKhoan getByNhanVien(String maNV) {
        return queryOne(SELECT + "WHERE t.maNV = ?", TaiKhoanDAO::map, maNV);
    }

    public boolean tonTaiTenDangNhap(String tenDangNhap) {
        return queryOne("SELECT 1 AS x FROM TaiKhoan WHERE tenDangNhap = ?", rs -> 1, tenDangNhap) != null;
    }

    public boolean kiemTraMatKhau(String maTK, String hash) {
        return queryOne("SELECT 1 AS x FROM TaiKhoan WHERE maTK = ? AND matKhau = ?", rs -> 1, maTK, hash) != null;
    }

    public void doiMatKhau(String maTK, String hashMoi) {
        update("UPDATE TaiKhoan SET matKhau = ? WHERE maTK = ?", hashMoi, maTK);
    }

    public void datLaiMatKhau(String maNV, String hashMoi) {
        update("UPDATE TaiKhoan SET matKhau = ? WHERE maNV = ?", hashMoi, maNV);
    }
}
