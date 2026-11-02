package dao;

import java.sql.*;
import java.util.ArrayList;
import connectDB.ConnectDB;
import entity.ChiTietPhieuXuat;
import entity.NhanVien;
import entity.PhieuXuat;

public class PhieuXuat_DAO {
    // Mã phiếu kế tiếp (chỉ để hiển thị trước khi lưu)
    public String taoMa() {
        try {
            return TaoMa.tiepTheo(ConnectDB.getConnection(), "PhieuXuat", "maPX", "PX", 3);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public ArrayList<PhieuXuat> getAll() {
        ArrayList<PhieuXuat> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT p.*, n.hoTen FROM PhieuXuat p JOIN NhanVien n ON p.maNV = n.maNV ORDER BY p.ngayXuat DESC, p.maPX DESC");
            while (rs.next()) {
                PhieuXuat p = new PhieuXuat();
                p.setMaPX(rs.getString("maPX"));
                p.setNgayXuat(rs.getDate("ngayXuat").toLocalDate());
                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                nv.setHoTen(rs.getString("hoTen"));
                p.setNhanVien(nv);
                p.setLyDo(rs.getString("lyDo"));
                ds.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public ArrayList<ChiTietPhieuXuat> layChiTiet(String maPX) {
        ArrayList<ChiTietPhieuXuat> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT ct.soLuong, nl.* FROM ChiTietPhieuXuat ct "
                    + "JOIN NguyenLieu nl ON ct.maNL = nl.maNL WHERE ct.maPX = ? ORDER BY nl.maNL");
            stmt.setString(1, maPX);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ChiTietPhieuXuat ct = new ChiTietPhieuXuat();
                ct.setNguyenLieu(NguyenLieu_DAO.taoNguyenLieu(rs));
                ct.setSoLuong(rs.getDouble("soLuong"));
                ds.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    // Lưu phiếu xuất và trừ tồn kho; không cho xuất quá số tồn (thiếu tồn thì rollback, trả false)
    public boolean luu(PhieuXuat px) {
        Connection con = ConnectDB.getConnection();
        try {
            con.setAutoCommit(false);
            String ma = TaoMa.tiepTheo(con, "PhieuXuat", "maPX", "PX", 3);
            PreparedStatement stmt = con.prepareStatement("INSERT INTO PhieuXuat(maPX, ngayXuat, maNV, lyDo) VALUES (?,?,?,?)");
            stmt.setString(1, ma);
            stmt.setDate(2, Date.valueOf(px.getNgayXuat()));
            stmt.setString(3, px.getNhanVien().getMaNV());
            stmt.setString(4, px.getLyDo());
            stmt.executeUpdate();

            for (ChiTietPhieuXuat ct : px.getDsChiTiet()) {
                stmt = con.prepareStatement("INSERT INTO ChiTietPhieuXuat(maPX, maNL, soLuong) VALUES (?,?,?)");
                stmt.setString(1, ma);
                stmt.setString(2, ct.getNguyenLieu().getMaNL());
                stmt.setDouble(3, ct.getSoLuong());
                stmt.executeUpdate();

                stmt = con.prepareStatement("UPDATE NguyenLieu SET soLuongTon = soLuongTon - ? WHERE maNL = ? AND soLuongTon >= ?");
                stmt.setDouble(1, ct.getSoLuong());
                stmt.setString(2, ct.getNguyenLieu().getMaNL());
                stmt.setDouble(3, ct.getSoLuong());
                if (stmt.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
            }
            con.commit();
            px.setMaPX(ma);
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
