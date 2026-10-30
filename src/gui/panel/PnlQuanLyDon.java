package gui.panel;

import dao.HoaDonDAO;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import gui.component.*;
import gui.dialog.DlgHoaDon;
import gui.util.AppColor;
import gui.util.FormatUtil;
import gui.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý đơn: tra cứu đơn, xem chi tiết, đổi trạng thái.
 * Pha chế chỉ được đổi trạng thái; hủy đơn và in lại hóa đơn dành cho Thu ngân và Quản lý.
 */
public class PnlQuanLyDon extends JPanel implements Refreshable {
    private final HoaDonDAO dao = new HoaDonDAO();
    private final DateField dfTu = new DateField(LocalDate.now());
    private final DateField dfDen = new DateField(LocalDate.now());
    private final JComboBox<String> cboTT = UI.combo(new String[]{"Tất cả", "Mới tạo", "Đang pha chế", "Hoàn thành", "Đã hủy"});
    private final JTextField txtTim = UI.field(14);
    private final DefaultTableModel mdl = UI.model("Mã HĐ", "STT", "Giờ", "Tổng tiền", "Thanh toán", "Trạng thái");
    private final JTable tbl = UI.table(mdl);
    private final JLabel lblDem = UI.muted(" ");
    private List<HoaDon> ds = new ArrayList<>();
    private HoaDon dangXem;

    // chi tiết
    private final JLabel lblTieuDe = UI.title("Chi tiết đơn");
    private final JLabel lblThongTin = UI.label(" ");
    private final PillLabel lblTrangThai = new PillLabel();
    private final DefaultTableModel mdlCT = UI.model("Món", "SL", "Thành tiền");
    private final JTable tblCT = UI.table(mdlCT);
    private final JLabel lblTamTinh = UI.label("0đ");
    private final JLabel lblGiam = UI.label("0đ");
    private final JLabel lblTong = new JLabel("0đ");
    private final BoGocButton btnPhaChe = UI.button("Bắt đầu pha chế", AppColor.ORANGE);
    private final BoGocButton btnHoanThanh = UI.button("Hoàn thành", AppColor.GREEN);
    private final BoGocButton btnHuy = UI.button("Hủy đơn", AppColor.RED);
    private final BoGocButton btnIn = UI.button("In lại hóa đơn", AppColor.BROWN);

    public PnlQuanLyDon() {
        setLayout(new BorderLayout(12, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(AppColor.BG);
        add(buildTrai(), BorderLayout.CENTER);
        add(buildPhai(), BorderLayout.EAST);
        hienChiTiet();
    }

    private JComponent buildTrai() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        JPanel loc = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 4));
        loc.setOpaque(false);
        loc.add(UI.label("Từ ngày"));
        loc.add(dfTu);
        loc.add(UI.label("Đến"));
        loc.add(dfDen);
        loc.add(UI.label("Trạng thái"));
        cboTT.setPreferredSize(new Dimension(130, 30));
        loc.add(cboTT);
        txtTim.setToolTipText("Mã hóa đơn hoặc số thứ tự");
        txtTim.setPreferredSize(new Dimension(150, 30));
        loc.add(txtTim);
        BoGocButton tim = UI.button("Tìm", AppColor.BROWN);
        BoGocButton lm = new BoGocButton("Hôm nay", AppColor.LATTE).foreground(AppColor.BROWN);
        loc.add(tim);
        loc.add(lm);
        p.add(loc, BorderLayout.NORTH);

