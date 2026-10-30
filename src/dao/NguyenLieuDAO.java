package dao;

import entity.NguyenLieu;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class NguyenLieuDAO extends BaseDAO {
    static NguyenLieu map(ResultSet rs) throws SQLException {
        return new NguyenLieu(rs.getString("maNL"), rs.getString("tenNL"), rs.getString("donViTinh"),
                rs.getDouble("soLuongTon"), rs.getDouble("mucToiThieu"));
    }

    public List<NguyenLieu> getAll() {
        return query("SELECT * FROM NguyenLieu ORDER BY maNL", NguyenLieuDAO::map);
    }

    public List<NguyenLieu> timKiem(String keyword) {
        String k = "%" + keyword + "%";
        return query("SELECT * FROM NguyenLieu WHERE maNL LIKE ? OR tenNL LIKE ? ORDER BY maNL", NguyenLieuDAO::map, k, k);
    }

    public List<NguyenLieu> getSapHet() {
        return query("SELECT * FROM NguyenLieu WHERE soLuongTon < mucToiThieu ORDER BY maNL", NguyenLieuDAO::map);
    }

    public String taoMa() {
        return nextId("NguyenLieu", "maNL", "NL", 2);
    }

    public void them(NguyenLieu n) {
        n.setMaNL(taoMa());
        update("INSERT INTO NguyenLieu(maNL, tenNL, donViTinh, soLuongTon, mucToiThieu) VALUES (?,?,?,?,?)",
                n.getMaNL(), n.getTenNL(), n.getDonViTinh(), n.getSoLuongTon(), n.getMucToiThieu());
    }

    public void sua(NguyenLieu n) {
        update("UPDATE NguyenLieu SET tenNL=?, donViTinh=?, soLuongTon=?, mucToiThieu=? WHERE maNL=?",
                n.getTenNL(), n.getDonViTinh(), n.getSoLuongTon(), n.getMucToiThieu(), n.getMaNL());
    }

    public void xoa(String ma) {
        update("DELETE FROM NguyenLieu WHERE maNL = ?", ma);
    }
}
