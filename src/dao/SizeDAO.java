package dao;

import entity.Size;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SizeDAO extends BaseDAO {
    static Size map(ResultSet rs) throws SQLException {
        return new Size(rs.getString("maSize"), rs.getString("tenSize"), rs.getDouble("phuThu"));
    }

    public List<Size> getAll() {
        return query("SELECT * FROM [Size] ORDER BY phuThu, maSize", SizeDAO::map);
    }

    public String taoMa() {
        return nextId("[Size]", "maSize", "S", 2);
    }

    public void them(Size s) {
        s.setMaSize(taoMa());
        update("INSERT INTO [Size](maSize, tenSize, phuThu) VALUES (?,?,?)", s.getMaSize(), s.getTenSize(), s.getPhuThu());
    }

    public void sua(Size s) {
        update("UPDATE [Size] SET tenSize = ?, phuThu = ? WHERE maSize = ?", s.getTenSize(), s.getPhuThu(), s.getMaSize());
    }

    public void xoa(String maSize) {
        update("DELETE FROM [Size] WHERE maSize = ?", maSize);
    }
}
