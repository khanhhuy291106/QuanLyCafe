package dao;

import entity.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PhieuNhapDAO extends BaseDAO {

    private static PhieuNhap map(ResultSet rs) throws SQLException {
        PhieuNhap p = new PhieuNhap();
        p.setMaPN(rs.getString("maPN"));
        p.setNgayNhap(date(rs, "ngayNhap"));
        NhanVien nv = new NhanVien();
        nv.setMaNV(rs.getString("maNV"));
        nv.setHoTen(rs.getString("hoTen"));
        p.setNhanVien(nv);
        NhaCungCap n = new NhaCungCap();
        n.setMaNCC(rs.getString("maNCC"));
        n.setTenNCC(rs.getString("tenNCC"));
        p.setNhaCungCap(n);
        p.setTongTien(rs.getDouble("tongTien"));
        return p;
    }

    public String taoMa() {
        return nextId("PhieuNhap", "maPN", "PN", 3);
    }

    public List<PhieuNhap> getAll() {
        return query("SELECT p.*, n.hoTen, c.tenNCC FROM PhieuNhap p JOIN NhanVien n ON p.maNV = n.maNV "
                + "JOIN NhaCungCap c ON p.maNCC = c.maNCC ORDER BY p.ngayNhap DESC, p.maPN DESC", PhieuNhapDAO::map);
    }

    public List<ChiTietPhieuNhap> layChiTiet(String maPN) {
        return query("SELECT ct.soLuong, ct.donGia, nl.* FROM ChiTietPhieuNhap ct JOIN NguyenLieu nl ON ct.maNL = nl.maNL "
                        + "WHERE ct.maPN = ? ORDER BY nl.maNL",
                rs -> {
                    ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
                    ct.setNguyenLieu(NguyenLieuDAO.map(rs));
                    ct.setSoLuong(rs.getDouble("soLuong"));
                    ct.setDonGia(rs.getDouble("donGia"));
                    return ct;
                }, maPN);
    }

    /** Lưu phiếu nhập và cộng tồn kho trong cùng một transaction. */
    public void luu(PhieuNhap pn) {
        if (pn.getDsChiTiet().isEmpty()) throw new DaoException("Phiếu nhập chưa có nguyên liệu nào.");
        inTransaction(c -> {
            String ma = nextId(c, "PhieuNhap", "maPN", "PN", 3);
            double tong = pn.tinhTong();
            exec(c, "INSERT INTO PhieuNhap(maPN, ngayNhap, maNV, maNCC, tongTien) VALUES (?,?,?,?,?)",
                    ma, pn.getNgayNhap(), pn.getNhanVien().getMaNV(), pn.getNhaCungCap().getMaNCC(), tong);
            for (ChiTietPhieuNhap ct : pn.getDsChiTiet()) {
                exec(c, "INSERT INTO ChiTietPhieuNhap(maPN, maNL, soLuong, donGia) VALUES (?,?,?,?)",
                        ma, ct.getNguyenLieu().getMaNL(), ct.getSoLuong(), ct.getDonGia());
                exec(c, "UPDATE NguyenLieu SET soLuongTon = soLuongTon + ? WHERE maNL = ?", ct.getSoLuong(), ct.getNguyenLieu().getMaNL());
            }
            pn.setMaPN(ma);
            pn.setTongTien(tong);
        });
    }
}
