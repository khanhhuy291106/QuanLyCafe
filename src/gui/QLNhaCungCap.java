package gui;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import dao.NhaCungCap_DAO;
import entity.NhaCungCap;

// Quản lý nhà cung cấp (nằm trong TabKho)
public class QLNhaCungCap extends JPanel implements TaiLai {
    private NhaCungCap_DAO dao = new NhaCungCap_DAO();
    private ArrayList<NhaCungCap> ds = new ArrayList<>();
    private JTextField txtMa = TienIch.oMa();
    private JTextField txtTen = new JTextField(10);
    private JTextField txtSdt = new JTextField(10);
    private JTextField txtDiaChi = new JTextField(10);
    private JTextField txtTim = new JTextField(18);
    private DefaultTableModel mdl = TienIch.moHinh("Mã NCC", "Tên nhà cung cấp", "SĐT", "Địa chỉ");
    private JTable tbl = TienIch.bang(mdl);
    private JButton btnThem = TienIch.nut("Thêm", TienIch.GREEN);
    private JButton btnSua = TienIch.nut("Sửa", TienIch.BLUE);
    private JButton btnXoa = TienIch.nut("Xóa", TienIch.RED);
    private JButton btnLamMoi = TienIch.nut("Làm mới", new Color(0xB8A890));
    private JButton btnTim = TienIch.nut("Tìm kiếm", TienIch.BROWN);

    public QLNhaCungCap() {
        JPanel form = new JPanel(new GridBagLayout());
        TienIch.themDong(form, 0, "Mã NCC", txtMa);
        TienIch.themDong(form, 1, "Tên", txtTen);
        TienIch.themDong(form, 2, "SĐT", txtSdt);
        TienIch.themDong(form, 3, "Địa chỉ", txtDiaChi);
        TienIch.themKhoangTrong(form, 4);
        JPanel trai = TienIch.theForm("Thông tin nhà cung cấp", 320, form, TienIch.pnlNutCrud(btnThem, btnSua, btnXoa, btnLamMoi));
        TienIch.doRong(tbl, 70, 220, 110, 240);
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
        ds = dao.timKiem(txtTim.getText().trim());
        mdl.setRowCount(0);
        for (NhaCungCap n : ds) {
            mdl.addRow(new Object[] { n.getMaNCC(), n.getTenNCC(), n.getSdt(), n.getDiaChi() });
        }
    }

    private void dienForm(int row) {
        NhaCungCap n = ds.get(row);
        txtMa.setText(n.getMaNCC());
        txtTen.setText(n.getTenNCC());
        txtSdt.setText(n.getSdt() == null ? "" : n.getSdt());
        txtDiaChi.setText(n.getDiaChi() == null ? "" : n.getDiaChi());
    }

    private void xoaForm() {
        txtMa.setText(TienIch.CHUA_CO_MA);
        txtTen.setText("");
        txtSdt.setText("");
        txtDiaChi.setText("");
    }

    private void lamMoi() {
        tbl.clearSelection();
        txtTim.setText("");
        xoaForm();
        taiLai();
    }

    private NhaCungCap docForm(String ma) {
        String ten = txtTen.getText().trim().replaceAll("\\s+", " ");
        if (ten.isEmpty() || ten.length() > 100) {
            JOptionPane.showMessageDialog(this, "Tên nhà cung cấp không được để trống và tối đa 100 ký tự.");
            return null;
        }
        String sdt = txtSdt.getText().trim();
        if (!sdt.matches("^0\\d{9,10}$")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại gồm 10 hoặc 11 chữ số và bắt đầu bằng số 0.");
            return null;
        }
        String diaChi = txtDiaChi.getText().trim();
        if (diaChi.length() > 200) {
            JOptionPane.showMessageDialog(this, "Địa chỉ tối đa 200 ký tự.");
            return null;
        }
        return new NhaCungCap(ma, ten, sdt, diaChi);
    }

    private void them() {
        NhaCungCap n = docForm(null);
        if (n == null) return;
        if (dao.them(n)) lamMoi();
        else JOptionPane.showMessageDialog(this, "Thêm thất bại!");
    }

    private void sua() {
        int r = tbl.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng trong bảng trước.");
            return;
        }
        NhaCungCap n = docForm(ds.get(r).getMaNCC());
        if (n == null) return;
        if (dao.sua(n)) lamMoi();
        else JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
    }

    private void xoa() {
        int r = tbl.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng trong bảng trước.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa dòng đang chọn?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        if (dao.xoa(ds.get(r).getMaNCC())) lamMoi();
        else JOptionPane.showMessageDialog(this, "Nhà cung cấp này đã có phiếu nhập nên không thể xóa.");
    }
}
