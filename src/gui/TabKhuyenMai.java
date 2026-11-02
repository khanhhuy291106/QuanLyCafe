package gui;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.jdatepicker.impl.JDatePickerImpl;
import dao.KhuyenMai_DAO;
import entity.KhuyenMai;

// Cài đặt khuyến mãi. CHỈ QUẢN LÝ được vào màn hình này; nhân viên chỉ chọn khuyến mãi có sẵn khi bán hàng.
public class TabKhuyenMai extends JPanel implements TaiLai {
    private KhuyenMai_DAO dao = new KhuyenMai_DAO();
    private ArrayList<KhuyenMai> ds = new ArrayList<>();
    private JTextField txtMa = TienIch.oMa();
    private JTextField txtTen = new JTextField(10);
    private JComboBox<String> cboLoai = new JComboBox<>(new String[] { "Phần trăm (%)", "Số tiền (đ)" });
    private JTextField txtGiaTri = new JTextField(10);
    private JTextField txtToiThieu = new JTextField(10);
    private JDatePickerImpl dpBD = DinhDangNgay.taoDatePicker(null);
    private JDatePickerImpl dpKT = DinhDangNgay.taoDatePicker(null);
    private JComboBox<String> cboTT = new JComboBox<>(new String[] { "Đang bật", "Tạm tắt" });
    private JTextField txtTim = new JTextField(18);
    private DefaultTableModel mdl = TienIch.moHinh("Mã", "Tên chương trình", "Giảm", "Đơn tối thiểu", "Hiệu lực", "Trạng thái");
    private JTable tbl = TienIch.bang(mdl);
    private JButton btnThem = TienIch.nut("Thêm", TienIch.GREEN);
    private JButton btnSua = TienIch.nut("Sửa", TienIch.BLUE);
    private JButton btnXoa = TienIch.nut("Xóa", TienIch.RED);
    private JButton btnLamMoi = TienIch.nut("Làm mới", new Color(0xB8A890));
    private JButton btnTim = TienIch.nut("Tìm kiếm", TienIch.BROWN);

    public TabKhuyenMai() {
        JPanel form = new JPanel(new GridBagLayout());
        TienIch.themDong(form, 0, "Mã KM", txtMa);
        TienIch.themDong(form, 1, "Tên", txtTen);
        TienIch.themDong(form, 2, "Loại giảm", cboLoai);
        TienIch.themDong(form, 3, "Giá trị", txtGiaTri);
        TienIch.themDong(form, 4, "Đơn tối thiểu", txtToiThieu);
        TienIch.themDong(form, 5, "Từ ngày", dpBD);
        TienIch.themDong(form, 6, "Đến ngày", dpKT);
        TienIch.themDong(form, 7, "Trạng thái", cboTT);
        TienIch.themKhoangTrong(form, 8);
        JPanel trai = TienIch.theForm("Thông tin khuyến mãi", 340, form, TienIch.pnlNutCrud(btnThem, btnSua, btnXoa, btnLamMoi));

        TienIch.canPhai(tbl, 2, 3);
        TienIch.canGiua(tbl, 4);
        TienIch.tomauTrangThai(tbl, 5);
        TienIch.doRong(tbl, 60, 200, 80, 100, 130, 110);
        JPanel phai = TienIch.phaiBang(TienIch.thanhTim(txtTim, btnTim), tbl, TienIch.ghiChu(
                "Màn hình Bán hàng chỉ liệt kê khuyến mãi đang bật, còn hạn và có đơn tối thiểu ≤ tạm tính. Mỗi hóa đơn áp tối đa một khuyến mãi."));
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
        ds = dao.timKiem(txtTim.getText().trim());
        mdl.setRowCount(0);
        DateTimeFormatter ngayThang = DateTimeFormatter.ofPattern("dd/MM");
        for (KhuyenMai k : ds) {
            mdl.addRow(new Object[] { k.getMaKM(), k.getTenKM(), k.getGiaTriText(), TienIch.tien(k.getDonToiThieu()),
                    k.getNgayBD().format(ngayThang) + " – " + TienIch.ngay(k.getNgayKT()), k.tenHieuLuc(LocalDate.now()) });
        }
    }

