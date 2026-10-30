package dao;

import entity.NhanVien;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class NhanVienDAO extends BaseDAO {

    public static NhanVien map(ResultSet rs) throws SQLException {
        return new NhanVien(rs.getString("maNV"), rs.getString("hoTen"), rs.getBoolean("gioiTinh"),
                date(rs, "ngaySinh"), rs.getString("sdt"), rs.getString("diaChi"), rs.getString("chucVu"),
                rs.getBoolean("trangThai"));
    }

    public List<NhanVien> getAll() {
        return query("SELECT * FROM NhanVien ORDER BY maNV", NhanVienDAO::map);
    }

    public List<NhanVien> timKiem(String keyword) {
        String k = "%" + keyword + "%";
        return query("SELECT * FROM NhanVien WHERE maNV LIKE ? OR hoTen LIKE ? OR sdt LIKE ? ORDER BY maNV",
                NhanVienDAO::map, k, k, k);
    }

    public NhanVien getById(String maNV) {
        return queryOne("SELECT * FROM NhanVien WHERE maNV = ?", NhanVienDAO::map, maNV);
    }

    public String taoMa() {
        return nextId("NhanVien", "maNV", "NV", 3);
    }

    /** Thêm nhân viên và tài khoản đăng nhập trong cùng một transaction. */
    public void themVoiTaiKhoan(NhanVien nv, String tenDangNhap, String matKhauHash) {
        inTransaction(c -> {
            nv.setMaNV(nextId(c, "NhanVien", "maNV", "NV", 3));
            exec(c, "INSERT INTO NhanVien(maNV, hoTen, gioiTinh, ngaySinh, sdt, diaChi, chucVu, trangThai) VALUES (?,?,?,?,?,?,?,?)",
                    nv.getMaNV(), nv.getHoTen(), nv.isGioiTinh(), nv.getNgaySinh(), nv.getSdt(), nv.getDiaChi(),
                    nv.getChucVu(), nv.isTrangThai());
            String maTK = nextId(c, "TaiKhoan", "maTK", "TK", 3);
            exec(c, "INSERT INTO TaiKhoan(maTK, tenDangNhap, matKhau, vaiTro, maNV, trangThai) VALUES (?,?,?,?,?,?)",
                    maTK, tenDangNhap, matKhauHash, nv.getChucVu(), nv.getMaNV(), nv.isTrangThai());
        });
    }

    /** Sửa nhân viên; vai trò và trạng thái của tài khoản đi theo chức vụ và trạng thái nhân viên. */
    public void capNhat(NhanVien nv) {
        inTransaction(c -> {
            exec(c, "UPDATE NhanVien SET hoTen=?, gioiTinh=?, ngaySinh=?, sdt=?, diaChi=?, chucVu=?, trangThai=? WHERE maNV=?",
                    nv.getHoTen(), nv.isGioiTinh(), nv.getNgaySinh(), nv.getSdt(), nv.getDiaChi(), nv.getChucVu(),
                    nv.isTrangThai(), nv.getMaNV());
            exec(c, "UPDATE TaiKhoan SET vaiTro=?, trangThai=? WHERE maNV=?", nv.getChucVu(), nv.isTrangThai(), nv.getMaNV());
        });
    }

    /** Cho nghỉ việc: không xóa dữ liệu, chỉ khóa nhân viên và tài khoản. */
    public void choNghiViec(String maNV) {
        inTransaction(c -> {
            exec(c, "UPDATE NhanVien SET trangThai = 0 WHERE maNV = ?", maNV);
            exec(c, "UPDATE TaiKhoan SET trangThai = 0 WHERE maNV = ?", maNV);
        });
    }
}
