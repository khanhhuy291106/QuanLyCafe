package dao;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import connectDB.ConnectDB;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.KhuyenMai;
import entity.NhanVien;
import entity.Topping;

public class HoaDon_DAO {
    // vw_HoaDonTong: hóa đơn kèm tạm tính (đã cộng topping)
    private static final String SELECT = "SELECT v.*, n.hoTen, k.tenKM FROM vw_HoaDonTong v "
            + "JOIN NhanVien n ON v.maNV = n.maNV LEFT JOIN KhuyenMai k ON v.maKM = k.maKM ";

    private HoaDon taoHoaDon(ResultSet rs) throws SQLException {
        HoaDon h = new HoaDon();
        h.setMaHD(rs.getString("maHD"));
        h.setSoThuTu(rs.getInt("soThuTu"));
        h.setNgayLap(rs.getTimestamp("ngayLap").toLocalDateTime());
        h.setTrangThai(rs.getString("trangThai"));
        h.setPhuongThucTT(rs.getString("phuongThucTT"));
        h.setGiamGia(rs.getDouble("giamGia"));
        h.setTamTinh(rs.getDouble("tamTinh"));
        NhanVien nv = new NhanVien();
        nv.setMaNV(rs.getString("maNV"));
        nv.setHoTen(rs.getString("hoTen"));
        h.setNhanVien(nv);
        if (rs.getString("maKM") != null) {
            KhuyenMai km = new KhuyenMai();
            km.setMaKM(rs.getString("maKM"));
            km.setTenKM(rs.getString("tenKM"));
            h.setKhuyenMai(km);
        }
        return h;
    }

