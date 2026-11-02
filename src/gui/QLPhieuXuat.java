package gui;

import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.jdatepicker.impl.JDatePickerImpl;
import dao.PhieuXuat_DAO;
import entity.*;

// Lập phiếu xuất kho (hao hụt, đổ bỏ, kiểm kê) (nằm trong TabKho): lưu phiếu và trừ tồn kho
public class QLPhieuXuat extends JPanel implements TaiLai {
    private PhieuXuat_DAO dao = new PhieuXuat_DAO();
    private NhanVien nv;
    private ArrayList<ChiTietPhieuXuat> ct = new ArrayList<>();
    private JTextField txtMa = TienIch.oMa();
    private JTextField txtNV = TienIch.oMa();
    private JDatePickerImpl dpNgay = DinhDangNgay.taoDatePicker(LocalDate.now());
    private JTextField txtLyDo = new JTextField(30);
    private JComboBox<NguyenLieu> cboNL = new JComboBox<>();
    private JTextField txtSL = new JTextField(5);
    private DefaultTableModel mdl = TienIch.moHinh("Nguyên liệu", "ĐVT", "Số lượng xuất", "Tồn hiện tại");
    private JTable tbl = TienIch.bang(mdl);
    private BangTonKho bangTon = new BangTonKho();

    public QLPhieuXuat(NhanVien nv) {
        this.nv = nv;
        setLayout(new BorderLayout(12, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(TienIch.BG);
        txtNV.setText(nv.getHoTen());

        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(TienIch.LINE),
                BorderFactory.createEmptyBorder(12, 14, 14, 14)));

        JPanel pnlTren = new JPanel(new BorderLayout(0, 8));
        pnlTren.setOpaque(false);
        pnlTren.add(TienIch.tieuDe("Lập phiếu xuất (hao hụt, đổ bỏ, kiểm kê)"), BorderLayout.NORTH);
        JPanel dong1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dong1.setOpaque(false);
        txtMa.setPreferredSize(new Dimension(90, 30));
        txtNV.setPreferredSize(new Dimension(150, 30));
        dong1.add(new JLabel("Mã phiếu"));
        dong1.add(txtMa);
        dong1.add(new JLabel("Ngày xuất"));
        dong1.add(dpNgay);
        dong1.add(new JLabel("Nhân viên"));
        dong1.add(txtNV);
        JPanel dong2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dong2.setOpaque(false);
        txtLyDo.setPreferredSize(new Dimension(380, 30));
        dong2.add(new JLabel("Lý do xuất"));
        dong2.add(txtLyDo);
        JPanel dong3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dong3.setOpaque(false);
        cboNL.setPreferredSize(new Dimension(190, 30));
        JButton btnThem = TienIch.nut("+ Thêm", TienIch.GREEN);
        dong3.add(new JLabel("Nguyên liệu"));
        dong3.add(cboNL);
        dong3.add(new JLabel("SL xuất"));
        dong3.add(txtSL);
        dong3.add(btnThem);
        JPanel dongs = new JPanel(new GridLayout(0, 1, 0, 6));
        dongs.setOpaque(false);
        dongs.add(dong1);
        dongs.add(dong2);
        dongs.add(dong3);
        pnlTren.add(dongs, BorderLayout.CENTER);
        card.add(pnlTren, BorderLayout.NORTH);

        TienIch.canGiua(tbl, 1);
        TienIch.canPhai(tbl, 2, 3);
        TienIch.doRong(tbl, 220, 70, 120, 120);
        card.add(TienIch.cuon(tbl), BorderLayout.CENTER);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlNut.setOpaque(false);
        JButton btnXoaDong = TienIch.nutPhu("Xóa dòng chọn");
        JButton btnLuu = TienIch.nut("Lưu phiếu xuất", TienIch.GREEN);
        btnLuu.setPreferredSize(new Dimension(150, 36));
        JButton btnLamMoi = TienIch.nutPhu("Làm mới");
        JButton btnLichSu = TienIch.nut("Lịch sử phiếu xuất", TienIch.BROWN);
        pnlNut.add(btnLuu);
        pnlNut.add(btnXoaDong);
        pnlNut.add(btnLamMoi);
        pnlNut.add(btnLichSu);
        card.add(pnlNut, BorderLayout.SOUTH);

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
            txtLyDo.setText("");
            hienBang();
            taiLai();
        });
        btnLichSu.addActionListener(e -> new LichSuPhieu(SwingUtilities.getWindowAncestor(this), false).setVisible(true));
    }

    @Override
    public void taiLai() {
        int iNL = cboNL.getSelectedIndex();
        cboNL.removeAllItems();
        for (NguyenLieu n : bangTon.taiLai()) {
            cboNL.addItem(n);
        }
        if (iNL >= 0 && iNL < cboNL.getItemCount()) cboNL.setSelectedIndex(iNL);
        txtMa.setText(dao.taoMa());
    }

    private void themDong() {
        NguyenLieu nl = (NguyenLieu) cboNL.getSelectedItem();
        if (nl == null) {
            JOptionPane.showMessageDialog(this, "Chưa có nguyên liệu để xuất.");
            return;
        }
        double sl = TienIch.parseSoThuc(txtSL.getText());
        if (sl <= 0) {
            JOptionPane.showMessageDialog(this, "Số lượng xuất phải là số lớn hơn 0 (tối đa 2 chữ số thập phân).");
            return;
        }
        // Nguyên liệu đã có trong phiếu thì cộng dồn; tổng không được vượt tồn kho
        ChiTietPhieuXuat daCo = null;
        for (ChiTietPhieuXuat x : ct) {
            if (x.getNguyenLieu().getMaNL().equals(nl.getMaNL())) daCo = x;
        }
        double tongXuat = sl + (daCo == null ? 0 : daCo.getSoLuong());
        if (tongXuat > nl.getSoLuongTon()) {
            JOptionPane.showMessageDialog(this, "Không đủ tồn kho. " + nl.getTenNL() + " chỉ còn "
                    + TienIch.soThuc(nl.getSoLuongTon()) + " " + nl.getDonViTinh() + ".");
            return;
        }
        if (daCo != null) {
            daCo.setSoLuong(tongXuat);
        } else {
            ChiTietPhieuXuat d = new ChiTietPhieuXuat();
            d.setNguyenLieu(nl);
            d.setSoLuong(sl);
            ct.add(d);
        }
        hienBang();
        txtSL.setText("");
    }

    private void hienBang() {
        mdl.setRowCount(0);
        for (ChiTietPhieuXuat d : ct) {
            mdl.addRow(new Object[] { d.getNguyenLieu().getTenNL(), d.getNguyenLieu().getDonViTinh(), TienIch.soThuc(d.getSoLuong()),
                    TienIch.soThuc(d.getNguyenLieu().getSoLuongTon()) });
        }
    }

    private void luuPhieu() {
        LocalDate ngay = DinhDangNgay.layNgay(dpNgay);
        if (ngay == null || ngay.isAfter(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Ngày xuất không được để trống hoặc ở tương lai.");
            return;
        }
        String lyDo = txtLyDo.getText().trim();
        if (lyDo.isEmpty() || lyDo.length() > 200) {
            JOptionPane.showMessageDialog(this, "Hãy nhập lý do xuất (tối đa 200 ký tự).");
            return;
        }
        if (ct.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phiếu xuất chưa có nguyên liệu nào.");
            return;
        }
        PhieuXuat px = new PhieuXuat();
        px.setNgayXuat(ngay);
        px.setNhanVien(nv);
        px.setLyDo(lyDo);
        px.setDsChiTiet(new ArrayList<>(ct));
        if (dao.luu(px)) {
            JOptionPane.showMessageDialog(this, "Đã lưu phiếu " + px.getMaPX() + ". Tồn kho đã được trừ.");
            ct.clear();
            txtLyDo.setText("");
            hienBang();
            taiLai();
        } else {
            JOptionPane.showMessageDialog(this, "Lưu phiếu xuất thất bại! (có thể không đủ tồn kho)", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
