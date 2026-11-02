package gui;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.jdatepicker.impl.JDatePickerImpl;
import dao.HoaDon_DAO;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.NhanVien;

// Quản lý đơn: tra cứu đơn, xem chi tiết, đổi trạng thái.
// Pha chế chỉ được đổi trạng thái; hủy đơn và in lại hóa đơn dành cho Thu ngân và Quản lý.
public class TabQuanLyDon extends JPanel implements TaiLai {
    private HoaDon_DAO dao = new HoaDon_DAO();
    private NhanVien nv;
    private ArrayList<HoaDon> ds = new ArrayList<>();
    private HoaDon dangXem = null;

    private JDatePickerImpl dpTu, dpDen;
    private JComboBox<String> cboTT;
    private JTextField txtTim;
    private DefaultTableModel mdl, mdlCT;
    private JTable tbl, tblCT;
    private JLabel lblDem, lblTieuDe, lblThongTin, lblTrangThai, lblTamTinh, lblGiam, lblTong;
    private JButton btnPhaChe, btnHoanThanh, btnHuy, btnIn;

    public TabQuanLyDon(NhanVien nv) {
        this.nv = nv;
        setLayout(new BorderLayout(12, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(TienIch.BG);
        add(taoBenTrai(), BorderLayout.CENTER);
        add(taoBenPhai(), BorderLayout.EAST);
        hienChiTiet();
    }

    private JPanel taoBenTrai() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        JPanel pnlLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pnlLoc.setOpaque(false);
        dpTu = DinhDangNgay.taoDatePicker(LocalDate.now());
        dpDen = DinhDangNgay.taoDatePicker(LocalDate.now());
        cboTT = new JComboBox<>(new String[] { "Tất cả", "Mới tạo", "Đang pha chế", "Hoàn thành", "Đã hủy" });
        txtTim = new JTextField(12);
        txtTim.setToolTipText("Mã hóa đơn hoặc số thứ tự");
        JButton btnTim = TienIch.nut("Tìm", TienIch.BROWN);
        JButton btnHomNay = TienIch.nutPhu("Hôm nay");
        pnlLoc.add(new JLabel("Từ ngày"));
        pnlLoc.add(dpTu);
        pnlLoc.add(new JLabel("Đến"));
        pnlLoc.add(dpDen);
        pnlLoc.add(new JLabel("Trạng thái"));
        pnlLoc.add(cboTT);
        pnlLoc.add(txtTim);
        pnlLoc.add(btnTim);
        pnlLoc.add(btnHomNay);
        p.add(pnlLoc, BorderLayout.NORTH);

        mdl = TienIch.moHinh("Mã HĐ", "STT", "Giờ", "Tổng tiền", "Thanh toán", "Trạng thái");
        tbl = TienIch.bang(mdl);
        TienIch.canGiua(tbl, 1, 2);
        TienIch.canPhai(tbl, 3);
        TienIch.tomauTrangThai(tbl, 5);
        TienIch.doRong(tbl, 80, 50, 70, 100, 110, 130);
        tbl.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) chonDong();
        });
        p.add(TienIch.cuon(tbl), BorderLayout.CENTER);
        lblDem = TienIch.ghiChu(" ");
        p.add(lblDem, BorderLayout.SOUTH);

        btnTim.addActionListener(e -> taiLai());
        txtTim.addActionListener(e -> taiLai());
        btnHomNay.addActionListener(e -> {
            DinhDangNgay.datNgay(dpTu, LocalDate.now());
            DinhDangNgay.datNgay(dpDen, LocalDate.now());
            cboTT.setSelectedIndex(0);
            txtTim.setText("");
            taiLai();
        });
        return p;
    }

    private JPanel taoBenPhai() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(TienIch.LINE),
                BorderFactory.createEmptyBorder(12, 14, 14, 14)));
        card.setPreferredSize(new Dimension(380, 100));

        JPanel pnlDau = new JPanel(new GridLayout(0, 1, 0, 4));
        pnlDau.setOpaque(false);
        lblTieuDe = TienIch.tieuDe("Chi tiết đơn");
        lblThongTin = new JLabel(" ");
        lblTrangThai = new JLabel();
        lblTrangThai.setOpaque(true);
        lblTrangThai.setForeground(Color.WHITE);
        lblTrangThai.setFont(TienIch.FONT_B);
        lblTrangThai.setBorder(BorderFactory.createEmptyBorder(3, 12, 3, 12));
        JPanel pnlTT = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTT.setOpaque(false);
        pnlTT.add(lblTrangThai);
        pnlDau.add(lblTieuDe);
        pnlDau.add(lblThongTin);
        pnlDau.add(pnlTT);
        card.add(pnlDau, BorderLayout.NORTH);

        mdlCT = TienIch.moHinh("Món", "SL", "Thành tiền");
        tblCT = TienIch.bang(mdlCT);
        tblCT.setRowHeight(62);
        TienIch.canGiua(tblCT, 1);
        TienIch.canPhai(tblCT, 2);
        TienIch.doRong(tblCT, 220, 36, 80);
        JPanel pnlGiua = new JPanel(new BorderLayout(0, 8));
        pnlGiua.setOpaque(false);
        pnlGiua.add(TienIch.cuon(tblCT), BorderLayout.CENTER);

        lblTamTinh = new JLabel("0đ");
        lblGiam = new JLabel("0đ");
        lblGiam.setForeground(TienIch.RED);
        lblTong = new JLabel("0đ");
        lblTong.setFont(TienIch.FONT_TITLE);
        lblTong.setForeground(TienIch.GREEN);
        JPanel pnlTong = new JPanel(new GridLayout(0, 1, 0, 3));
        pnlTong.setOpaque(false);
        pnlTong.add(taoDong("Tạm tính", lblTamTinh));
        pnlTong.add(taoDong("Giảm giá", lblGiam));
        JPanel pnlCong = new JPanel(new BorderLayout());
        pnlCong.setOpaque(false);
        pnlCong.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, TienIch.LINE));
        JLabel lblCong = new JLabel("TỔNG CỘNG");
        lblCong.setFont(TienIch.FONT_B);
        pnlCong.add(lblCong, BorderLayout.WEST);
        pnlCong.add(lblTong, BorderLayout.EAST);
        pnlTong.add(pnlCong);
        pnlGiua.add(pnlTong, BorderLayout.SOUTH);
        card.add(pnlGiua, BorderLayout.CENTER);

        btnPhaChe = TienIch.nut("Bắt đầu pha chế", TienIch.ORANGE);
        btnHoanThanh = TienIch.nut("Hoàn thành", TienIch.GREEN);
        btnHuy = TienIch.nut("Hủy đơn", TienIch.RED);
        btnIn = TienIch.nut("In lại hóa đơn", TienIch.BROWN);
        JPanel pnlNut = new JPanel(new GridLayout(0, 2, 8, 8));
        pnlNut.setOpaque(false);
        pnlNut.add(btnPhaChe);
        pnlNut.add(btnHoanThanh);
        if (!nv.laPhaChe()) { // pha chế không được hủy đơn / in lại
            pnlNut.add(btnHuy);
            pnlNut.add(btnIn);
        }
        card.add(pnlNut, BorderLayout.SOUTH);

        btnPhaChe.addActionListener(e -> doiTrangThai(HoaDon.DANG_PHA_CHE, null));
        btnHoanThanh.addActionListener(e -> doiTrangThai(HoaDon.HOAN_THANH, null));
        btnHuy.addActionListener(e -> doiTrangThai(HoaDon.DA_HUY, "Hủy đơn này? Thao tác không thể hoàn tác."));
        btnIn.addActionListener(e -> {
            if (dangXem != null) new XemHoaDon(SwingUtilities.getWindowAncestor(this), dangXem, -1, -1).setVisible(true);
        });
        return card;
    }

    private JPanel taoDong(String ten, JLabel gt) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(new JLabel(ten), BorderLayout.WEST);
        p.add(gt, BorderLayout.EAST);
        return p;
    }

    // ------------------------------------------------------------------ dữ liệu
    @Override
    public void taiLai() {
        taiDanhSach(dangXem == null ? null : dangXem.getMaHD());
    }

    private void taiDanhSach(String giuMa) {
        LocalDate tu = DinhDangNgay.layNgay(dpTu);
        LocalDate den = DinhDangNgay.layNgay(dpDen);
        if (tu == null || den == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn đủ ngày bắt đầu và kết thúc.");
            return;
        }
        if (tu.isAfter(den)) {
            JOptionPane.showMessageDialog(this, "Ngày bắt đầu không được sau ngày kết thúc.");
            return;
        }
        String tt = HoaDon.maTrangThai((String) cboTT.getSelectedItem());
        ds = dao.timKiem(tu, den, tt, txtTim.getText());
        mdl.setRowCount(0);
        int chon = -1;
        for (int i = 0; i < ds.size(); i++) {
            HoaDon h = ds.get(i);
            String gio = tu.equals(den) ? TienIch.gio(h.getNgayLap()) : h.getNgayLap().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
            mdl.addRow(new Object[] { h.getMaHD(), String.format("%03d", h.getSoThuTu()), gio, TienIch.tien(h.tinhTongTien()),
                    HoaDon.tenPhuongThuc(h.getPhuongThucTT()), HoaDon.tenTrangThai(h.getTrangThai()) });
            if (h.getMaHD().equals(giuMa)) chon = i;
        }
        lblDem.setText("Tìm thấy " + ds.size() + " đơn");
        if (chon >= 0) {
            tbl.setRowSelectionInterval(chon, chon);
        } else {
            tbl.clearSelection();
            dangXem = null;
            hienChiTiet();
        }
    }

    private void chonDong() {
        int r = tbl.getSelectedRow();
        if (r < 0 || r >= ds.size()) return;
        dangXem = dao.layHoaDon(ds.get(r).getMaHD());
        hienChiTiet();
    }

    private String html(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void hienChiTiet() {
        HoaDon h = dangXem;
        mdlCT.setRowCount(0);
        if (h == null) {
            lblTieuDe.setText("Chi tiết đơn");
            lblThongTin.setText("Chọn một đơn trong bảng bên trái");
            lblTrangThai.setVisible(false);
            lblTamTinh.setText("0đ");
            lblGiam.setText("0đ");
            lblTong.setText("0đ");
            btnPhaChe.setEnabled(false);
            btnHoanThanh.setEnabled(false);
            btnHuy.setEnabled(false);
            btnIn.setEnabled(false);
            return;
        }
        lblTieuDe.setText("Chi tiết đơn #" + String.format("%03d", h.getSoThuTu()) + "  (" + h.getMaHD() + ")");
        lblThongTin.setText("<html>" + TienIch.ngayGio(h.getNgayLap()) + " &nbsp;|&nbsp; NV: " + html(h.getNhanVien().getHoTen())
                + "<br>Thanh toán: " + HoaDon.tenPhuongThuc(h.getPhuongThucTT()) + "</html>");
        String tenTT = HoaDon.tenTrangThai(h.getTrangThai());
        lblTrangThai.setText(tenTT);
        lblTrangThai.setBackground(TienIch.mauTrangThai(tenTT));
        lblTrangThai.setVisible(true);
        for (ChiTietHoaDon ct : h.getDsChiTiet()) {
            String moTa = ct.moTaTuyChon();
            String ghiChu = (ct.getGhiChu() == null || ct.getGhiChu().trim().isEmpty()) ? "" : " | " + ct.getGhiChu().trim();
            mdlCT.addRow(new Object[] { "<html><b>" + html(ct.getMon().getTenMon()) + "</b><br><span style='color:#8A8178'>"
                    + html(moTa + ghiChu) + "</span></html>", ct.getSoLuong(), TienIch.tien(ct.tinhThanhTien()) });
        }
        lblTamTinh.setText(TienIch.tienD(h.tinhTamTinh()));
        lblGiam.setText(h.getGiamGia() > 0 ? "-" + TienIch.tienD(h.getGiamGia()) : "0đ");
        lblTong.setText(TienIch.tienD(h.tinhTongTien()));
        String tt = h.getTrangThai();
        btnPhaChe.setEnabled(HoaDon.MOI_TAO.equals(tt));
        btnHoanThanh.setEnabled(HoaDon.DANG_PHA_CHE.equals(tt));
        btnHuy.setEnabled(HoaDon.MOI_TAO.equals(tt) || HoaDon.DANG_PHA_CHE.equals(tt));
        btnIn.setEnabled(true);
    }

    private void doiTrangThai(String moi, String hoiXacNhan) {
        if (dangXem == null) return;
        if (hoiXacNhan != null && JOptionPane.showConfirmDialog(this, hoiXacNhan, "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        String ma = dangXem.getMaHD();
        if (!dao.capNhatTrangThai(ma, moi)) {
            JOptionPane.showMessageDialog(this, "Đổi trạng thái thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        taiDanhSach(ma);
        chonDong();
    }
}
