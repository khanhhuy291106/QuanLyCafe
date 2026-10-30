package gui.panel;

import dao.HoaDonDAO;
import gui.component.*;
import gui.util.AppColor;
import gui.util.ExportUtil;
import gui.util.FormatUtil;
import gui.util.SessionManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Thống kê doanh thu theo ngày/tháng, món bán chạy, xuất báo cáo CSV.
 * CHỈ QUẢN LÝ được xem. Doanh thu tính các đơn chưa hủy (khách trả tiền ngay khi tạo đơn).
 */
public class PnlThongKe extends JPanel implements Refreshable {
    private final HoaDonDAO dao = new HoaDonDAO();
    private final DateField dfTu = new DateField(LocalDate.now().minusDays(6));
    private final DateField dfDen = new DateField(LocalDate.now());
    private final JComboBox<String> cboKieu = UI.combo(new String[]{"Theo ngày", "Theo tháng"});
    private final JLabel lblDoanhThu = new JLabel("-");
    private final JLabel lblSoDon = new JLabel("-");
    private final JLabel lblTB = new JLabel("-");
    private final JLabel lblTop = new JLabel("-");
    private final JPanel pnlCot = new JPanel(new BorderLayout());
    private final JPanel pnlTop = new JPanel(new BorderLayout());
    private final DefaultTableModel mdl = UI.model("Ngày", "Số đơn", "Doanh thu (đ)", "TB / đơn (đ)");
    private final JTable tbl = UI.table(mdl);
    private final Font fontBieuDo = new Font(AppColor.FONT_NAME, Font.PLAIN, 11);

