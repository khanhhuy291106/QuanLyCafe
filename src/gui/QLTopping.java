package gui;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import dao.Topping_DAO;
import entity.Topping;

// Quản lý topping (nằm trong TabThucDon)
public class QLTopping extends JPanel implements TaiLai {
    private Topping_DAO dao = new Topping_DAO();
    private ArrayList<Topping> ds = new ArrayList<>();
    private JTextField txtMa = TienIch.oMa();
    private JTextField txtTen = new JTextField(10);
    private JTextField txtGia = new JTextField(10);
    private JComboBox<String> cboTT = new JComboBox<>(new String[] { "Đang bán", "Ngừng bán" });
    private JTextField txtTim = new JTextField(18);
    private DefaultTableModel mdl = TienIch.moHinh("Mã", "Tên topping", "Giá", "Trạng thái");
    private JTable tbl = TienIch.bang(mdl);
    private JButton btnThem = TienIch.nut("Thêm", TienIch.GREEN);
    private JButton btnSua = TienIch.nut("Sửa", TienIch.BLUE);
    private JButton btnXoa = TienIch.nut("Xóa", TienIch.RED);
    private JButton btnLamMoi = TienIch.nut("Làm mới", new Color(0xB8A890));
    private JButton btnTim = TienIch.nut("Tìm kiếm", TienIch.BROWN);

    public QLTopping() {
        JPanel form = new JPanel(new GridBagLayout());
        TienIch.themDong(form, 0, "Mã topping", txtMa);
        TienIch.themDong(form, 1, "Tên topping", txtTen);
        TienIch.themDong(form, 2, "Giá (đ)", txtGia);
        TienIch.themDong(form, 3, "Trạng thái", cboTT);
        TienIch.themKhoangTrong(form, 4);
        JPanel trai = TienIch.theForm("Thông tin topping", 320, form, TienIch.pnlNutCrud(btnThem, btnSua, btnXoa, btnLamMoi));
        TienIch.canPhai(tbl, 2);
        TienIch.tomauTrangThai(tbl, 3);
        TienIch.doRong(tbl, 80, 220, 100, 120);
        TienIch.xepCrud(this, trai, TienIch.phaiBang(TienIch.thanhTim(txtTim, btnTim), tbl, null));

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
        for (Topping t : dao.getAll()) {
            if (k.isEmpty() || t.getTenTopping().toLowerCase().contains(k)) ds.add(t);
        }
        mdl.setRowCount(0);
        for (Topping t : ds) {
            mdl.addRow(new Object[] { t.getMaTopping(), t.getTenTopping(), TienIch.tien(t.getGia()), t.isTrangThai() ? "Đang bán" : "Ngừng bán" });
        }
    }

    private void dienForm(int row) {
        Topping t = ds.get(row);
        txtMa.setText(t.getMaTopping());
        txtTen.setText(t.getTenTopping());
        txtGia.setText(TienIch.tien(t.getGia()));
        cboTT.setSelectedIndex(t.isTrangThai() ? 0 : 1);
    }

    private void xoaForm() {
        txtMa.setText(TienIch.CHUA_CO_MA);
        txtTen.setText("");
        txtGia.setText("");
        cboTT.setSelectedIndex(0);
    }

    private void lamMoi() {
        tbl.clearSelection();
        txtTim.setText("");
        xoaForm();
        taiLai();
    }

    private Topping docForm(String ma) {
        String ten = txtTen.getText().trim().replaceAll("\\s+", " ");
        if (ten.isEmpty() || ten.length() > 50) {
            JOptionPane.showMessageDialog(this, "Tên topping không được để trống và tối đa 50 ký tự.");
            return null;
        }
        double gia = TienIch.parseTien(txtGia.getText());
        if (gia < 0) {
            JOptionPane.showMessageDialog(this, "Giá topping phải là số nguyên không âm.");
            return null;
        }
        return new Topping(ma, ten, gia, cboTT.getSelectedIndex() == 0);
    }

    private void them() {
        Topping t = docForm(null);
        if (t == null) return;
        if (dao.them(t)) {
            lamMoi();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thất bại! (tên topping có thể bị trùng)");
        }
    }

    private void sua() {
        int r = tbl.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng trong bảng trước.");
            return;
        }
        Topping t = docForm(ds.get(r).getMaTopping());
        if (t == null) return;
        if (dao.sua(t)) {
            lamMoi();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại! (tên topping có thể bị trùng)");
        }
    }

    private void xoa() {
        int r = tbl.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng trong bảng trước.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa dòng đang chọn?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        if (dao.xoa(ds.get(r).getMaTopping())) {
            lamMoi();
        } else {
            JOptionPane.showMessageDialog(this, "Topping này đã được dùng trong hóa đơn nên không thể xóa. Hãy đổi trạng thái sang “Ngừng bán”.");
        }
    }
}
