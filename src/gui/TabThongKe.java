package gui;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import dao.HoaDon_DAO;

// Thống kê doanh thu theo ngày/tháng, món bán chạy, xuất báo cáo CSV. CHỈ QUẢN LÝ được xem.
// Doanh thu tính các đơn chưa hủy (khách trả tiền ngay khi tạo đơn).
public class TabThongKe extends JPanel implements TaiLai {
    private HoaDon_DAO dao = new HoaDon_DAO();
    private JDatePickerImpl dpTu = DinhDangNgay.taoDatePicker(LocalDate.now().minusDays(6));
    private JDatePickerImpl dpDen = DinhDangNgay.taoDatePicker(LocalDate.now());
    private JComboBox<String> cboKieu = new JComboBox<>(new String[] { "Theo ngày", "Theo tháng" });
    private JLabel lblDoanhThu = new JLabel("-");
    private JLabel lblSoDon = new JLabel("-");
    private JLabel lblTB = new JLabel("-");
    private JLabel lblTop = new JLabel("-");
    private JPanel pnlCot = new JPanel(new BorderLayout());
    private JPanel pnlTop = new JPanel(new BorderLayout());
    private DefaultTableModel mdl = TienIch.moHinh("Ngày", "Số đơn", "Doanh thu (đ)", "TB / đơn (đ)");
    private JTable tbl = TienIch.bang(mdl);

    public TabThongKe() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        setBackground(TienIch.BG);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(0, 0, 10, 0);

        // bộ lọc
        JPanel pnlLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pnlLoc.setOpaque(false);
        JButton btnTK = TienIch.nut("Thống kê", TienIch.BROWN);
        JButton btnCsv = TienIch.nut("Xuất CSV", TienIch.GREEN);
        pnlLoc.add(new JLabel("Từ ngày"));
        pnlLoc.add(dpTu);
        pnlLoc.add(new JLabel("Đến ngày"));
        pnlLoc.add(dpDen);
        pnlLoc.add(new JLabel("Xem"));
        pnlLoc.add(cboKieu);
        pnlLoc.add(btnTK);
        pnlLoc.add(btnCsv);
        g.gridy = 0;
        g.weighty = 0;
        add(pnlLoc, g);

        // thẻ chỉ số
        JPanel pnlThe = new JPanel(new GridLayout(1, 4, 12, 0));
        pnlThe.setOpaque(false);
        pnlThe.add(taoThe("Tổng doanh thu", lblDoanhThu, TienIch.GREEN, 20));
        pnlThe.add(taoThe("Số đơn", lblSoDon, TienIch.BLUE, 20));
        pnlThe.add(taoThe("Giá trị trung bình / đơn", lblTB, TienIch.ACCENT, 20));
        pnlThe.add(taoThe("Món bán chạy nhất", lblTop, TienIch.BROWN, 15));
        g.gridy = 1;
        add(pnlThe, g);

        // biểu đồ
        JPanel pnlBieuDo = new JPanel(new GridBagLayout());
        pnlBieuDo.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridy = 0;
        c.gridx = 0;
        c.weightx = 0.6;
        c.insets = new Insets(0, 0, 0, 12);
        pnlBieuDo.add(taoKhung("Doanh thu (triệu đồng)", pnlCot), c);
        c.gridx = 1;
        c.weightx = 0.4;
        c.insets = new Insets(0, 0, 0, 0);
        pnlBieuDo.add(taoKhung("Top 5 món bán chạy (số ly)", pnlTop), c);
        g.gridy = 2;
        g.weighty = 0.6;
        add(pnlBieuDo, g);

        // bảng chi tiết
        JPanel pnlBang = new JPanel(new BorderLayout(0, 6));
        pnlBang.setOpaque(false);
        pnlBang.add(TienIch.tieuDe("Doanh thu chi tiết"), BorderLayout.NORTH);
        TienIch.canPhai(tbl, 1, 2, 3);
        pnlBang.add(TienIch.cuon(tbl), BorderLayout.CENTER);
        g.gridy = 3;
        g.weighty = 0.4;
        g.insets = new Insets(0, 0, 0, 0);
        add(pnlBang, g);

