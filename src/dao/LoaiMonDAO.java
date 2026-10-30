package dao;

import entity.LoaiMon;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class LoaiMonDAO extends BaseDAO {
    static LoaiMon map(ResultSet rs) throws SQLException {
        return new LoaiMon(rs.getString("maLoai"), rs.getString("tenLoai"));
    }

    public List<LoaiMon> getAll() {
        return query("SELECT * FROM LoaiMon ORDER BY maLoai", LoaiMonDAO::map);
    }

    public String taoMa() {
        return nextId("LoaiMon", "maLoai", "L", 2);
    }

    public void them(LoaiMon l) {
        l.setMaLoai(taoMa());
        update("INSERT INTO LoaiMon(maLoai, tenLoai) VALUES (?,?)", l.getMaLoai(), l.getTenLoai());
    }

    public void sua(LoaiMon l) {
        update("UPDATE LoaiMon SET tenLoai = ? WHERE maLoai = ?", l.getTenLoai(), l.getMaLoai());
    }

    public void xoa(String maLoai) {
        update("DELETE FROM LoaiMon WHERE maLoai = ?", maLoai);
    }
}
