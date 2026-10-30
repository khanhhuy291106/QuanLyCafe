package dao;

import entity.KhuyenMai;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class KhuyenMaiDAO extends BaseDAO {
    static KhuyenMai map(ResultSet rs) throws SQLException {
        return new KhuyenMai(rs.getString("maKM"), rs.getString("tenKM"), rs.getString("loaiGiam"), rs.getDouble("giaTri"),
                rs.getDouble("donToiThieu"), date(rs, "ngayBD"), date(rs, "ngayKT"), rs.getBoolean("trangThai"));
    }

    public List<KhuyenMai> getAll() {
        return query("SELECT * FROM KhuyenMai ORDER BY maKM", KhuyenMaiDAO::map);
    }

    public List<KhuyenMai> timKiem(String keyword) {
        String k = "%" + keyword + "%";
        return query("SELECT * FROM KhuyenMai WHERE maKM LIKE ? OR tenKM LIKE ? ORDER BY maKM", KhuyenMaiDAO::map, k, k);
    }

    /** Các khuyến mãi dùng được cho đơn có tạm tính này vào ngày này. */
    public List<KhuyenMai> getApDung(double tamTinh, LocalDate ngay) {
        return query("SELECT * FROM KhuyenMai WHERE trangThai = 1 AND ? BETWEEN ngayBD AND ngayKT AND donToiThieu <= ? ORDER BY maKM",
                KhuyenMaiDAO::map, ngay, tamTinh);
    }

    public String taoMa() {
        return nextId("KhuyenMai", "maKM", "KM", 3);
    }

    public void them(KhuyenMai k) {
        k.setMaKM(taoMa());
        update("INSERT INTO KhuyenMai(maKM, tenKM, loaiGiam, giaTri, donToiThieu, ngayBD, ngayKT, trangThai) VALUES (?,?,?,?,?,?,?,?)",
                k.getMaKM(), k.getTenKM(), k.getLoaiGiam(), k.getGiaTri(), k.getDonToiThieu(), k.getNgayBD(), k.getNgayKT(), k.isTrangThai());
    }

    public void sua(KhuyenMai k) {
        update("UPDATE KhuyenMai SET tenKM=?, loaiGiam=?, giaTri=?, donToiThieu=?, ngayBD=?, ngayKT=?, trangThai=? WHERE maKM=?",
                k.getTenKM(), k.getLoaiGiam(), k.getGiaTri(), k.getDonToiThieu(), k.getNgayBD(), k.getNgayKT(), k.isTrangThai(), k.getMaKM());
    }

    public void xoa(String maKM) {
        update("DELETE FROM KhuyenMai WHERE maKM = ?", maKM);
    }
}
