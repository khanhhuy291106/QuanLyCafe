package dao;

import entity.LoaiMon;
import entity.Mon;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MonDAO extends BaseDAO {
    private static final String SELECT = "SELECT m.maMon, m.tenMon, m.giaCoBan, m.hinhAnh, m.trangThai, l.maLoai, l.tenLoai "
            + "FROM Mon m JOIN LoaiMon l ON m.maLoai = l.maLoai ";

    static Mon map(ResultSet rs) throws SQLException {
        return new Mon(rs.getString("maMon"), rs.getString("tenMon"), rs.getDouble("giaCoBan"), rs.getString("hinhAnh"),
                new LoaiMon(rs.getString("maLoai"), rs.getString("tenLoai")), rs.getBoolean("trangThai"));
    }

    public List<Mon> getAll() {
        return query(SELECT + "ORDER BY m.maMon", MonDAO::map);
    }

    /** maLoai = null nghĩa là tất cả loại. */
    public List<Mon> timKiem(String keyword, String maLoai) {
        String k = "%" + keyword + "%";
        if (maLoai == null) return query(SELECT + "WHERE (m.tenMon LIKE ? OR m.maMon LIKE ?) ORDER BY m.maMon", MonDAO::map, k, k);
        return query(SELECT + "WHERE (m.tenMon LIKE ? OR m.maMon LIKE ?) AND m.maLoai = ? ORDER BY m.maMon", MonDAO::map, k, k, maLoai);
    }

    public List<Mon> getDangBan() {
        return query(SELECT + "WHERE m.trangThai = 1 ORDER BY l.maLoai, m.maMon", MonDAO::map);
    }

    public String taoMa() {
        return nextId("Mon", "maMon", "MON", 3);
    }

    public void them(Mon m) {
        m.setMaMon(taoMa());
        update("INSERT INTO Mon(maMon, tenMon, giaCoBan, hinhAnh, maLoai, trangThai) VALUES (?,?,?,?,?,?)",
                m.getMaMon(), m.getTenMon(), m.getGiaCoBan(), m.getHinhAnh(), m.getLoaiMon().getMaLoai(), m.isTrangThai());
    }

    public void sua(Mon m) {
        update("UPDATE Mon SET tenMon=?, giaCoBan=?, hinhAnh=?, maLoai=?, trangThai=? WHERE maMon=?",
                m.getTenMon(), m.getGiaCoBan(), m.getHinhAnh(), m.getLoaiMon().getMaLoai(), m.isTrangThai(), m.getMaMon());
    }

    public void xoa(String maMon) {
        update("DELETE FROM Mon WHERE maMon = ?", maMon);
    }
}
