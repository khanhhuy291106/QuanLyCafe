package dao;

import entity.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PhieuXuatDAO extends BaseDAO {

    private static PhieuXuat map(ResultSet rs) throws SQLException {
        PhieuXuat p = new PhieuXuat();
        p.setMaPX(rs.getString("maPX"));
        p.setNgayXuat(date(rs, "ngayXuat"));
        NhanVien nv = new NhanVien();
        nv.setMaNV(rs.getString("maNV"));
        nv.setHoTen(rs.getString("hoTen"));
        p.setNhanVien(nv);
        p.setLyDo(rs.getString("lyDo"));
        return p;
    }

    public String taoMa() {
        return nextId("PhieuXuat", "maPX", "PX", 3);
    }

    public List<PhieuXuat> getAll() {
        return query("SELECT p.*, n.hoTen FROM PhieuXuat p JOIN NhanVien n ON p.maNV = n.maNV ORDER BY p.ngayXuat DESC, p.maPX DESC", PhieuXuatDAO::map);
    }

    public List<ChiTietPhieuXuat> layChiTiet(String maPX) {
        return query("SELECT ct.soLuong, nl.* FROM ChiTietPhieuXuat ct JOIN NguyenLieu nl ON ct.maNL = nl.maNL WHERE ct.maPX = ? ORDER BY nl.maNL",
                rs -> {
                    ChiTietPhieuXuat ct = new ChiTietPhieuXuat();
                    ct.setNguyenLieu(NguyenLieuDAO.map(rs));
                    ct.setSoLuong(rs.getDouble("soLuong"));
                    return ct;
                }, maPX);
    }

    /** Lưu phiếu xuất và trừ tồn kho; không cho xuất quá số tồn. */
    public void luu(PhieuXuat px) {
        if (px.getDsChiTiet().isEmpty()) throw new DaoException("Phiếu xuất chưa có nguyên liệu nào.");
        inTransaction(c -> {
            String ma = nextId(c, "PhieuXuat", "maPX", "PX", 3);
            exec(c, "INSERT INTO PhieuXuat(maPX, ngayXuat, maNV, lyDo) VALUES (?,?,?,?)",
                    ma, px.getNgayXuat(), px.getNhanVien().getMaNV(), px.getLyDo());
            for (ChiTietPhieuXuat ct : px.getDsChiTiet()) {
                exec(c, "INSERT INTO ChiTietPhieuXuat(maPX, maNL, soLuong) VALUES (?,?,?)", ma, ct.getNguyenLieu().getMaNL(), ct.getSoLuong());
                int n = exec(c, "UPDATE NguyenLieu SET soLuongTon = soLuongTon - ? WHERE maNL = ? AND soLuongTon >= ?",
                        ct.getSoLuong(), ct.getNguyenLieu().getMaNL(), ct.getSoLuong());
                if (n == 0) throw new DaoException("Không đủ tồn kho để xuất: " + ct.getNguyenLieu().getTenNL());
            }
            px.setMaPX(ma);
        });
    }
}
