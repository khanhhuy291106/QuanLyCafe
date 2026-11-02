package gui;

import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import dao.HoaDon_DAO;
import dao.NguyenLieu_DAO;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.NguyenLieu;
import entity.NhanVien;

// Trang chủ: chỉ số trong ngày, đơn đang chờ và cảnh báo nguyên liệu sắp hết
public class TabTrangChu extends JPanel implements TaiLai {
    private HoaDon_DAO hd_dao = new HoaDon_DAO();
    private NguyenLieu_DAO nl_dao = new NguyenLieu_DAO();
    private NhanVien nv;
    private JLabel lblDoanhThu = new JLabel("-");
    private JLabel lblSoDon = new JLabel("-");
    private JLabel lblPhaChe = new JLabel("-");
    private JLabel lblSapHet = new JLabel("-");
    private DefaultTableModel mdlCho = TienIch.moHinh("STT", "Giờ", "Món", "Trạng thái");
    private DefaultTableModel mdlHet = TienIch.moHinh("Nguyên liệu", "Tồn", "Tối thiểu");

    public TabTrangChu(NhanVien nv, GiaoDienChinh gd) {
        this.nv = nv;
        setLayout(new BorderLayout(0, 14));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        setBackground(TienIch.BG);

        JPanel pnlTren = new JPanel(new GridBagLayout());
        pnlTren.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 10, 0);

        JLabel lblChao = new JLabel("Xin chào, " + nv.getHoTen() + "!");
        lblChao.setFont(TienIch.FONT_TITLE);
        g.gridy = 0;
        pnlTren.add(lblChao, g);

        // Quản lý xem doanh thu; nhân viên chỉ xem số đơn đã hoàn thành
        JPanel pnlThe = new JPanel(new GridLayout(1, 4, 12, 0));
        pnlThe.setOpaque(false);
        pnlThe.add(taoThe(nv.laQuanLy() ? "Doanh thu hôm nay" : "Đơn đã hoàn thành hôm nay", lblDoanhThu, TienIch.GREEN));
        pnlThe.add(taoThe("Số đơn hôm nay", lblSoDon, TienIch.BLUE));
        pnlThe.add(taoThe("Đơn đang pha chế", lblPhaChe, TienIch.ORANGE));
        pnlThe.add(taoThe("Nguyên liệu sắp hết", lblSapHet, TienIch.RED));
        g.gridy = 1;
        pnlTren.add(pnlThe, g);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlNut.setOpaque(false);
        if (!nv.laPhaChe()) {
            JButton btnTao = TienIch.nut("+ Tạo đơn mới", TienIch.GREEN);
            btnTao.addActionListener(e -> gd.hienThi("sell"));
            pnlNut.add(btnTao);
        }
        JButton btnCho = TienIch.nut("Xem đơn đang chờ", TienIch.BROWN);
        btnCho.addActionListener(e -> gd.hienThi("orders"));
        pnlNut.add(btnCho);
        if (nv.laQuanLy()) {
            JButton btnNhap = TienIch.nut("Lập phiếu nhập", TienIch.ACCENT);
            btnNhap.addActionListener(e -> gd.hienThi("stock:nhap"));
            pnlNut.add(btnNhap);
        }
        g.gridy = 2;
        g.insets = new Insets(2, 0, 0, 0);
        pnlTren.add(pnlNut, g);
        add(pnlTren, BorderLayout.NORTH);

        // Giữa: bảng đơn đang chờ + bảng nguyên liệu sắp hết
        JPanel pnlGiua = new JPanel(new GridBagLayout());
        pnlGiua.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridy = 0;
        c.gridx = 0;
        c.weightx = 0.62;
        c.insets = new Insets(0, 0, 0, 14);
        JTable tblCho = TienIch.bang(mdlCho);
        TienIch.canGiua(tblCho, 0, 1);
        TienIch.tomauTrangThai(tblCho, 3);
        TienIch.doRong(tblCho, 50, 60, 280, 120);
        pnlGiua.add(taoKhung("Đơn đang chờ", tblCho, TienIch.BROWN), c);
        c.gridx = 1;
        c.weightx = 0.38;
        c.insets = new Insets(0, 0, 0, 0);
        JTable tblHet = TienIch.bang(mdlHet);
        TienIch.canPhai(tblHet, 1, 2);
        TienIch.tomauCanhBao(tblHet, r -> true);
        pnlGiua.add(taoKhung("Nguyên liệu sắp hết", tblHet, TienIch.RED), c);
        add(pnlGiua, BorderLayout.CENTER);
    }

    // Thẻ chỉ số có vạch màu bên trái
    private JPanel taoThe(String tieuDe, JLabel lblGiaTri, Color mau) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        Border trong = BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 6, 0, 0, mau),
                BorderFactory.createEmptyBorder(10, 12, 10, 12));
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(TienIch.LINE), trong));
        p.add(TienIch.ghiChu(tieuDe), BorderLayout.NORTH);
        lblGiaTri.setFont(new Font(TienIch.FONT_NAME, Font.BOLD, 22));
        lblGiaTri.setForeground(mau);
        p.add(lblGiaTri, BorderLayout.CENTER);
        p.setPreferredSize(new Dimension(180, 76));
        return p;
    }

    private JPanel taoKhung(String tieuDe, JTable tbl, Color mau) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel lbl = TienIch.tieuDe(tieuDe);
        lbl.setForeground(mau);
        p.add(lbl, BorderLayout.NORTH);
        p.add(TienIch.cuon(tbl), BorderLayout.CENTER);
        return p;
    }

    @Override
    public void taiLai() {
        LocalDate hn = LocalDate.now();
        if (nv.laQuanLy()) lblDoanhThu.setText(TienIch.tienD(hd_dao.doanhThuNgay(hn)));
        else lblDoanhThu.setText(String.valueOf(hd_dao.demDon(hn, HoaDon.HOAN_THANH)));
        lblSoDon.setText(String.valueOf(hd_dao.demDon(hn, null)));
        lblPhaChe.setText(String.valueOf(hd_dao.demDon(hn, HoaDon.DANG_PHA_CHE)));

        ArrayList<NguyenLieu> dsHet = nl_dao.getSapHet();
        lblSapHet.setText(String.valueOf(dsHet.size()));
        mdlHet.setRowCount(0);
        for (NguyenLieu n : dsHet) {
            mdlHet.addRow(new Object[] { n.getTenNL(), TienIch.soThuc(n.getSoLuongTon()) + " " + n.getDonViTinh(),
                    TienIch.soThuc(n.getMucToiThieu()) + " " + n.getDonViTinh() });
        }

        mdlCho.setRowCount(0);
        int dem = 0;
        for (HoaDon h : hd_dao.donDangCho()) {
            String tomTat = "";
            if (dem++ < 12) { // chỉ tóm tắt món cho 12 đơn đầu để load nhanh
                ArrayList<ChiTietHoaDon> ds = hd_dao.layChiTiet(h.getMaHD());
                for (int i = 0; i < ds.size() && i < 3; i++) {
                    if (i > 0) tomTat += ", ";
                    tomTat += ds.get(i).getMon().getTenMon();
                    if (ds.get(i).getSoLuong() > 1) tomTat += " x" + ds.get(i).getSoLuong();
                }
                if (ds.size() > 3) tomTat += "…";
            }
            mdlCho.addRow(new Object[] { String.format("%03d", h.getSoThuTu()), TienIch.gio(h.getNgayLap()), tomTat,
                    HoaDon.tenTrangThai(h.getTrangThai()) });
        }
    }
}
