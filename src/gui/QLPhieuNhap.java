package gui;

import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.jdatepicker.impl.JDatePickerImpl;
import dao.NhaCungCap_DAO;
import dao.PhieuNhap_DAO;
import entity.*;

// Lập phiếu nhập kho (nằm trong TabKho): lưu phiếu và cộng tồn kho
public class QLPhieuNhap extends JPanel implements TaiLai {
    private PhieuNhap_DAO dao = new PhieuNhap_DAO();
    private NhaCungCap_DAO ncc_dao = new NhaCungCap_DAO();
    private NhanVien nv;
    private ArrayList<ChiTietPhieuNhap> ct = new ArrayList<>();
    private JTextField txtMa = TienIch.oMa();
    private JTextField txtNV = TienIch.oMa();
    private JDatePickerImpl dpNgay = DinhDangNgay.taoDatePicker(LocalDate.now());
    private JComboBox<NhaCungCap> cboNCC = new JComboBox<>();
    private JComboBox<NguyenLieu> cboNL = new JComboBox<>();
    private JTextField txtSL = new JTextField(5);
    private JTextField txtGia = new JTextField(8);
    private DefaultTableModel mdl = TienIch.moHinh("Nguyên liệu", "ĐVT", "Số lượng", "Đơn giá", "Thành tiền");
    private JTable tbl = TienIch.bang(mdl);
    private JLabel lblTong = new JLabel("0đ");
    private BangTonKho bangTon = new BangTonKho();

    public QLPhieuNhap(NhanVien nv) {
        this.nv = nv;
        setLayout(new BorderLayout(12, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(TienIch.BG);
        txtNV.setText(nv.getHoTen());

        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(TienIch.LINE),
                BorderFactory.createEmptyBorder(12, 14, 14, 14)));

        // phần nhập thông tin phiếu
        JPanel pnlTren = new JPanel(new BorderLayout(0, 8));
        pnlTren.setOpaque(false);
        pnlTren.add(TienIch.tieuDe("Lập phiếu nhập"), BorderLayout.NORTH);
        JPanel dong1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dong1.setOpaque(false);
        txtMa.setPreferredSize(new Dimension(90, 30));
        txtNV.setPreferredSize(new Dimension(150, 30));
        dong1.add(new JLabel("Mã phiếu"));
        dong1.add(txtMa);
        dong1.add(new JLabel("Ngày nhập"));
        dong1.add(dpNgay);
        dong1.add(new JLabel("Nhân viên"));
        dong1.add(txtNV);
        JPanel dong2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dong2.setOpaque(false);
        cboNCC.setPreferredSize(new Dimension(260, 30));
        dong2.add(new JLabel("Nhà cung cấp"));
        dong2.add(cboNCC);
        JPanel dong3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dong3.setOpaque(false);
        cboNL.setPreferredSize(new Dimension(190, 30));
        JButton btnThem = TienIch.nut("+ Thêm", TienIch.GREEN);
        dong3.add(new JLabel("Nguyên liệu"));
        dong3.add(cboNL);
        dong3.add(new JLabel("SL"));
        dong3.add(txtSL);
        dong3.add(new JLabel("Đơn giá"));
        dong3.add(txtGia);
        dong3.add(btnThem);
        JPanel dongs = new JPanel(new GridLayout(0, 1, 0, 6));
        dongs.setOpaque(false);
        dongs.add(dong1);
        dongs.add(dong2);
        dongs.add(dong3);
        pnlTren.add(dongs, BorderLayout.CENTER);
        card.add(pnlTren, BorderLayout.NORTH);

        TienIch.canGiua(tbl, 1);
        TienIch.canPhai(tbl, 2, 3, 4);
        TienIch.doRong(tbl, 190, 60, 90, 100, 120);
        card.add(TienIch.cuon(tbl), BorderLayout.CENTER);

        // phần tổng tiền và nút
        JPanel pnlDuoi = new JPanel(new BorderLayout(0, 8));
        pnlDuoi.setOpaque(false);
        JPanel pnlTong = new JPanel(new BorderLayout());
        pnlTong.setOpaque(false);
        JButton btnXoaDong = TienIch.nutPhu("Xóa dòng chọn");
        pnlTong.add(btnXoaDong, BorderLayout.WEST);
        JPanel pnlSoTien = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlSoTien.setOpaque(false);
        JLabel lblNhan = new JLabel("Tổng tiền phiếu");
        lblNhan.setFont(TienIch.FONT_B);
        lblTong.setFont(new Font(TienIch.FONT_NAME, Font.BOLD, 20));
        lblTong.setForeground(TienIch.GREEN);
        pnlSoTien.add(lblNhan);
        pnlSoTien.add(lblTong);
        pnlTong.add(pnlSoTien, BorderLayout.EAST);
        pnlDuoi.add(pnlTong, BorderLayout.NORTH);
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlNut.setOpaque(false);
        JButton btnLuu = TienIch.nut("Lưu phiếu nhập", TienIch.GREEN);
        btnLuu.setPreferredSize(new Dimension(150, 36));
        JButton btnLamMoi = TienIch.nutPhu("Làm mới");
        JButton btnLichSu = TienIch.nut("Lịch sử phiếu nhập", TienIch.BROWN);
        pnlNut.add(btnLuu);
        pnlNut.add(btnLamMoi);
        pnlNut.add(btnLichSu);
        pnlDuoi.add(pnlNut, BorderLayout.SOUTH);
        card.add(pnlDuoi, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
        add(bangTon, BorderLayout.EAST);

        btnThem.addActionListener(e -> themDong());
        btnXoaDong.addActionListener(e -> {
            int r = tbl.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Hãy chọn dòng cần xóa trong bảng.");
                return;
            }
            ct.remove(r);
            hienBang();
        });
        btnLuu.addActionListener(e -> luuPhieu());
        btnLamMoi.addActionListener(e -> {
            ct.clear();
            hienBang();
            taiLai();
        });
        btnLichSu.addActionListener(e -> new LichSuPhieu(SwingUtilities.getWindowAncestor(this), true).setVisible(true));
    }

