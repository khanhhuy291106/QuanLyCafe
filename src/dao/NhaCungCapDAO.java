package dao;

import entity.NhaCungCap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class NhaCungCapDAO extends BaseDAO {
    static NhaCungCap map(ResultSet rs) throws SQLException {
        return new NhaCungCap(rs.getString("maNCC"), rs.getString("tenNCC"), rs.getString("sdt"), rs.getString("diaChi"));
    }

    public List<NhaCungCap> getAll() {
        return query("SELECT * FROM NhaCungCap ORDER BY maNCC", NhaCungCapDAO::map);
    }

    public List<NhaCungCap> timKiem(String keyword) {
        String k = "%" + keyword + "%";
        return query("SELECT * FROM NhaCungCap WHERE maNCC LIKE ? OR tenNCC LIKE ? OR sdt LIKE ? ORDER BY maNCC", NhaCungCapDAO::map, k, k, k);
    }

    public String taoMa() {
        return nextId("NhaCungCap", "maNCC", "NCC", 2);
    }

    public void them(NhaCungCap n) {
        n.setMaNCC(taoMa());
        update("INSERT INTO NhaCungCap(maNCC, tenNCC, sdt, diaChi) VALUES (?,?,?,?)", n.getMaNCC(), n.getTenNCC(), n.getSdt(), n.getDiaChi());
    }

    public void sua(NhaCungCap n) {
        update("UPDATE NhaCungCap SET tenNCC=?, sdt=?, diaChi=? WHERE maNCC=?", n.getTenNCC(), n.getSdt(), n.getDiaChi(), n.getMaNCC());
    }

    public void xoa(String ma) {
        update("DELETE FROM NhaCungCap WHERE maNCC = ?", ma);
    }
}