        btnTK.addActionListener(e -> thongKe());
        btnCsv.addActionListener(e -> xuatCsv());
    }

    private JPanel taoThe(String tieuDe, JLabel lblGiaTri, Color mau, int coChu) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(TienIch.LINE),
                BorderFactory.createEmptyBorder(8, 16, 8, 10)));
        p.add(TienIch.ghiChu(tieuDe), BorderLayout.NORTH);
        lblGiaTri.setFont(new Font(TienIch.FONT_NAME, Font.BOLD, coChu));
        lblGiaTri.setForeground(mau);
        p.add(lblGiaTri, BorderLayout.CENTER);
        p.setPreferredSize(new Dimension(150, 64));
        return p;
    }

    private JPanel taoKhung(String tieuDe, JPanel noiDung) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(TienIch.LINE),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        p.add(TienIch.tieuDe(tieuDe), BorderLayout.NORTH);
        noiDung.setOpaque(false);
        p.add(noiDung, BorderLayout.CENTER);
        return p;
    }

    @Override
    public void taiLai() {
        thongKe();
    }

    private void thongKe() {
        LocalDate tu = DinhDangNgay.layNgay(dpTu);
        LocalDate den = DinhDangNgay.layNgay(dpDen);
        if (tu == null || den == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn đủ ngày bắt đầu và ngày kết thúc.");
            return;
        }
        if (tu.isAfter(den)) {
            JOptionPane.showMessageDialog(this, "Ngày bắt đầu không được sau ngày kết thúc.");
            return;
        }
        if (ChronoUnit.DAYS.between(tu, den) > 730) {
            JOptionPane.showMessageDialog(this, "Chỉ thống kê tối đa 2 năm mỗi lần.");
            return;
        }
        boolean theoNgay = cboKieu.getSelectedIndex() == 0;
        ArrayList<Object[]> rows = theoNgay ? dao.thongKeTheoNgay(tu, den) : dao.thongKeTheoThang(tu, den);
        ArrayList<Object[]> top = dao.topMon(tu, den, 5);

        mdl.setColumnIdentifiers(new String[] { theoNgay ? "Ngày" : "Tháng", "Số đơn", "Doanh thu (đ)", "TB / đơn (đ)" });
        TienIch.canPhai(tbl, 1, 2, 3);
        mdl.setRowCount(0);
        DefaultCategoryDataset dsCot = new DefaultCategoryDataset();
        double tong = 0;
        int tongDon = 0;
        for (Object[] r : rows) {
            String nhan = theoNgay ? TienIch.ngay((LocalDate) r[0]) : (String) r[0];
            int soDon = (Integer) r[1];
            double doanhThu = (Double) r[2];
            tong += doanhThu;
            tongDon += soDon;
            mdl.addRow(new Object[] { nhan, soDon, TienIch.tien(doanhThu), TienIch.tien(soDon == 0 ? 0 : doanhThu / soDon) });
            String nhanNgan = theoNgay ? ((LocalDate) r[0]).format(DateTimeFormatter.ofPattern("dd/MM")) : nhan;
            dsCot.addValue(doanhThu / 1000000.0, "Doanh thu", nhanNgan);
        }
        lblDoanhThu.setText(TienIch.tienD(tong));
        lblSoDon.setText(String.valueOf(tongDon));
        lblTB.setText(TienIch.tienD(tongDon == 0 ? 0 : tong / tongDon));
        lblTop.setText(top.isEmpty() ? "-" : (String) top.get(0)[0]);

        DefaultCategoryDataset dsTop = new DefaultCategoryDataset();
        for (Object[] r : top) {
            dsTop.addValue((Integer) r[1], "Số ly", (String) r[0]);
        }
        veBieuDo(pnlCot, dsCot, PlotOrientation.VERTICAL, TienIch.BROWN, "Triệu đồng");
        veBieuDo(pnlTop, dsTop, PlotOrientation.HORIZONTAL, TienIch.GREEN, "Số ly");
    }

    private void veBieuDo(JPanel pnl, DefaultCategoryDataset data, PlotOrientation huong, Color mau, String tenTruc) {
        JFreeChart chart = ChartFactory.createBarChart(null, null, tenTruc, data, huong, false, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(TienIch.LINE);
        BarRenderer r = (BarRenderer) plot.getRenderer();
        r.setSeriesPaint(0, mau);
        r.setMaximumBarWidth(0.15);
        r.setDefaultItemLabelsVisible(true);
        if (huong == PlotOrientation.VERTICAL) {
            plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }
        pnl.removeAll();
        pnl.add(new ChartPanel(chart), BorderLayout.CENTER);
        pnl.revalidate();
        pnl.repaint();
    }

    // Xuất bảng đang hiển thị ra file CSV (UTF-8 có BOM để Excel đọc đúng tiếng Việt)
    private void xuatCsv() {
        if (mdl.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Chưa có dữ liệu để xuất. Hãy bấm Thống kê trước.");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("thong_ke_doanh_thu_" + DinhDangNgay.layNgay(dpTu) + "_" + DinhDangNgay.layNgay(dpDen) + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".csv")) f = new File(f.getParentFile(), f.getName() + ".csv");
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(f.toPath()), StandardCharsets.UTF_8)) {
            w.write('\uFEFF');
            for (int c = 0; c < mdl.getColumnCount(); c++) {
                w.write((c > 0 ? "," : "") + "\"" + mdl.getColumnName(c) + "\"");
            }
            w.write("\r\n");
            for (int r = 0; r < mdl.getRowCount(); r++) {
                for (int c = 0; c < mdl.getColumnCount(); c++) {
                    String v = String.valueOf(mdl.getValueAt(r, c)).replace("\"", "\"\"");
                    w.write((c > 0 ? "," : "") + "\"" + v + "\"");
                }
                w.write("\r\n");
            }
            JOptionPane.showMessageDialog(this, "Đã xuất file CSV. Mở bằng Excel để xem.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không ghi được file: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
