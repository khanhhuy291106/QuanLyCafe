package gui;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import dao.Size_DAO;
import entity.Size;

// Quản lý size (nằm trong TabThucDon)
public class QLSize extends JPanel implements TaiLai {
    private Size_DAO dao = new Size_DAO();
    private ArrayList<Size> ds = new ArrayList<>();
    private JTextField txtMa = TienIch.oMa();
    private JTextField txtTen = new JTextField(10);
    private JTextField txtPhuThu = new JTextField(10);
    private JTextField txtTim = new JTextField(18);
    private DefaultTableModel mdl = TienIch.moHinh("Mã size", "Tên size", "Phụ thu");
    private JTable tbl = TienIch.bang(mdl);
    private JButton btnThem = TienIch.nut("Thêm", TienIch.GREEN);
    private JButton btnSua = TienIch.nut("Sửa", TienIch.BLUE);
    private JButton btnXoa = TienIch.nut("Xóa", TienIch.RED);
    private JButton btnLamMoi = TienIch.nut("Làm mới", new Color(0xB8A890));
    private JButton btnTim = TienIch.nut("Tìm kiếm", TienIch.BROWN);

    public QLSize() {
        JPanel form = new JPanel(new GridBagLayout());
        TienIch.themDong(form, 0, "Mã size", txtMa);
        TienIch.themDong(form, 1, "Tên size", txtTen);
        TienIch.themDong(form, 2, "Phụ thu (đ)", txtPhuThu);
        TienIch.themKhoangTrong(form, 3);
        JPanel trai = TienIch.theForm("Thông tin size", 320, form, TienIch.pnlNutCrud(btnThem, btnSua, btnXoa, btnLamMoi));
        TienIch.canPhai(tbl, 2);
        TienIch.doRong(tbl, 100, 150, 150);
        JPanel phai = TienIch.phaiBang(TienIch.thanhTim(txtTim, btnTim), tbl,
                TienIch.ghiChu("Giá một ly = giá cơ bản của món (size M) + phụ thu của size. Phụ thu có thể âm, ví dụ size S: -3.000."));
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

    private String hienPhuThu(double v) {
        if (v > 0) return "+" + TienIch.tien(v);
        if (v < 0) return "-" + TienIch.tien(-v);
        return "0";
    }

    @Override
    public void taiLai() {
        String k = txtTim.getText().trim().toLowerCase();
        ds = new ArrayList<>();
        for (Size s : dao.getAll()) {
            if (k.isEmpty() || s.getTenSize().toLowerCase().contains(k)) ds.add(s);
        }
        mdl.setRowCount(0);
        for (Size s : ds) {
            mdl.addRow(new Object[] { s.getMaSize(), s.getTenSize(), hienPhuThu(s.getPhuThu()) });
        }
    }

    private void dienForm(int row) {
        Size s = ds.get(row);
        txtMa.setText(s.getMaSize());
        txtTen.setText(s.getTenSize());
        txtPhuThu.setText(String.valueOf((long) s.getPhuThu()));
    }

    private void xoaForm() {
        txtMa.setText(TienIch.CHUA_CO_MA);
        txtTen.setText("");
        txtPhuThu.setText("0");
    }

    private void lamMoi() {
        tbl.clearSelection();
        txtTim.setText("");
        xoaForm();
        taiLai();
    }

    // Đọc và kiểm tra form. Trả null nếu có lỗi (đã báo cho người dùng)
    private Size docForm(String ma) {
        String ten = txtTen.getText().trim();
        if (!ten.matches("[\\p{L}0-9 ]{1,10}")) {
            JOptionPane.showMessageDialog(this, "Tên size gồm 1 đến 10 chữ cái hoặc chữ số, ví dụ S, M, L, XL.");
            return null;
        }
        String pt = txtPhuThu.getText().trim().replace(".", "");
        if (!pt.matches("-?\\d{1,7}")) {
            JOptionPane.showMessageDialog(this, "Phụ thu phải là số nguyên, ví dụ 5000 hoặc -3000.");
            return null;
        }
        return new Size(ma, ten, Double.parseDouble(pt));
    }

    private void them() {
        Size s = docForm(null);
        if (s == null) return;
        if (dao.them(s)) {
            lamMoi();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thất bại! (tên size có thể bị trùng)");
        }
    }

    private void sua() {
        int r = tbl.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng trong bảng trước.");
            return;
        }
        Size s = docForm(ds.get(r).getMaSize());
        if (s == null) return;
        if (dao.sua(s)) {
            lamMoi();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại! (tên size có thể bị trùng)");
        }
    }

    private void xoa() {
        int r = tbl.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng trong bảng trước.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa dòng đang chọn?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        if (dao.xoa(ds.get(r).getMaSize())) {
            lamMoi();
        } else {
            JOptionPane.showMessageDialog(this, "Size này đã được dùng trong hóa đơn nên không thể xóa.");
        }
    }
}
