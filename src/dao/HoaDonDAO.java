package dao;

import entity.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class HoaDonDAO extends BaseDAO {

    private static final String SEL = "SELECT v.*, n.hoTen, k.tenKM FROM vw_HoaDonTong v "
            + "JOIN NhanVien n ON v.maNV = n.maNV LEFT JOIN KhuyenMai k ON v.maKM = k.maKM ";

    private static HoaDon mapHeader(ResultSet rs) throws SQLException {
        HoaDon h = new HoaDon();
        h.setMaHD(rs.getString("maHD"));
        h.setSoThuTu(rs.getInt("soThuTu"));
        h.setNgayLap(rs.getTimestamp("ngayLap").toLocalDateTime());
        h.setTrangThai(rs.getString("trangThai"));
        h.setPhuongThucTT(rs.getString("phuongThucTT"));
        h.setGiamGia(rs.getDouble("giamGia"));
        NhanVien nv = new NhanVien();
        nv.setMaNV(rs.getString("maNV"));
        nv.setHoTen(rs.getString("hoTen"));
        h.setNhanVien(nv);
        String maKM = rs.getString("maKM");
        if (maKM != null) {
            KhuyenMai km = new KhuyenMai();
            km.setMaKM(maKM);
            km.setTenKM(rs.getString("tenKM"));
            h.setKhuyenMai(km);
        }
        h.setTamTinhDB(rs.getDouble("tamTinh"));
        return h;
    }

    // ------------------------------------------------------------ lưu hóa đơn
    /** Lưu hóa đơn + chi tiết + topping trong một transaction; điền lại mã, số thứ tự, giờ lập vào hd. */
    public void luuHoaDon(HoaDon hd) {
        if (hd.getDsChiTiet().isEmpty()) throw new DaoException("Hóa đơn chưa có món nào.");
        inTransaction(c -> {
            LocalDateTime now = LocalDateTime.now();
            String maHD = nextId(c, "HoaDon", "maHD", "HD", 4);
            int stt = soThuTu(c, now.toLocalDate());
            exec(c, "INSERT INTO HoaDon(maHD, soThuTu, ngayLap, trangThai, phuongThucTT, giamGia, maNV, maKM) VALUES (?,?,?,?,?,?,?,?)",
                    maHD, stt, now, HoaDon.MOI_TAO, hd.getPhuongThucTT(), hd.getGiamGia(), hd.getNhanVien().getMaNV(),
                    hd.getKhuyenMai() == null ? null : hd.getKhuyenMai().getMaKM());
            String sqlCT = "INSERT INTO ChiTietHoaDon(maHD, maMon, maSize, soLuong, donGia, mucDuong, mucDa, ghiChu) VALUES (?,?,?,?,?,?,?,?)";
            for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
                int maCT;
                try (PreparedStatement ps = c.prepareStatement(sqlCT, Statement.RETURN_GENERATED_KEYS)) {
                    setParams(ps, maHD, ct.getMon().getMaMon(), ct.getSize() == null ? null : ct.getSize().getMaSize(),
                            ct.getSoLuong(), ct.getDonGia(), ct.getMucDuong(), ct.getMucDa(), ct.getGhiChu());
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) {
                        k.next();
                        maCT = k.getInt(1);
                    }
                }
                for (Topping t : ct.getDsTopping())
                    exec(c, "INSERT INTO ChiTietHoaDon_Topping(maCT, maTopping, gia) VALUES (?,?,?)", maCT, t.getMaTopping(), t.getGia());
            }
            hd.setMaHD(maHD);
            hd.setSoThuTu(stt);
            hd.setNgayLap(now);
            hd.setTrangThai(HoaDon.MOI_TAO);
        });
    }

    private static int soThuTu(Connection c, LocalDate ngay) throws SQLException {
        List<Integer> l = query(c, "SELECT ISNULL(MAX(soThuTu), 0) + 1 AS n FROM HoaDon WHERE ngayLap >= ? AND ngayLap < ?",
                rs -> rs.getInt("n"), ngay.atStartOfDay(), ngay.plusDays(1).atStartOfDay());
        return l.get(0);
    }

    /** Số thứ tự của đơn kế tiếp trong ngày hôm nay (để hiển thị trước khi lưu). */
    public int soThuTuTiepTheo() {
        try {
            return soThuTu(con(), LocalDate.now());
        } catch (SQLException e) {
            throw DaoException.from(e);
        }
    }

    // ------------------------------------------------------------ đọc
    /** trangThai = null là tất cả. keyword khớp mã hóa đơn hoặc số thứ tự. */
    public List<HoaDon> timKiem(LocalDate tu, LocalDate den, String trangThai, String keyword) {
        StringBuilder sql = new StringBuilder(SEL + "WHERE v.ngayLap >= ? AND v.ngayLap < ?");
        List<Object> p = new ArrayList<>(Arrays.asList(tu.atStartOfDay(), den.plusDays(1).atStartOfDay()));
        if (trangThai != null) {
            sql.append(" AND v.trangThai = ?");
            p.add(trangThai);
        }
        String kw = keyword == null ? "" : keyword.trim();
        if (!kw.isEmpty()) {
            String so = kw.replaceFirst("^#", "");
            if (so.matches("\\d{1,6}")) {
                sql.append(" AND (v.maHD LIKE ? OR v.soThuTu = ?)");
                p.add("%" + kw + "%");
                p.add(Integer.parseInt(so));
            } else {
                sql.append(" AND v.maHD LIKE ?");
                p.add("%" + kw + "%");
            }
        }
        sql.append(" ORDER BY v.ngayLap DESC");
        return query(sql.toString(), HoaDonDAO::mapHeader, p.toArray());
    }

    public HoaDon layHoaDon(String maHD) {
        HoaDon h = queryOne(SEL + "WHERE v.maHD = ?", HoaDonDAO::mapHeader, maHD);
        if (h != null) {
            h.setDsChiTiet(layChiTiet(maHD));
            for (ChiTietHoaDon ct : h.getDsChiTiet()) ct.setHoaDon(h);
        }
        return h;
    }

    public List<ChiTietHoaDon> layChiTiet(String maHD) {
        String sql = "SELECT ct.maCT, ct.soLuong, ct.donGia, ct.mucDuong, ct.mucDa, ct.ghiChu, "
                + "m.maMon, m.tenMon, m.giaCoBan, m.hinhAnh, m.trangThai, l.maLoai, l.tenLoai, s.maSize, s.tenSize, s.phuThu "
                + "FROM ChiTietHoaDon ct JOIN Mon m ON ct.maMon = m.maMon JOIN LoaiMon l ON m.maLoai = l.maLoai "
                + "LEFT JOIN [Size] s ON ct.maSize = s.maSize WHERE ct.maHD = ? ORDER BY ct.maCT";
        Map<Integer, ChiTietHoaDon> map = new LinkedHashMap<>();
        try {
            Connection c = con();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, maHD);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ChiTietHoaDon ct = new ChiTietHoaDon();
                        ct.setMon(MonDAO.map(rs));
                        if (rs.getString("maSize") != null) ct.setSize(SizeDAO.map(rs));
                        ct.setSoLuong(rs.getInt("soLuong"));
                        ct.setDonGia(rs.getDouble("donGia"));
                        ct.setMucDuong(rs.getString("mucDuong"));
                        ct.setMucDa(rs.getString("mucDa"));
                        ct.setGhiChu(rs.getString("ghiChu"));
                        map.put(rs.getInt("maCT"), ct);
                    }
                }
            }
            String sqlTp = "SELECT t.maCT, t.maTopping, t.gia, tp.tenTopping FROM ChiTietHoaDon_Topping t "
                    + "JOIN Topping tp ON t.maTopping = tp.maTopping "
                    + "WHERE t.maCT IN (SELECT maCT FROM ChiTietHoaDon WHERE maHD = ?)";
            try (PreparedStatement ps = c.prepareStatement(sqlTp)) {
                ps.setString(1, maHD);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ChiTietHoaDon ct = map.get(rs.getInt("maCT"));
                        if (ct != null)
                            ct.getDsTopping().add(new Topping(rs.getString("maTopping"), rs.getString("tenTopping"), rs.getDouble("gia"), true));
                    }
                }
            }
        } catch (SQLException e) {
            throw DaoException.from(e);
        }
        return new ArrayList<>(map.values());
    }

    // ------------------------------------------------------------ cập nhật trạng thái
    public void capNhatTrangThai(String maHD, String trangThai) {
        update("UPDATE HoaDon SET trangThai = ? WHERE maHD = ?", trangThai, maHD);
    }

    // ------------------------------------------------------------ trang chủ
    /** Doanh thu một ngày: các đơn chưa hủy (khách trả tiền ngay khi tạo đơn). */
    public double doanhThuNgay(LocalDate d) {
        Double v = queryOne("SELECT ISNULL(SUM(tamTinh - giamGia), 0) AS v FROM vw_HoaDonTong WHERE ngayLap >= ? AND ngayLap < ? AND trangThai <> 'DA_HUY'",
                rs -> rs.getDouble("v"), d.atStartOfDay(), d.plusDays(1).atStartOfDay());
        return v == null ? 0 : v;
    }

    /** Đếm đơn trong ngày. trangThai = null là mọi đơn chưa hủy. */
    public int demDon(LocalDate d, String trangThai) {
        String sql = "SELECT COUNT(*) AS n FROM HoaDon WHERE ngayLap >= ? AND ngayLap < ? AND "
                + (trangThai == null ? "trangThai <> 'DA_HUY'" : "trangThai = ?");
        Integer v = trangThai == null
                ? queryOne(sql, rs -> rs.getInt("n"), d.atStartOfDay(), d.plusDays(1).atStartOfDay())
                : queryOne(sql, rs -> rs.getInt("n"), d.atStartOfDay(), d.plusDays(1).atStartOfDay(), trangThai);
        return v == null ? 0 : v;
    }

    /** Các đơn hôm nay chưa hoàn thành, cũ nhất lên đầu. */
    public List<HoaDon> donDangCho() {
        LocalDate d = LocalDate.now();
        return query(SEL + "WHERE v.ngayLap >= ? AND v.ngayLap < ? AND v.trangThai IN ('MOI_TAO', 'DANG_PHA_CHE') ORDER BY v.ngayLap",
                HoaDonDAO::mapHeader, d.atStartOfDay(), d.plusDays(1).atStartOfDay());
    }

    // ------------------------------------------------------------ thống kê
    /** Mỗi dòng: {LocalDate ngày, Integer số đơn, Double doanh thu}. Bỏ qua đơn đã hủy. */
    public List<Object[]> thongKeTheoNgay(LocalDate tu, LocalDate den) {
        return query("SELECT CAST(ngayLap AS DATE) AS ngay, COUNT(*) AS soDon, SUM(tamTinh - giamGia) AS doanhThu "
                        + "FROM vw_HoaDonTong WHERE trangThai <> 'DA_HUY' AND ngayLap >= ? AND ngayLap < ? "
                        + "GROUP BY CAST(ngayLap AS DATE) ORDER BY ngay",
                rs -> new Object[]{date(rs, "ngay"), rs.getInt("soDon"), rs.getDouble("doanhThu")},
                tu.atStartOfDay(), den.plusDays(1).atStartOfDay());
    }

    /** Mỗi dòng: {String "MM/yyyy", Integer số đơn, Double doanh thu}. */
    public List<Object[]> thongKeTheoThang(LocalDate tu, LocalDate den) {
        return query("SELECT YEAR(ngayLap) AS y, MONTH(ngayLap) AS m, COUNT(*) AS soDon, SUM(tamTinh - giamGia) AS doanhThu "
                        + "FROM vw_HoaDonTong WHERE trangThai <> 'DA_HUY' AND ngayLap >= ? AND ngayLap < ? "
                        + "GROUP BY YEAR(ngayLap), MONTH(ngayLap) ORDER BY y, m",
                rs -> new Object[]{String.format("%02d/%d", rs.getInt("m"), rs.getInt("y")), rs.getInt("soDon"), rs.getDouble("doanhThu")},
                tu.atStartOfDay(), den.plusDays(1).atStartOfDay());
    }

    /** Mỗi dòng: {String tên món, Integer số lượng bán}. */
    public List<Object[]> topMon(LocalDate tu, LocalDate den, int gioiHan) {
        return query("SELECT TOP (?) m.tenMon, SUM(ct.soLuong) AS sl FROM ChiTietHoaDon ct "
                        + "JOIN HoaDon h ON ct.maHD = h.maHD JOIN Mon m ON ct.maMon = m.maMon "
                        + "WHERE h.trangThai <> 'DA_HUY' AND h.ngayLap >= ? AND h.ngayLap < ? "
                        + "GROUP BY m.maMon, m.tenMon ORDER BY sl DESC",
                rs -> new Object[]{rs.getString("tenMon"), rs.getInt("sl")},
                gioiHan, tu.atStartOfDay(), den.plusDays(1).atStartOfDay());
    }
}
