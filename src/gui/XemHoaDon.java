package gui;

import java.awt.*;
import java.awt.print.*;
import java.util.ArrayList;
import javax.swing.*;
import entity.ChiTietHoaDon;
import entity.HoaDon;

// Xem trước hóa đơn dạng giấy in và cho phép in
public class XemHoaDon extends JDialog {
    // Thông tin quán in trên hóa đơn: sửa tại đây
    private static final String TEN_QUAN = "CAFÉ ABC";
    private static final String DIA_CHI = "12 Đường Mẫu, TP. Hồ Chí Minh";
    private static final String DIEN_THOAI = "ĐT: 0900 000 000";
    private static final int RONG = 40;

    private ArrayList<String> dong;

    // khachDua âm = không hiện dòng khách đưa/tiền thừa (in lại hóa đơn cũ)
    public XemHoaDon(Window owner, HoaDon hd, double khachDua, double tienThua) {
        super(owner, "Hóa đơn " + hd.getMaHD(), ModalityType.APPLICATION_MODAL);
        dong = noiDungHoaDon(hd, khachDua, tienThua);
        setSize(440, 620);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(0, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JTextArea ta = new JTextArea(String.join("\n", dong));
        ta.setEditable(false);
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        ta.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        add(TienIch.cuon(ta), BorderLayout.CENTER);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnDong = TienIch.nutPhu("Đóng");
        JButton btnIn = TienIch.nut("In hóa đơn", TienIch.BROWN);
        pnlNut.add(btnDong);
        pnlNut.add(btnIn);
        add(pnlNut, BorderLayout.SOUTH);

        btnDong.addActionListener(e -> dispose());
        btnIn.addActionListener(e -> in());
    }

    private String pad(String s, int w) {
        if (s.length() >= w) return s.substring(0, w);
        return s + " ".repeat(w - s.length());
    }

    private String giua(String s) {
        return " ".repeat(Math.max(0, (RONG - s.length()) / 2)) + s;
    }

    private String haiBen(String trai, String phai) {
        int cach = RONG - trai.length() - phai.length();
        if (cach < 1) {
            trai = trai.substring(0, Math.max(0, trai.length() + cach - 1));
            cach = 1;
        }
        return trai + " ".repeat(cach) + phai;
    }

    private ArrayList<String> noiDungHoaDon(HoaDon hd, double khachDua, double tienThua) {
        String gach = "-".repeat(RONG);
        ArrayList<String> l = new ArrayList<>();
        l.add(giua(TEN_QUAN));
        l.add(giua(DIA_CHI));
        l.add(giua(DIEN_THOAI));
        l.add(gach);
        l.add(giua("HÓA ĐƠN BÁN HÀNG"));
        l.add("Số: " + hd.getMaHD() + "   STT: #" + String.format("%03d", hd.getSoThuTu()));
        l.add("Ngày: " + TienIch.ngayGio(hd.getNgayLap()));
        l.add("Thu ngân: " + (hd.getNhanVien() == null ? "" : hd.getNhanVien().getHoTen()));
        l.add(gach);
        l.add(haiBen("Món", "T.tiền"));
        for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
            l.add(haiBen(ct.getSoLuong() + " x " + ct.getMon().getTenMon(), TienIch.tien(ct.tinhThanhTien())));
            String moTa = ct.moTaTuyChon();
            if (!moTa.isEmpty()) l.add("    " + moTa);
            if (ct.getGhiChu() != null && !ct.getGhiChu().trim().isEmpty()) l.add("    Ghi chú: " + ct.getGhiChu().trim());
        }
        l.add(gach);
        l.add(haiBen("Tạm tính", TienIch.tien(hd.tinhTamTinh())));
        if (hd.getGiamGia() > 0) {
            String km = hd.getKhuyenMai() == null ? "" : " (" + hd.getKhuyenMai().getMaKM() + ")";
            l.add(haiBen("Giảm giá" + km, "-" + TienIch.tien(hd.getGiamGia())));
        }
        l.add(haiBen("TỔNG CỘNG", TienIch.tien(hd.tinhTongTien())));
        l.add(haiBen("Thanh toán", HoaDon.tenPhuongThuc(hd.getPhuongThucTT())));
        if (khachDua >= 0) {
            l.add(haiBen("Khách đưa", TienIch.tien(khachDua)));
            l.add(haiBen("Tiền thừa", TienIch.tien(tienThua)));
        }
        l.add(gach);
        l.add(giua("Cảm ơn quý khách!"));
        l.add(giua("Hẹn gặp lại"));
        return l;
    }

    // Mở hộp thoại in. Muốn ra file PDF thì chọn máy in "Microsoft Print to PDF".
    private void in() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Hóa đơn");
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 10);
        job.setPrintable((g, pf, trang) -> {
            FontMetrics fm = g.getFontMetrics(font);
            int soDongMoiTrang = (int) (pf.getImageableHeight() / fm.getHeight());
            int dau = trang * soDongMoiTrang;
            if (dau >= dong.size()) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(pf.getImageableX(), pf.getImageableY());
            g2.setFont(font);
            g2.setColor(Color.BLACK);
            int y = fm.getAscent();
            for (int i = dau; i < Math.min(dong.size(), dau + soDongMoiTrang); i++) {
                g2.drawString(dong.get(i), 0, y);
                y += fm.getHeight();
            }
            return Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Không in được: " + e.getMessage(), "Lỗi in", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