    public PnlThongKe() {
        if (!SessionManager.laQuanLy()) throw new SecurityException("Chỉ Quản lý mới xem được thống kê.");
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        setBackground(AppColor.BG);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(0, 0, 10, 0);

        // bộ lọc
        JPanel loc = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 4));
        loc.setOpaque(false);
        loc.add(UI.label("Từ ngày"));
        loc.add(dfTu);
        loc.add(UI.label("Đến ngày"));
        loc.add(dfDen);
        loc.add(UI.label("Xem"));
        cboKieu.setPreferredSize(new Dimension(120, 30));
        loc.add(cboKieu);
        BoGocButton tk = UI.button("Thống kê", AppColor.BROWN);
        BoGocButton csv = UI.button("Xuất CSV", AppColor.GREEN);
        loc.add(tk);
        loc.add(csv);
        g.gridy = 0;
        g.weighty = 0;
        add(loc, g);

        // thẻ chỉ số
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        cards.add(the("Tổng doanh thu", lblDoanhThu, AppColor.GREEN, 20));
        cards.add(the("Số đơn", lblSoDon, AppColor.BLUE, 20));
        cards.add(the("Giá trị trung bình / đơn", lblTB, AppColor.ACCENT, 20));
        cards.add(the("Món bán chạy nhất", lblTop, AppColor.BROWN, 15));
        g.gridy = 1;
        add(cards, g);

        // biểu đồ
        JPanel charts = new JPanel(new GridBagLayout());
        charts.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridy = 0;
        c.gridx = 0;
        c.weightx = 0.6;
        c.insets = new Insets(0, 0, 0, 12);
        charts.add(khung("Doanh thu (triệu đồng)", pnlCot), c);
        c.gridx = 1;
        c.weightx = 0.4;
        c.insets = new Insets(0, 0, 0, 0);
        charts.add(khung("Top 5 món bán chạy (số ly)", pnlTop), c);
        g.gridy = 2;
        g.weighty = 0.6;
        add(charts, g);

        // bảng chi tiết
        JPanel bang = new JPanel(new BorderLayout(0, 6));
        bang.setOpaque(false);
        bang.add(UI.title("Doanh thu chi tiết"), BorderLayout.NORTH);
        UI.alignRight(tbl, 1, 2, 3);
        bang.add(UI.scroll(tbl), BorderLayout.CENTER);
        g.gridy = 3;
        g.weighty = 0.4;
        g.insets = new Insets(0, 0, 0, 0);
        add(bang, g);

        tk.addActionListener(UI.on(this::thongKe));
        csv.addActionListener(e -> {
            if (mdl.getRowCount() == 0) {
                UI.warn("Chưa có dữ liệu để xuất. Hãy bấm Thống kê trước.");
                return;
            }
            if (ExportUtil.xuatCsv(this, mdl, "thong_ke_doanh_thu_" + dfTu.getDate() + "_" + dfDen.getDate()))
                UI.info("Đã xuất file CSV. Mở bằng Excel để xem.");
        });
    }

    private JComponent the(String tieuDe, JLabel gt, Color mau, int coChu) {
        RoundedPanel p = new RoundedPanel(new BorderLayout(0, 4), Color.WHITE, AppColor.LINE, 10);
        p.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 10));
        p.add(UI.muted(tieuDe), BorderLayout.NORTH);
        gt.setFont(new Font(AppColor.FONT_NAME, Font.BOLD, coChu));
        gt.setForeground(mau);
        p.add(gt, BorderLayout.CENTER);
        p.setPreferredSize(new Dimension(150, 64));
        return p;
    }

    private JComponent khung(String tieuDe, JComponent noiDung) {
        RoundedPanel p = new RoundedPanel(new BorderLayout(0, 4), Color.WHITE, AppColor.LINE, 10);
        p.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        p.add(UI.title(tieuDe), BorderLayout.NORTH);
        noiDung.setOpaque(false);
        p.add(noiDung, BorderLayout.CENTER);
        return p;
    }

    @Override
    public void refresh() {
        thongKe();
    }

    private void thongKe() {
        LocalDate tu = dfTu.getDate(), den = dfDen.getDate();
        if (tu == null || den == null) {
            UI.warn("Hãy chọn đủ ngày bắt đầu và ngày kết thúc.");
            return;
        }
        if (tu.isAfter(den)) {
            UI.warn("Ngày bắt đầu không được sau ngày kết thúc.");
            return;
        }
        if (ChronoUnit.DAYS.between(tu, den) > 730) {
            UI.warn("Chỉ thống kê tối đa 2 năm mỗi lần.");
            return;
        }
        boolean theoNgay = cboKieu.getSelectedIndex() == 0;
        List<Object[]> rows;
        List<Object[]> top;
        try {
            rows = theoNgay ? dao.thongKeTheoNgay(tu, den) : dao.thongKeTheoThang(tu, den);
            top = dao.topMon(tu, den, 5);
        } catch (dao.DaoException e) {
            UI.error(e.getMessage());
            return;
        }

        mdl.setColumnIdentifiers(new String[]{theoNgay ? "Ngày" : "Tháng", "Số đơn", "Doanh thu (đ)", "TB / đơn (đ)"});
        UI.alignRight(tbl, 1, 2, 3);
        mdl.setRowCount(0);
        DefaultCategoryDataset dsCot = new DefaultCategoryDataset();
        double tong = 0;
        int tongDon = 0;
        for (Object[] r : rows) {
            String nhan = theoNgay ? FormatUtil.ngay((LocalDate) r[0]) : (String) r[0];
            int so = (Integer) r[1];
            double dt = (Double) r[2];
            tong += dt;
            tongDon += so;
            mdl.addRow(new Object[]{nhan, so, FormatUtil.tien(dt), FormatUtil.tien(so == 0 ? 0 : dt / so)});
            String ngan = theoNgay ? ((LocalDate) r[0]).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")) : nhan;
            dsCot.addValue(dt / 1_000_000.0, "Doanh thu", ngan);
        }
        lblDoanhThu.setText(FormatUtil.tienD(tong));
        lblSoDon.setText(String.valueOf(tongDon));
        lblTB.setText(FormatUtil.tienD(tongDon == 0 ? 0 : tong / tongDon));
        lblTop.setText(top.isEmpty() ? "-" : (String) top.get(0)[0]);

        DefaultCategoryDataset dsTop = new DefaultCategoryDataset();
        for (Object[] r : top) dsTop.addValue((Integer) r[1], "Số ly", (String) r[0]);

        manHinh(pnlCot, taoBieuDo(dsCot, PlotOrientation.VERTICAL, AppColor.BROWN, "0.0", rows.size() <= 14, "Triệu đồng"));
        manHinh(pnlTop, taoBieuDo(dsTop, PlotOrientation.HORIZONTAL, AppColor.GREEN, "0", true, "Số ly"));
    }

    private void manHinh(JPanel p, JFreeChart chart) {
        p.removeAll();
        ChartPanel cp = new ChartPanel(chart);
        cp.setOpaque(false);
        cp.setMouseWheelEnabled(false);
        cp.setPopupMenu(null);
        p.add(cp, BorderLayout.CENTER);
        p.revalidate();
        p.repaint();
    }

    private JFreeChart taoBieuDo(DefaultCategoryDataset ds, PlotOrientation huong, Color mau, String dinhDang, boolean hienNhan, String truc) {
        JFreeChart chart = ChartFactory.createBarChart(null, null, truc, ds, huong, false, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(AppColor.LINE);
        BarRenderer r = (BarRenderer) plot.getRenderer();
        r.setSeriesPaint(0, mau);
        r.setBarPainter(new StandardBarPainter());
        r.setShadowVisible(false);
        r.setMaximumBarWidth(0.12);
        if (huong == PlotOrientation.HORIZONTAL) r.setMaximumBarWidth(0.2);
        r.setDefaultItemLabelsVisible(hienNhan);
        r.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat(dinhDang)));
        r.setDefaultItemLabelFont(fontBieuDo.deriveFont(Font.BOLD));
        CategoryAxis dx = plot.getDomainAxis();
        dx.setTickLabelFont(fontBieuDo);
        dx.setAxisLineVisible(false);
        if (huong == PlotOrientation.VERTICAL) dx.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        NumberAxis rx = (NumberAxis) plot.getRangeAxis();
        rx.setTickLabelFont(fontBieuDo);
        rx.setLabelFont(fontBieuDo);
        rx.setAxisLineVisible(false);
        rx.setUpperMargin(0.15);
        return chart;
    }
}
