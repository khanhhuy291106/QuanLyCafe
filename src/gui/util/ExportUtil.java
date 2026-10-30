package gui.util;

import entity.ChiTietHoaDon;
import entity.HoaDon;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.print.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/** Xuất file CSV (mở được bằng Excel), dựng nội dung hóa đơn và in hóa đơn. */
public final class ExportUtil {
    private ExportUtil() {
    }

    // Thông tin quán in trên hóa đơn: sửa tại đây.
    public static final String TEN_QUAN = "CAFÉ ABC";
    public static final String DIA_CHI = "12 Đường Mẫu, TP. Hồ Chí Minh";
    public static final String DIEN_THOAI = "ĐT: 0900 000 000";
    private static final int WIDTH = 40;

    // ------------------------------------------------------------------ CSV
    /** Mở hộp thoại lưu file rồi ghi nội dung bảng ra CSV. Trả về true nếu đã lưu. */
    public static boolean xuatCsv(Component parent, TableModel model, String tenGoiY) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(tenGoiY + ".csv"));
        if (fc.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return false;
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".csv")) f = new File(f.getParentFile(), f.getName() + ".csv");
        List<String[]> rows = new ArrayList<>();
        String[] head = new String[model.getColumnCount()];
        for (int c = 0; c < head.length; c++) head[c] = model.getColumnName(c);
        rows.add(head);
        for (int r = 0; r < model.getRowCount(); r++) {
            String[] row = new String[head.length];
            for (int c = 0; c < head.length; c++) row[c] = String.valueOf(model.getValueAt(r, c));
            rows.add(row);
        }
        try {
            ghiCsv(f, rows);
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "Không ghi được file: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /** Ghi UTF-8 có BOM để Excel đọc đúng tiếng Việt. */
    public static void ghiCsv(File f, List<String[]> rows) throws IOException {
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(f.toPath()), StandardCharsets.UTF_8)) {
            w.write('\uFEFF');
            for (String[] row : rows) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) sb.append(',');
                    String v = row[i] == null ? "" : row[i];
                    sb.append('"').append(v.replace("\"", "\"\"")).append('"');
                }
                w.write(sb.append("\r\n").toString());
            }
        }
    }

    // ------------------------------------------------------------------ Hóa đơn
    private static String pad(String s, int w) {
        if (s.length() >= w) return s.substring(0, w);
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < w) sb.append(' ');
        return sb.toString();
    }

    private static String giua(String s) {
        int sp = Math.max(0, (WIDTH - s.length()) / 2);
        return pad("", sp) + s;
    }

    private static String haiBen(String trai, String phai) {
        int sp = WIDTH - trai.length() - phai.length();
        if (sp < 1) {
            trai = trai.substring(0, Math.max(0, trai.length() + sp - 1));
            sp = 1;
        }
        return trai + pad("", sp) + phai;
    }

    private static String gach() {
        return "-".repeat(WIDTH);
    }

    /** Dựng các dòng chữ của hóa đơn. khachDua và tienThua âm nghĩa là không in (chuyển khoản hoặc in lại). */
    public static List<String> noiDungHoaDon(HoaDon hd, double khachDua, double tienThua) {
        List<String> l = new ArrayList<>();
        l.add(giua(TEN_QUAN));
        l.add(giua(DIA_CHI));
        l.add(giua(DIEN_THOAI));
        l.add(gach());
        l.add(giua("HÓA ĐƠN BÁN HÀNG"));
        l.add("Số: " + hd.getMaHD() + "   STT: #" + String.format("%03d", hd.getSoThuTu()));
        l.add("Ngày: " + FormatUtil.ngayGio(hd.getNgayLap()));
        l.add("Thu ngân: " + (hd.getNhanVien() == null ? "" : hd.getNhanVien().getHoTen()));
        l.add(gach());
        l.add(haiBen("Món", "T.tiền"));
        for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
            l.add(haiBen(ct.getSoLuong() + " x " + ct.getMon().getTenMon(), FormatUtil.tien(ct.tinhThanhTien())));
            String mt = ct.moTaTuyChon();
            if (!mt.isEmpty()) l.add("    " + mt);
            if (ct.getGhiChu() != null && !ct.getGhiChu().trim().isEmpty()) l.add("    Ghi chú: " + ct.getGhiChu().trim());
        }
        l.add(gach());
        l.add(haiBen("Tạm tính", FormatUtil.tien(hd.tinhTamTinh())));
        if (hd.getGiamGia() > 0) {
            String km = hd.getKhuyenMai() == null ? "" : " (" + hd.getKhuyenMai().getMaKM() + ")";
            l.add(haiBen("Giảm giá" + km, "-" + FormatUtil.tien(hd.getGiamGia())));
        }
        l.add(haiBen("TỔNG CỘNG", FormatUtil.tien(hd.tinhTongTien())));
        l.add(haiBen("Thanh toán", HoaDon.tenPhuongThuc(hd.getPhuongThucTT())));
        if (khachDua >= 0) {
            l.add(haiBen("Khách đưa", FormatUtil.tien(khachDua)));
            l.add(haiBen("Tiền thừa", FormatUtil.tien(tienThua)));
        }
        l.add(gach());
        l.add(giua("Cảm ơn quý khách!"));
        l.add(giua("Hẹn gặp lại"));
        return l;
    }

    /** Mở hộp thoại in. Muốn ra file PDF thì chọn máy in "Microsoft Print to PDF". */
    public static void inVanBan(List<String> dong, Component parent) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Hóa đơn");
        final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 10);
        job.setPrintable((g, pf, page) -> {
            FontMetrics fm = g.getFontMetrics(font);
            int perPage = (int) (pf.getImageableHeight() / fm.getHeight());
            int first = page * perPage;
            if (first >= dong.size()) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(pf.getImageableX(), pf.getImageableY());
            g2.setFont(font);
            g2.setColor(Color.BLACK);
            int y = fm.getAscent();
            for (int i = first; i < Math.min(dong.size(), first + perPage); i++) {
                g2.drawString(dong.get(i), 0, y);
                y += fm.getHeight();
            }
            return Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(parent, "Không in được: " + e.getMessage(), "Lỗi in", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
