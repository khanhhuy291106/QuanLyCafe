package dao;

import entity.Topping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ToppingDAO extends BaseDAO {
    static Topping map(ResultSet rs) throws SQLException {
        return new Topping(rs.getString("maTopping"), rs.getString("tenTopping"), rs.getDouble("gia"), rs.getBoolean("trangThai"));
    }

    public List<Topping> getAll() {
        return query("SELECT * FROM Topping ORDER BY maTopping", ToppingDAO::map);
    }

    public List<Topping> getDangBan() {
        return query("SELECT * FROM Topping WHERE trangThai = 1 ORDER BY maTopping", ToppingDAO::map);
    }

    public String taoMa() {
        return nextId("Topping", "maTopping", "TP", 2);
    }

    public void them(Topping t) {
        t.setMaTopping(taoMa());
        update("INSERT INTO Topping(maTopping, tenTopping, gia, trangThai) VALUES (?,?,?,?)",
                t.getMaTopping(), t.getTenTopping(), t.getGia(), t.isTrangThai());
    }

    public void sua(Topping t) {
        update("UPDATE Topping SET tenTopping = ?, gia = ?, trangThai = ? WHERE maTopping = ?",
                t.getTenTopping(), t.getGia(), t.isTrangThai(), t.getMaTopping());
    }

    public void xoa(String ma) {
        update("DELETE FROM Topping WHERE maTopping = ?", ma);
    }
}
