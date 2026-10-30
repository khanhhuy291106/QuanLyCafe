package gui.panel;

import dao.HoaDonDAO;
import dao.NguyenLieuDAO;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.NguyenLieu;
import gui.component.*;
import gui.util.AppColor;
import gui.util.FormatUtil;
import gui.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

/** Trang chủ: chỉ số trong ngày, đơn đang chờ và cảnh báo nguyên liệu sắp hết. */
public class PnlTrangChu extends JPanel implements Refreshable {
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final NguyenLieuDAO nlDAO = new NguyenLieuDAO();
    private final JLabel lblChao = new JLabel();
    private final KpiCard cardDoanhThu;
    private final KpiCard cardSoDon = new KpiCard("Số đơn hôm nay", AppColor.BLUE);
    private final KpiCard cardPhaChe = new KpiCard("Đơn đang pha chế", AppColor.ORANGE);
    private final KpiCard cardSapHet = new KpiCard("Nguyên liệu sắp hết", AppColor.RED);
    private final DefaultTableModel mdlCho = UI.model("STT", "Giờ", "Món", "Trạng thái");
    private final DefaultTableModel mdlHet = UI.model("Nguyên liệu", "Tồn", "Tối thiểu");

    /** Thẻ chỉ số có vạch màu bên trái. */
    private static class KpiCard extends RoundedPanel {
        private final Color mau;
        private final JLabel lblGiaTri = new JLabel("-");

        KpiCard(String tieuDe, Color mau) {
            super(new BorderLayout(0, 4), Color.WHITE, AppColor.LINE, 10);
            this.mau = mau;
            setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 12));
            add(UI.muted(tieuDe), BorderLayout.NORTH);
            lblGiaTri.setFont(new Font(AppColor.FONT_NAME, Font.BOLD, 22));
            lblGiaTri.setForeground(mau);
            add(lblGiaTri, BorderLayout.CENTER);
            setPreferredSize(new Dimension(180, 76));
        }

        void setValue(String s) {
            lblGiaTri.setText(s);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(mau);
            g2.fillRoundRect(0, 6, 6, getHeight() - 13, 6, 6);
            g2.dispose();
        }
    }

    public PnlTrangChu(Consumer<String> dieuHuong) {
        boolean ql = SessionManager.laQuanLy();
        // Quản lý xem doanh thu; nhân viên chỉ xem số đơn đã hoàn thành.
        cardDoanhThu = new KpiCard(ql ? "Doanh thu hôm nay" : "Đơn đã hoàn thành hôm nay", AppColor.GREEN);
        setLayout(new BorderLayout(0, 14));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        setBackground(AppColor.BG);

        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 10, 0);
        lblChao.setFont(AppColor.FONT_TITLE);
        lblChao.setForeground(AppColor.INK);
        lblChao.setText("Xin chào, " + SessionManager.getNhanVien().getHoTen() + "!");
        top.add(lblChao, g);

        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        cards.add(cardDoanhThu);
        cards.add(cardSoDon);
        cards.add(cardPhaChe);
        cards.add(cardSapHet);
        g.gridy = 1;
        top.add(cards, g);

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nut.setOpaque(false);
        if (!SessionManager.laPhaChe()) {
            BoGocButton b = UI.button("+ Tạo đơn mới", AppColor.GREEN);
            b.addActionListener(e -> dieuHuong.accept("sell"));
            nut.add(b);
        }
        BoGocButton bCho = UI.button("Xem đơn đang chờ", AppColor.BROWN);
        bCho.addActionListener(e -> dieuHuong.accept("orders"));
        nut.add(bCho);
        if (ql) {
            BoGocButton bKho = UI.button("Lập phiếu nhập", AppColor.ACCENT);
            bKho.addActionListener(e -> dieuHuong.accept("stock:nhap"));
            nut.add(bKho);
        }
        g.gridy = 2;
        g.insets = new Insets(2, 0, 0, 0);
        top.add(nut, g);
        add(top, BorderLayout.NORTH);

        JPanel giua = new JPanel(new GridBagLayout());
        giua.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridy = 0;
        c.gridx = 0;
        c.weightx = 0.62;
        c.insets = new Insets(0, 0, 0, 14);
        giua.add(khung("Đơn đang chờ", mdlCho, AppColor.BROWN, true), c);
        c.gridx = 1;
        c.weightx = 0.38;
        c.insets = new Insets(0, 0, 0, 0);
        giua.add(khung("Nguyên liệu sắp hết", mdlHet, AppColor.RED, false), c);
        add(giua, BorderLayout.CENTER);
    }

    private JComponent khung(String tieuDe, DefaultTableModel m, Color mau, boolean donCho) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel t = UI.title(tieuDe);
        t.setForeground(mau);
        p.add(t, BorderLayout.NORTH);
        JTable tb = UI.table(m);
        tb.setRowHeight(30);
        if (donCho) {
            UI.alignCenter(tb, 0, 1);
            UI.badge(tb, 3);
            UI.widths(tb, 50, 60, 280, 120);
        } else {
            UI.alignRight(tb, 1, 2);
            UI.warnRows(tb, r -> true);
        }
        p.add(UI.scroll(tb), BorderLayout.CENTER);
        return p;
    }

    @Override
    public void refresh() {
        UI.run(() -> {
            LocalDate hn = LocalDate.now();
            if (SessionManager.laQuanLy()) cardDoanhThu.setValue(FormatUtil.tienD(hoaDonDAO.doanhThuNgay(hn)));
            else cardDoanhThu.setValue(String.valueOf(hoaDonDAO.demDon(hn, HoaDon.HOAN_THANH)));
            cardSoDon.setValue(String.valueOf(hoaDonDAO.demDon(hn, null)));
            cardPhaChe.setValue(String.valueOf(hoaDonDAO.demDon(hn, HoaDon.DANG_PHA_CHE)));

            List<NguyenLieu> het = nlDAO.getSapHet();
            cardSapHet.setValue(String.valueOf(het.size()));
            mdlHet.setRowCount(0);
            for (NguyenLieu n : het)
                mdlHet.addRow(new Object[]{n.getTenNL(), FormatUtil.soThuc(n.getSoLuongTon()) + " " + n.getDonViTinh(),
                        FormatUtil.soThuc(n.getMucToiThieu()) + " " + n.getDonViTinh()});

            mdlCho.setRowCount(0);
            int dem = 0;
            for (HoaDon h : hoaDonDAO.donDangCho()) {
                String tomTat = "";
                if (dem++ < 12) {
                    StringBuilder sb = new StringBuilder();
                    List<ChiTietHoaDon> ds = hoaDonDAO.layChiTiet(h.getMaHD());
                    for (int i = 0; i < ds.size() && i < 3; i++)
                        sb.append(i > 0 ? ", " : "").append(ds.get(i).getMon().getTenMon()).append(ds.get(i).getSoLuong() > 1 ? " x" + ds.get(i).getSoLuong() : "");
                    if (ds.size() > 3) sb.append("…");
                    tomTat = sb.toString();
                }
                mdlCho.addRow(new Object[]{String.format("%03d", h.getSoThuTu()), FormatUtil.gio(h.getNgayLap()), tomTat,
                        HoaDon.tenTrangThai(h.getTrangThai())});
            }
        });
    }
}