    private void dienForm(int row) {
        KhuyenMai k = ds.get(row);
        txtMa.setText(k.getMaKM());
        txtTen.setText(k.getTenKM());
        cboLoai.setSelectedIndex(KhuyenMai.PHAN_TRAM.equals(k.getLoaiGiam()) ? 0 : 1);
        txtGiaTri.setText(String.valueOf((long) k.getGiaTri()));
        txtToiThieu.setText(TienIch.tien(k.getDonToiThieu()));
        DinhDangNgay.datNgay(dpBD, k.getNgayBD());
        DinhDangNgay.datNgay(dpKT, k.getNgayKT());
        cboTT.setSelectedIndex(k.isTrangThai() ? 0 : 1);
    }

    private void xoaForm() {
        txtMa.setText(TienIch.CHUA_CO_MA);
        txtTen.setText("");
        cboLoai.setSelectedIndex(0);
        txtGiaTri.setText("");
        txtToiThieu.setText("0");
        DinhDangNgay.datNgay(dpBD, LocalDate.now());
        DinhDangNgay.datNgay(dpKT, LocalDate.now().plusDays(30));
        cboTT.setSelectedIndex(0);
    }

    private void lamMoi() {
        tbl.clearSelection();
        txtTim.setText("");
        xoaForm();
        taiLai();
    }

    // Đọc và kiểm tra form. Trả null nếu có lỗi (đã báo cho người dùng)
    private KhuyenMai docForm(String ma) {
        String ten = txtTen.getText().trim().replaceAll("\\s+", " ");
        if (ten.isEmpty() || ten.length() > 100) {
            JOptionPane.showMessageDialog(this, "Tên chương trình không được để trống và tối đa 100 ký tự.");
            return null;
        }
        boolean phanTram = cboLoai.getSelectedIndex() == 0;
        double giaTri = TienIch.parseTien(txtGiaTri.getText());
        if (giaTri <= 0) {
            JOptionPane.showMessageDialog(this, "Giá trị giảm phải là số nguyên lớn hơn 0.");
            return null;
        }
        if (phanTram && giaTri > 100) {
            JOptionPane.showMessageDialog(this, "Giảm theo phần trăm không được quá 100.");
            return null;
        }
        double toiThieu = TienIch.parseTien(txtToiThieu.getText());
        if (toiThieu < 0) {
            JOptionPane.showMessageDialog(this, "Đơn tối thiểu phải là số nguyên không âm (nhập 0 nếu không yêu cầu).");
            return null;
        }
        LocalDate bd = DinhDangNgay.layNgay(dpBD);
        LocalDate kt = DinhDangNgay.layNgay(dpKT);
        if (bd == null || kt == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn đủ ngày bắt đầu và ngày kết thúc.");
            return null;
        }
        if (kt.isBefore(bd)) {
            JOptionPane.showMessageDialog(this, "Ngày kết thúc không được trước ngày bắt đầu.");
            return null;
        }
        return new KhuyenMai(ma, ten, phanTram ? KhuyenMai.PHAN_TRAM : KhuyenMai.SO_TIEN, giaTri, toiThieu, bd, kt, cboTT.getSelectedIndex() == 0);
    }

    private void them() {
        KhuyenMai k = docForm(null);
        if (k == null) return;
        if (dao.them(k)) {
            lamMoi();
            JOptionPane.showMessageDialog(this, "Đã thêm khuyến mãi " + k.getMaKM() + ".");
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thất bại!");
        }
    }

    private void sua() {
        int r = tbl.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng trong bảng trước.");
            return;
        }
        KhuyenMai k = docForm(ds.get(r).getMaKM());
        if (k == null) return;
        if (dao.sua(k)) {
            lamMoi();
            JOptionPane.showMessageDialog(this, "Đã cập nhật khuyến mãi " + k.getMaKM() + ".");
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
        }
    }

    private void xoa() {
        int r = tbl.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng trong bảng trước.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa dòng đang chọn?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        if (dao.xoa(ds.get(r).getMaKM())) {
            lamMoi();
        } else {
            JOptionPane.showMessageDialog(this, "Khuyến mãi này đã được áp dụng cho hóa đơn nên không thể xóa. Hãy chọn “Tạm tắt” thay vì xóa.");
        }
    }
}