        UI.alignCenter(tbl, 1, 2);
        UI.alignRight(tbl, 3);
        UI.badge(tbl, 5);
        UI.widths(tbl, 80, 50, 70, 100, 110, 130);
        tbl.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) chonDong();
        });
        p.add(UI.scroll(tbl), BorderLayout.CENTER);
        p.add(lblDem, BorderLayout.SOUTH);

        tim.addActionListener(UI.on(this::refresh));
        txtTim.addActionListener(UI.on(this::refresh));
        lm.addActionListener(UI.on(() -> {
            dfTu.setDate(LocalDate.now());
            dfDen.setDate(LocalDate.now());
            cboTT.setSelectedIndex(0);
            txtTim.setText("");
            refresh();
        }));
        return p;
    }

    private JComponent buildPhai() {
        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 8), Color.WHITE, AppColor.LINE, 10);
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));
        card.setPreferredSize(new Dimension(380, 100));

        JPanel head = new JPanel(new GridLayout(0, 1, 0, 4));
        head.setOpaque(false);
        head.add(lblTieuDe);
        head.add(lblThongTin);
        JPanel tt = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tt.setOpaque(false);
        tt.add(lblTrangThai);
        head.add(tt);
        card.add(head, BorderLayout.NORTH);

        tblCT.setRowHeight(62);
        UI.alignCenter(tblCT, 1);
        UI.alignRight(tblCT, 2);
        UI.widths(tblCT, 220, 36, 80);
        JPanel giua = new JPanel(new BorderLayout(0, 8));
        giua.setOpaque(false);
        giua.add(UI.scroll(tblCT), BorderLayout.CENTER);
        JPanel tong = new JPanel(new GridLayout(0, 1, 0, 3));
        tong.setOpaque(false);
        tong.add(dong("Tạm tính", lblTamTinh));
        lblGiam.setForeground(AppColor.RED);
        tong.add(dong("Giảm giá", lblGiam));
        JPanel t = new JPanel(new BorderLayout());
        t.setOpaque(false);
        t.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppColor.LINE));
        t.add(UI.bold("TỔNG CỘNG"), BorderLayout.WEST);
        lblTong.setFont(new Font(AppColor.FONT_NAME, Font.BOLD, 18));
        lblTong.setForeground(AppColor.GREEN);
        t.add(lblTong, BorderLayout.EAST);
        tong.add(t);
        giua.add(tong, BorderLayout.SOUTH);
        card.add(giua, BorderLayout.CENTER);

        JPanel bt = new JPanel(new GridLayout(0, 2, 8, 8));
        bt.setOpaque(false);
        bt.add(btnPhaChe);
        bt.add(btnHoanThanh);
        if (!SessionManager.laPhaChe()) {
            bt.add(btnHuy);
            bt.add(btnIn);
        }
        card.add(bt, BorderLayout.SOUTH);

        btnPhaChe.addActionListener(UI.on(() -> doiTrangThai(HoaDon.DANG_PHA_CHE, null)));
        btnHoanThanh.addActionListener(UI.on(() -> doiTrangThai(HoaDon.HOAN_THANH, null)));
        btnHuy.addActionListener(UI.on(() -> doiTrangThai(HoaDon.DA_HUY, "Hủy đơn này? Thao tác không thể hoàn tác.")));
        btnIn.addActionListener(e -> {
            if (dangXem != null) new DlgHoaDon(SwingUtilities.getWindowAncestor(this), dangXem, -1, -1).setVisible(true);
        });
        return card;
    }

    private JPanel dong(String ten, JLabel gt) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(UI.label(ten), BorderLayout.WEST);
        p.add(gt, BorderLayout.EAST);
        return p;
    }

    // ------------------------------------------------------------------ dữ liệu
    @Override
    public void refresh() {
        taiDanhSach(dangXem == null ? null : dangXem.getMaHD());
    }

    private void taiDanhSach(String giuMa) {
        LocalDate tu = dfTu.getDate(), den = dfDen.getDate();
        if (tu == null || den == null) {
            UI.warn("Hãy chọn đủ ngày bắt đầu và kết thúc.");
            return;
        }
        if (tu.isAfter(den)) {
            UI.warn("Ngày bắt đầu không được sau ngày kết thúc.");
            return;
        }
        String tt = HoaDon.maTrangThai((String) cboTT.getSelectedItem());
        UI.run(() -> ds = dao.timKiem(tu, den, tt, txtTim.getText()));
        mdl.setRowCount(0);
        int chon = -1;
        for (int i = 0; i < ds.size(); i++) {
            HoaDon h = ds.get(i);
            mdl.addRow(new Object[]{h.getMaHD(), String.format("%03d", h.getSoThuTu()),
                    tu.equals(den) ? FormatUtil.gio(h.getNgayLap()) : h.getNgayLap().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm")),
                    FormatUtil.tien(h.tinhTongTien()), HoaDon.tenPhuongThuc(h.getPhuongThucTT()), HoaDon.tenTrangThai(h.getTrangThai())});
            if (h.getMaHD().equals(giuMa)) chon = i;
        }
        lblDem.setText("Tìm thấy " + ds.size() + " đơn");
        if (chon >= 0) tbl.setRowSelectionInterval(chon, chon);
        else {
            tbl.clearSelection();
            dangXem = null;
            hienChiTiet();
        }
    }

    private void chonDong() {
        int r = tbl.getSelectedRow();
        if (r < 0 || r >= ds.size()) return;
        UI.run(() -> dangXem = dao.layHoaDon(ds.get(r).getMaHD()));
        hienChiTiet();
    }

    private static String html(String s) {
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
        lblThongTin.setText("<html>" + FormatUtil.ngayGio(h.getNgayLap()) + " &nbsp;|&nbsp; NV: " + html(h.getNhanVien().getHoTen())
                + "<br>Thanh toán: " + HoaDon.tenPhuongThuc(h.getPhuongThucTT()) + "</html>");
        String tenTT = HoaDon.tenTrangThai(h.getTrangThai());
        lblTrangThai.setText(tenTT);
        lblTrangThai.setNen(AppColor.statusColor(tenTT));
        lblTrangThai.setVisible(true);
        for (ChiTietHoaDon ct : h.getDsChiTiet()) {
            String mt = ct.moTaTuyChon();
            String gc = ct.getGhiChu() == null || ct.getGhiChu().trim().isEmpty() ? "" : " | " + ct.getGhiChu().trim();
            mdlCT.addRow(new Object[]{"<html><b>" + html(ct.getMon().getTenMon()) + "</b><br><span style='color:#8A8178'>"
                    + html(mt + gc) + "</span></html>", ct.getSoLuong(), FormatUtil.tien(ct.tinhThanhTien())});
        }
        lblTamTinh.setText(FormatUtil.tienD(h.tinhTamTinh()));
        lblGiam.setText(h.getGiamGia() > 0 ? "-" + FormatUtil.tienD(h.getGiamGia()) : "0đ");
        lblTong.setText(FormatUtil.tienD(h.tinhTongTien()));
        String tt = h.getTrangThai();
        btnPhaChe.setEnabled(HoaDon.MOI_TAO.equals(tt));
        btnHoanThanh.setEnabled(HoaDon.DANG_PHA_CHE.equals(tt));
        btnHuy.setEnabled(HoaDon.MOI_TAO.equals(tt) || HoaDon.DANG_PHA_CHE.equals(tt));
        btnIn.setEnabled(true);
    }

    private void doiTrangThai(String moi, String hoiXacNhan) {
        if (dangXem == null) return;
        if (hoiXacNhan != null && !UI.confirm(hoiXacNhan)) return;
        String ma = dangXem.getMaHD();
        dao.capNhatTrangThai(ma, moi);
        taiDanhSach(ma);
        chonDong();
    }
}
