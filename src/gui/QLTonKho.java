package gui;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import dao.NguyenLieu_DAO;
import entity.NguyenLieu;

// Tồn kho nguyên liệu (nằm trong TabKho). chiXem = true: nhân viên chỉ xem bảng, không có form
public class QLTonKho extends JPanel implements TaiLai {
    private NguyenLieu_DAO dao = new NguyenLieu_DAO();
    private ArrayList<NguyenLieu> ds = new ArrayList<>();
    private JTextField txtMa = TienIch.oMa();
    private JTextField txtTen = new JTextField(10);
    private JTextField txtDvt = new JTextField(10);
    private JTextField txtTon = new JTextField(10);
    private JTextField txtToiThieu = new JTextField(10);
    private JTextField txtTim = new JTextField(18);
    private DefaultTableModel mdl = TienIch.moHinh("Mã NL", "Tên nguyên liệu", "ĐVT", "Tồn kho", "Tối thiểu", "Tình trạng");
    private JTable tbl = TienIch.bang(mdl);
    private JButton btnThem = TienIch.nut("Thêm", TienIch.GREEN);
    private JButton btnSua = TienIch.nut("Sửa", TienIch.BLUE);
    private JButton btnXoa = TienIch.nut("Xóa", TienIch.RED);
    private JButton btnLamMoi = TienIch.nut("Làm mới", new Color(0xB8A890));
    private JButton btnTim = TienIch.nut("Tìm kiếm", TienIch.BROWN);

    public QLTonKho(boolean chiXem) {
        JPanel form = new JPanel(new GridBagLayout());
        TienIch.themDong(form, 0, "Mã NL", txtMa);
        TienIch.themDong(form, 1, "Tên", txtTen);
        TienIch.themDong(form, 2, "Đơn vị tính", txtDvt);
        TienIch.themDong(form, 3, "Tồn ban đầu", txtTon);
        TienIch.themDong(form, 4, "Mức tối thiểu", txtToiThieu);
        TienIch.themKhoangTrong(form, 5);
        JPanel trai = TienIch.theForm("Thông tin nguyên liệu", 320, form, TienIch.pnlNutCrud(btnThem, btnSua, btnXoa, btnLamMoi));

        TienIch.canPhai(tbl, 3, 4);
        TienIch.canGiua(tbl, 2, 5);
        TienIch.doRong(tbl, 70, 220, 70, 110, 110, 100);
        TienIch.tomauCanhBao(tbl, r -> r < ds.size() && ds.get(r).sapHet());
        String ghiChu = chiXem ? "Bạn chỉ có quyền xem tồn kho. Dòng màu đỏ là nguyên liệu sắp hết."
                : "Dòng màu đỏ là nguyên liệu sắp hết. Số tồn chỉ nhập khi thêm mới; sau đó tồn thay đổi qua Phiếu nhập và Phiếu xuất.";
        JPanel phai = TienIch.phaiBang(TienIch.thanhTim(txtTim, btnTim), tbl, TienIch.ghiChu(ghiChu));
        TienIch.xepCrud(this, trai, phai);
        trai.setVisible(!chiXem);

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
        for (NguyenLieu n : ds) {
            mdl.addRow(new Object[] { n.getMaNL(), n.getTenNL(), n.getDonViTinh(), TienIch.soThuc(n.getSoLuongTon()),
                    TienIch.soThuc(n.getMucToiThieu()), n.sapHet() ? "Sắp hết" : "Đủ" });
        }
    }

    private void dienForm(int row) {
        NguyenLieu n = ds.get(row);
        txtMa.setText(n.getMaNL());
        txtTen.setText(n.getTenNL());
        txtDvt.setText(n.getDonViTinh());
        txtTon.setText(TienIch.soThuc(n.getSoLuongTon()));
        txtToiThieu.setText(TienIch.soThuc(n.getMucToiThieu()));
        txtTon.setEditable(false); // tồn chỉ thay đổi qua phiếu nhập/xuất
        txtTon.setBackground(new Color(0xF0EBE3));
    }

    private void xoaForm() {
        txtMa.setText(TienIch.CHUA_CO_MA);
        txtTen.setText("");
        txtDvt.setText("");
        txtTon.setText("0");
        txtToiThieu.setText("0");
        txtTon.setEditable(true);
        txtTon.setBackground(Color.WHITE);
    }

    private void lamMoi() {
        tbl.clearSelection();
        txtTim.setText("");
        xoaForm();
        taiLai();
    }

    // Đọc và kiểm tra form. tonCu chỉ dùng khi sửa (giữ nguyên tồn). Trả null nếu có lỗi.
    private NguyenLieu docForm(String ma, double tonCu) {
        String ten = txtTen.getText().trim().replaceAll("\\s+", " ");
        String dvt = txtDvt.getText().trim();
        if (ten.isEmpty() || ten.length() > 100) {
            JOptionPane.showMessageDialog(this, "Tên nguyên liệu không được để trống và tối đa 100 ký tự.");
            return null;
        }
        if (dvt.isEmpty() || dvt.length() > 20) {
            JOptionPane.showMessageDialog(this, "Đơn vị tính không được để trống và tối đa 20 ký tự (kg, lít, hộp, cái...).");
            return null;
        }
        double ton = (ma == null) ? TienIch.parseSoThuc(txtTon.getText()) : tonCu;
        if (ton < 0) {
            JOptionPane.showMessageDialog(this, "Tồn ban đầu phải là số không âm (tối đa 2 chữ số thập phân).");
            return null;
        }
        double toiThieu = TienIch.parseSoThuc(txtToiThieu.getText());
        if (toiThieu < 0) {
            JOptionPane.showMessageDialog(this, "Mức tối thiểu phải là số không âm (tối đa 2 chữ số thập phân).");
            return null;
        }
        return new NguyenLieu(ma, ten, dvt, ton, toiThieu);
    }

    private void them() {
        NguyenLieu n = docForm(null, 0);
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
        NguyenLieu goc = ds.get(r);
        NguyenLieu n = docForm(goc.getMaNL(), goc.getSoLuongTon());
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
        if (dao.xoa(ds.get(r).getMaNL())) lamMoi();
        else JOptionPane.showMessageDialog(this, "Nguyên liệu này đã có phiếu nhập hoặc phiếu xuất nên không thể xóa.");
    }
}