    // ------------------------------------------------------------ lưu hóa đơn
    // Số thứ tự đơn kế tiếp trong ngày (reset mỗi ngày)
    private int soThuTu(Connection con, LocalDate ngay) throws SQLException {
        PreparedStatement stmt = con.prepareStatement("SELECT ISNULL(MAX(soThuTu), 0) + 1 FROM HoaDon WHERE ngayLap >= ? AND ngayLap < ?");
        stmt.setTimestamp(1, Timestamp.valueOf(ngay.atStartOfDay()));
        stmt.setTimestamp(2, Timestamp.valueOf(ngay.plusDays(1).atStartOfDay()));
        ResultSet rs = stmt.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    // Số thứ tự của đơn kế tiếp hôm nay (để hiển thị trước khi lưu)
    public int soThuTuTiepTheo() {
        try {
            return soThuTu(ConnectDB.getConnection(), LocalDate.now());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    // Lưu hóa đơn + chi tiết + topping trong một transaction; điền lại mã, số thứ tự, giờ lập vào hd
    public boolean luuHoaDon(HoaDon hd) {
        Connection con = ConnectDB.getConnection();
        try {
            con.setAutoCommit(false);
            LocalDateTime now = LocalDateTime.now();
            String maHD = TaoMa.tiepTheo(con, "HoaDon", "maHD", "HD", 4);
            int stt = soThuTu(con, now.toLocalDate());

            PreparedStatement stmt = con.prepareStatement("INSERT INTO HoaDon(maHD, soThuTu, ngayLap, trangThai, phuongThucTT, giamGia, maNV, maKM) VALUES (?,?,?,?,?,?,?,?)");
            stmt.setString(1, maHD);
            stmt.setInt(2, stt);
            stmt.setTimestamp(3, Timestamp.valueOf(now));
            stmt.setString(4, HoaDon.MOI_TAO);
            stmt.setString(5, hd.getPhuongThucTT());
            stmt.setDouble(6, hd.getGiamGia());
            stmt.setString(7, hd.getNhanVien().getMaNV());
            if (hd.getKhuyenMai() == null) stmt.setNull(8, Types.VARCHAR);
            else stmt.setString(8, hd.getKhuyenMai().getMaKM());
            stmt.executeUpdate();

            for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
                stmt = con.prepareStatement("INSERT INTO ChiTietHoaDon(maHD, maMon, maSize, soLuong, donGia, mucDuong, mucDa, ghiChu) VALUES (?,?,?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, maHD);
                stmt.setString(2, ct.getMon().getMaMon());
                if (ct.getSize() == null) stmt.setNull(3, Types.VARCHAR);
                else stmt.setString(3, ct.getSize().getMaSize());
                stmt.setInt(4, ct.getSoLuong());
                stmt.setDouble(5, ct.getDonGia());
                stmt.setString(6, ct.getMucDuong());
                stmt.setString(7, ct.getMucDa());
                stmt.setString(8, ct.getGhiChu());
                stmt.executeUpdate();
                ResultSet keys = stmt.getGeneratedKeys();
                keys.next();
                int maCT = keys.getInt(1);

                for (Topping t : ct.getDsTopping()) {
                    stmt = con.prepareStatement("INSERT INTO ChiTietHoaDon_Topping(maCT, maTopping, gia) VALUES (?,?,?)");
                    stmt.setInt(1, maCT);
                    stmt.setString(2, t.getMaTopping());
                    stmt.setDouble(3, t.getGia());
                    stmt.executeUpdate();
                }
            }
            con.commit();
            hd.setMaHD(maHD);
            hd.setSoThuTu(stt);
            hd.setNgayLap(now);
            hd.setTrangThai(HoaDon.MOI_TAO);
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

    // ------------------------------------------------------------ đọc
    // trangThai = null là tất cả. keyword khớp mã hóa đơn hoặc số thứ tự.
    public ArrayList<HoaDon> timKiem(LocalDate tu, LocalDate den, String trangThai, String keyword) {
        ArrayList<HoaDon> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = SELECT + "WHERE v.ngayLap >= ? AND v.ngayLap < ?";
            if (trangThai != null) sql += " AND v.trangThai = ?";
            String kw = keyword.trim();
            String so = kw.replaceFirst("^#", "");
            boolean laSo = so.matches("\\d{1,6}");
            if (!kw.isEmpty() && laSo) sql += " AND (v.maHD LIKE ? OR v.soThuTu = ?)";
            else if (!kw.isEmpty()) sql += " AND v.maHD LIKE ?";
            sql += " ORDER BY v.ngayLap DESC";

            PreparedStatement stmt = con.prepareStatement(sql);
            int i = 1;
            stmt.setTimestamp(i++, Timestamp.valueOf(tu.atStartOfDay()));
            stmt.setTimestamp(i++, Timestamp.valueOf(den.plusDays(1).atStartOfDay()));
            if (trangThai != null) stmt.setString(i++, trangThai);
            if (!kw.isEmpty()) stmt.setString(i++, "%" + kw + "%");
            if (!kw.isEmpty() && laSo) stmt.setInt(i++, Integer.parseInt(so));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ds.add(taoHoaDon(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    // Lấy một hóa đơn kèm chi tiết các món
    public HoaDon layHoaDon(String maHD) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(SELECT + "WHERE v.maHD = ?");
            stmt.setString(1, maHD);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                HoaDon h = taoHoaDon(rs);
                h.setDsChiTiet(layChiTiet(maHD));
                return h;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<ChiTietHoaDon> layChiTiet(String maHD) {
        // Giữ thứ tự dòng theo maCT để gắn topping vào đúng dòng
        Map<Integer, ChiTietHoaDon> map = new LinkedHashMap<>();
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT ct.maCT, ct.soLuong, ct.donGia, ct.mucDuong, ct.mucDa, ct.ghiChu, "
                    + "m.maMon, m.tenMon, m.giaCoBan, m.hinhAnh, m.trangThai, l.maLoai, l.tenLoai, s.maSize, s.tenSize, s.phuThu "
                    + "FROM ChiTietHoaDon ct JOIN Mon m ON ct.maMon = m.maMon JOIN LoaiMon l ON m.maLoai = l.maLoai "
                    + "LEFT JOIN [Size] s ON ct.maSize = s.maSize WHERE ct.maHD = ? ORDER BY ct.maCT");
            stmt.setString(1, maHD);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ChiTietHoaDon ct = new ChiTietHoaDon();
                ct.setMon(Mon_DAO.taoMon(rs));
                if (rs.getString("maSize") != null) ct.setSize(Size_DAO.taoSize(rs));
                ct.setSoLuong(rs.getInt("soLuong"));
                ct.setDonGia(rs.getDouble("donGia"));
                ct.setMucDuong(rs.getString("mucDuong"));
                ct.setMucDa(rs.getString("mucDa"));
                ct.setGhiChu(rs.getString("ghiChu"));
                map.put(rs.getInt("maCT"), ct);
            }

            stmt = con.prepareStatement("SELECT t.maCT, t.maTopping, t.gia, tp.tenTopping FROM ChiTietHoaDon_Topping t "
                    + "JOIN Topping tp ON t.maTopping = tp.maTopping "
                    + "WHERE t.maCT IN (SELECT maCT FROM ChiTietHoaDon WHERE maHD = ?)");
            stmt.setString(1, maHD);
            rs = stmt.executeQuery();
            while (rs.next()) {
                ChiTietHoaDon ct = map.get(rs.getInt("maCT"));
                if (ct != null) {
                    ct.getDsTopping().add(new Topping(rs.getString("maTopping"), rs.getString("tenTopping"), rs.getDouble("gia"), true));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(map.values());
    }

    // ------------------------------------------------------------ cập nhật trạng thái
    public boolean capNhatTrangThai(String maHD, String trangThai) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("UPDATE HoaDon SET trangThai = ? WHERE maHD = ?");
            stmt.setString(1, trangThai);
            stmt.setString(2, maHD);
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    // ------------------------------------------------------------ trang chủ
    // Doanh thu một ngày: các đơn chưa hủy (khách trả tiền ngay khi tạo đơn)
    public double doanhThuNgay(LocalDate d) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT ISNULL(SUM(tamTinh - giamGia), 0) FROM vw_HoaDonTong "
                    + "WHERE ngayLap >= ? AND ngayLap < ? AND trangThai <> 'DA_HUY'");
            stmt.setTimestamp(1, Timestamp.valueOf(d.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(d.plusDays(1).atStartOfDay()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Đếm đơn trong ngày. trangThai = null là mọi đơn chưa hủy.
    public int demDon(LocalDate d, String trangThai) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT COUNT(*) FROM HoaDon WHERE ngayLap >= ? AND ngayLap < ? AND ";
            sql += (trangThai == null) ? "trangThai <> 'DA_HUY'" : "trangThai = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setTimestamp(1, Timestamp.valueOf(d.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(d.plusDays(1).atStartOfDay()));
            if (trangThai != null) stmt.setString(3, trangThai);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Các đơn hôm nay chưa hoàn thành, cũ nhất lên đầu
    public ArrayList<HoaDon> donDangCho() {
        ArrayList<HoaDon> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            LocalDate d = LocalDate.now();
            PreparedStatement stmt = con.prepareStatement(SELECT + "WHERE v.ngayLap >= ? AND v.ngayLap < ? "
                    + "AND v.trangThai IN ('MOI_TAO', 'DANG_PHA_CHE') ORDER BY v.ngayLap");
            stmt.setTimestamp(1, Timestamp.valueOf(d.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(d.plusDays(1).atStartOfDay()));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ds.add(taoHoaDon(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    // ------------------------------------------------------------ thống kê (bỏ qua đơn đã hủy)
    // Mỗi dòng: {LocalDate ngày, Integer số đơn, Double doanh thu}
    public ArrayList<Object[]> thongKeTheoNgay(LocalDate tu, LocalDate den) {
        ArrayList<Object[]> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT CAST(ngayLap AS DATE) AS ngay, COUNT(*) AS soDon, SUM(tamTinh - giamGia) AS doanhThu "
                    + "FROM vw_HoaDonTong WHERE trangThai <> 'DA_HUY' AND ngayLap >= ? AND ngayLap < ? "
                    + "GROUP BY CAST(ngayLap AS DATE) ORDER BY ngay");
            stmt.setTimestamp(1, Timestamp.valueOf(tu.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(den.plusDays(1).atStartOfDay()));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ds.add(new Object[] { rs.getDate("ngay").toLocalDate(), rs.getInt("soDon"), rs.getDouble("doanhThu") });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    // Mỗi dòng: {String "MM/yyyy", Integer số đơn, Double doanh thu}
    public ArrayList<Object[]> thongKeTheoThang(LocalDate tu, LocalDate den) {
        ArrayList<Object[]> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT YEAR(ngayLap) AS y, MONTH(ngayLap) AS m, COUNT(*) AS soDon, SUM(tamTinh - giamGia) AS doanhThu "
                    + "FROM vw_HoaDonTong WHERE trangThai <> 'DA_HUY' AND ngayLap >= ? AND ngayLap < ? "
                    + "GROUP BY YEAR(ngayLap), MONTH(ngayLap) ORDER BY y, m");
            stmt.setTimestamp(1, Timestamp.valueOf(tu.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(den.plusDays(1).atStartOfDay()));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String thang = String.format("%02d/%d", rs.getInt("m"), rs.getInt("y"));
                ds.add(new Object[] { thang, rs.getInt("soDon"), rs.getDouble("doanhThu") });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    // Mỗi dòng: {String tên món, Integer số lượng bán}
    public ArrayList<Object[]> topMon(LocalDate tu, LocalDate den, int gioiHan) {
        ArrayList<Object[]> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT TOP (?) m.tenMon, SUM(ct.soLuong) AS sl FROM ChiTietHoaDon ct "
                    + "JOIN HoaDon h ON ct.maHD = h.maHD JOIN Mon m ON ct.maMon = m.maMon "
                    + "WHERE h.trangThai <> 'DA_HUY' AND h.ngayLap >= ? AND h.ngayLap < ? "
                    + "GROUP BY m.maMon, m.tenMon ORDER BY sl DESC");
            stmt.setInt(1, gioiHan);
            stmt.setTimestamp(2, Timestamp.valueOf(tu.atStartOfDay()));
            stmt.setTimestamp(3, Timestamp.valueOf(den.plusDays(1).atStartOfDay()));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ds.add(new Object[] { rs.getString("tenMon"), rs.getInt("sl") });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }
}