    @Override
    public void taiLai() {
        int iNCC = cboNCC.getSelectedIndex();
        int iNL = cboNL.getSelectedIndex();
        cboNCC.removeAllItems();
        for (NhaCungCap n : ncc_dao.getAll()) {
            cboNCC.addItem(n);
        }
        cboNL.removeAllItems();
        for (NguyenLieu n : bangTon.taiLai()) {
            cboNL.addItem(n);
        }
        if (iNCC >= 0 && iNCC < cboNCC.getItemCount()) cboNCC.setSelectedIndex(iNCC);
        if (iNL >= 0 && iNL < cboNL.getItemCount()) cboNL.setSelectedIndex(iNL);
        txtMa.setText(dao.taoMa());
    }

    private void themDong() {
        NguyenLieu nl = (NguyenLieu) cboNL.getSelectedItem();
        if (nl == null) {
            JOptionPane.showMessageDialog(this, "Chưa có nguyên liệu. Hãy thêm nguyên liệu ở thẻ Tồn kho trước.");
            return;
        }
        double sl = TienIch.parseSoThuc(txtSL.getText());
        if (sl <= 0) {
            JOptionPane.showMessageDialog(this, "Số lượng phải là số lớn hơn 0 (tối đa 2 chữ số thập phân), ví dụ 10 hoặc 2,5.");
            return;
        }
        double gia = TienIch.parseTien(txtGia.getText());
        if (gia < 0) {
            JOptionPane.showMessageDialog(this, "Đơn giá phải là số nguyên không âm, ví dụ 180000 hoặc 180.000.");
            return;
        }
        // Nguyên liệu đã có trong phiếu thì cộng dồn số lượng
        for (ChiTietPhieuNhap x : ct) {
            if (x.getNguyenLieu().getMaNL().equals(nl.getMaNL())) {
                x.setSoLuong(x.getSoLuong() + sl);
                x.setDonGia(gia);
                hienBang();
                return;
            }
        }
        ChiTietPhieuNhap d = new ChiTietPhieuNhap();
        d.setNguyenLieu(nl);
        d.setSoLuong(sl);
        d.setDonGia(gia);
        ct.add(d);
        hienBang();
        txtSL.setText("");
        txtGia.setText("");
    }

    private void hienBang() {
        mdl.setRowCount(0);
        double tong = 0;
        for (ChiTietPhieuNhap d : ct) {
            tong += d.thanhTien();
            mdl.addRow(new Object[] { d.getNguyenLieu().getTenNL(), d.getNguyenLieu().getDonViTinh(), TienIch.soThuc(d.getSoLuong()),
                    TienIch.tien(d.getDonGia()), TienIch.tien(d.thanhTien()) });
        }
        lblTong.setText(TienIch.tienD(tong));
    }

    private void luuPhieu() {
        NhaCungCap ncc = (NhaCungCap) cboNCC.getSelectedItem();
        if (ncc == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn nhà cung cấp (thêm ở thẻ Nhà cung cấp nếu chưa có).");
            return;
        }
        LocalDate ngay = DinhDangNgay.layNgay(dpNgay);
        if (ngay == null || ngay.isAfter(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Ngày nhập không được để trống hoặc ở tương lai.");
            return;
        }
        if (ct.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phiếu nhập chưa có nguyên liệu nào.");
            return;
        }
        PhieuNhap pn = new PhieuNhap();
        pn.setNgayNhap(ngay);
        pn.setNhanVien(nv);
        pn.setNhaCungCap(ncc);
        pn.setDsChiTiet(new ArrayList<>(ct));
        if (dao.luu(pn)) {
            JOptionPane.showMessageDialog(this, "Đã lưu phiếu " + pn.getMaPN() + ", tổng tiền " + TienIch.tienD(pn.getTongTien()) + ". Tồn kho đã được cộng thêm.");
            ct.clear();
            hienBang();
            taiLai();
        } else {
            JOptionPane.showMessageDialog(this, "Lưu phiếu nhập thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
