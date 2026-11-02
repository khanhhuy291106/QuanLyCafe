package dao;

import java.sql.*;
import java.util.ArrayList;
import connectDB.ConnectDB;
import entity.ChiTietPhieuNhap;
import entity.NhaCungCap;
import entity.NhanVien;
import entity.PhieuNhap;

public class PhieuNhap_DAO {
    // Mã phiếu kế tiếp (chỉ để hiển thị trước khi lưu)
    public String taoMa() {
        try {
            return TaoMa.tiepTheo(ConnectDB.getConnection(), "PhieuNhap", "maPN", "PN", 3);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public ArrayList<PhieuNhap> getAll() {
        ArrayList<PhieuNhap> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT p.*, n.hoTen, c.tenNCC FROM PhieuNhap p JOIN NhanVien n ON p.maNV = n.maNV "
                    + "JOIN NhaCungCap c ON p.maNCC = c.maNCC ORDER BY p.ngayNhap DESC, p.maPN DESC");
            while (rs.next()) {
                PhieuNhap p = new PhieuNhap();
                p.setMaPN(rs.getString("maPN"));
                p.setNgayNhap(rs.getDate("ngayNhap").toLocalDate());
                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                nv.setHoTen(rs.getString("hoTen"));
                p.setNhanVien(nv);
                NhaCungCap ncc = new NhaCungCap();
                ncc.setMaNCC(rs.getString("maNCC"));
                ncc.setTenNCC(rs.getString("tenNCC"));
                p.setNhaCungCap(ncc);
                p.setTongTien(rs.getDouble("tongTien"));
                ds.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public ArrayList<ChiTietPhieuNhap> layChiTiet(String maPN) {
        ArrayList<ChiTietPhieuNhap> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT ct.soLuong, ct.donGia, nl.* FROM ChiTietPhieuNhap ct "
                    + "JOIN NguyenLieu nl ON ct.maNL = nl.maNL WHERE ct.maPN = ? ORDER BY nl.maNL");
            stmt.setString(1, maPN);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
                ct.setNguyenLieu(NguyenLieu_DAO.taoNguyenLieu(rs));
                ct.setSoLuong(rs.getDouble("soLuong"));
                ct.setDonGia(rs.getDouble("donGia"));
                ds.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    // Lưu phiếu nhập và cộng tồn kho trong cùng một transaction
    public boolean luu(PhieuNhap pn) {
        Connection con = ConnectDB.getConnection();
        try {
            con.setAutoCommit(false);
            String ma = TaoMa.tiepTheo(con, "PhieuNhap", "maPN", "PN", 3);
            double tong = pn.tinhTong();
            PreparedStatement stmt = con.prepareStatement("INSERT INTO PhieuNhap(maPN, ngayNhap, maNV, maNCC, tongTien) VALUES (?,?,?,?,?)");
            stmt.setString(1, ma);
            stmt.setDate(2, Date.valueOf(pn.getNgayNhap()));
            stmt.setString(3, pn.getNhanVien().getMaNV());
            stmt.setString(4, pn.getNhaCungCap().getMaNCC());
            stmt.setDouble(5, tong);
            stmt.executeUpdate();

            for (ChiTietPhieuNhap ct : pn.getDsChiTiet()) {
                stmt = con.prepareStatement("INSERT INTO ChiTietPhieuNhap(maPN, maNL, soLuong, donGia) VALUES (?,?,?,?)");
                stmt.setString(1, ma);
                stmt.setString(2, ct.getNguyenLieu().getMaNL());
                stmt.setDouble(3, ct.getSoLuong());
                stmt.setDouble(4, ct.getDonGia());
                stmt.executeUpdate();

                stmt = con.prepareStatement("UPDATE NguyenLieu SET soLuongTon = soLuongTon + ? WHERE maNL = ?");
                stmt.setDouble(1, ct.getSoLuong());
                stmt.setString(2, ct.getNguyenLieu().getMaNL());
                stmt.executeUpdate();
            }
            con.commit();
            pn.setMaPN(ma);
            pn.setTongTien(tong);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
