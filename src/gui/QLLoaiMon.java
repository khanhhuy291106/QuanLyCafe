package gui;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import dao.LoaiMon_DAO;
import entity.LoaiMon;

// Quản lý loại món (nằm trong TabThucDon)
public class QLLoaiMon extends JPanel implements TaiLai {
    private LoaiMon_DAO dao = new LoaiMon_DAO();
    private ArrayList<LoaiMon> ds = new ArrayList<>();
    private JTextField txtMa = TienIch.oMa();
    private JTextField txtTen = new JTextField(10);
    private JTextField txtTim = new JTextField(18);
    private DefaultTableModel mdl = TienIch.moHinh("Mã loại", "Tên loại");
    private JTable tbl = TienIch.bang(mdl);
    private JButton btnThem = TienIch.nut("Thêm", TienIch.GREEN);
    private JButton btnSua = TienIch.nut("Sửa", TienIch.BLUE);
    private JButton btnXoa = TienIch.nut("Xóa", TienIch.RED);
    private JButton btnLamMoi = TienIch.nut("Làm mới", new Color(0xB8A890));
    private JButton btnTim = TienIch.nut("Tìm kiếm", TienIch.BROWN);

    public QLLoaiMon() {
        JPanel form = new JPanel(new GridBagLayout());
        TienIch.themDong(form, 0, "Mã loại", txtMa);
        TienIch.themDong(form, 1, "Tên loại", txtTen);
        TienIch.themKhoangTrong(form, 2);
        JPanel trai = TienIch.theForm("Thông tin loại món", 320, form, TienIch.pnlNutCrud(btnThem, btnSua, btnXoa, btnLamMoi));
        TienIch.doRong(tbl, 100, 300);
        JPanel phai = TienIch.phaiBang(TienIch.thanhTim(txtTim, btnTim), tbl,
                TienIch.ghiChu("Loại “Bánh” là loại đặc biệt: món thuộc loại này không có size, đường, đá và topping."));
        TienIch.xepCrud(this, trai, phai);

        tbl.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tbl.getSelectedRow() >= 0) dienForm(tbl.getSelectedRow());
        });
        btnTim.addActionListener(e -> taiLai());
        txtTim.addActionListener(e -> taiLai());
        btnThem.addActionListener(e -> them());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());
        btnLamMoi.addActionListener(e -> lamMoi());
        xoaForm();
    }

    @Override
    public void taiLai() {
        String k = txtTim.getText().trim().toLowerCase();
        ds = new ArrayList<>();
        for (LoaiMon l : dao.getAll()) {
            if (k.isEmpty() || l.getTenLoai().toLowerCase().contains(k)) ds.add(l);
        }
        mdl.setRowCount(0);
        for (LoaiMon l : ds) {
            mdl.addRow(new Object[] { l.getMaLoai(), l.getTenLoai() });
        }
    }

    private void dienForm(int row) {
        txtMa.setText(ds.get(row).getMaLoai());
        txtTen.setText(ds.get(row).getTenLoai());
    }

    private void xoaForm() {
        txtMa.setText(TienIch.CHUA_CO_MA);
        txtTen.setText("");
    }

    private void lamMoi() {
        tbl.clearSelection();
        txtTim.setText("");
        xoaForm();
        taiLai();
    }

    private String docTen() {
        String t = txtTen.getText().trim().replaceAll("\\s+", " ");
        if (t.isEmpty() || t.length() > 50) {
            JOptionPane.showMessageDialog(this, "Tên loại không được để trống và tối đa 50 ký tự.");
            return null;
        }
        return t;
    }

    private void them() {
        String t = docTen();
        if (t == null) return;
        if (dao.them(new LoaiMon(null, t))) {
            lamMoi();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thất bại! (tên loại có thể bị trùng)");
        }
    }

    private void sua() {
        int r = tbl.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng trong bảng trước.");
            return;
        }
        String t = docTen();
        if (t == null) return;
        if (dao.sua(new LoaiMon(ds.get(r).getMaLoai(), t))) {
            lamMoi();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại! (tên loại có thể bị trùng)");
        }
    }

    private void xoa() {
        int r = tbl.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng trong bảng trước.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa dòng đang chọn?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        if (dao.xoa(ds.get(r).getMaLoai())) {
            lamMoi();
        } else {
            JOptionPane.showMessageDialog(this, "Loại món này đang có món thuộc về nó nên không thể xóa.");
        }
    }
}
