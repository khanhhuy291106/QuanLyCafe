package gui;

import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.jdatepicker.impl.JDatePickerImpl;
import dao.NhanVien_DAO;
import dao.TaiKhoan_DAO;
import entity.NhanVien;
import entity.TaiKhoan;

// Quản lý nhân viên và tài khoản đăng nhập. CHỈ QUẢN LÝ được vào màn hình này.
public class TabNhanVien extends JPanel implements TaiLai {
    private static final String MAT_KHAU_MAC_DINH = "123456";

    private NhanVien_DAO dao = new NhanVien_DAO();
    private TaiKhoan_DAO tk_dao = new TaiKhoan_DAO();
    private NhanVien nvHienTai;
    private ArrayList<NhanVien> ds = new ArrayList<>();
    private JTextField txtMa = TienIch.oMa();
    private JTextField txtTen = new JTextField(10);
    private JRadioButton rdNam = new JRadioButton("Nam", true);
    private JRadioButton rdNu = new JRadioButton("Nữ");
    private JDatePickerImpl dpNgaySinh = DinhDangNgay.taoDatePicker(null);
    private JTextField txtSdt = new JTextField(10);
    private JTextField txtDiaChi = new JTextField(10);
    private JComboBox<String> cboChucVu = new JComboBox<>(new String[] { "Quản lý", "Thu ngân", "Pha chế" });
    private JComboBox<String> cboTT = new JComboBox<>(new String[] { "Đang làm", "Nghỉ việc" });
    private JTextField txtTenDN = new JTextField(10);
    private JTextField txtTim = new JTextField(18);
    private DefaultTableModel mdl = TienIch.moHinh("Mã NV", "Họ tên", "GT", "SĐT", "Chức vụ", "Trạng thái");
    private JTable tbl = TienIch.bang(mdl);
    private JButton btnThem = TienIch.nut("Thêm", TienIch.GREEN);
    private JButton btnSua = TienIch.nut("Sửa", TienIch.BLUE);
    private JButton btnNghi = TienIch.nut("Nghỉ việc", TienIch.RED);
    private JButton btnLamMoi = TienIch.nut("Làm mới", new Color(0xB8A890));
    private JButton btnDatLai = TienIch.nut("Đặt lại MK", TienIch.ORANGE);
    private JButton btnTim = TienIch.nut("Tìm kiếm", TienIch.BROWN);

    public TabNhanVien(NhanVien nvHienTai) {
        this.nvHienTai = nvHienTai;
        JPanel form = new JPanel(new GridBagLayout());
        ButtonGroup nhomGT = new ButtonGroup();
        nhomGT.add(rdNam);
        nhomGT.add(rdNu);
        JPanel pnlGT = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlGT.setOpaque(false);
        pnlGT.add(rdNam);
        pnlGT.add(rdNu);
        TienIch.themDong(form, 0, "Mã NV", txtMa);
        TienIch.themDong(form, 1, "Họ tên", txtTen);
        TienIch.themDong(form, 2, "Giới tính", pnlGT);
        TienIch.themDong(form, 3, "Ngày sinh", dpNgaySinh);
        TienIch.themDong(form, 4, "SĐT", txtSdt);
        TienIch.themDong(form, 5, "Địa chỉ", txtDiaChi);
        TienIch.themDong(form, 6, "Chức vụ", cboChucVu);
        TienIch.themDong(form, 7, "Trạng thái", cboTT);
        JLabel lblTK = new JLabel("Tài khoản đăng nhập");
        lblTK.setFont(TienIch.FONT_B);
        lblTK.setForeground(TienIch.BROWN);
        TienIch.themDong(form, 8, "", lblTK);
        TienIch.themDong(form, 9, "Tên đăng nhập", txtTenDN);
        TienIch.themKhoangTrong(form, 10);
        JPanel trai = TienIch.theForm("Thông tin nhân viên", 340, form, TienIch.pnlNutCrud(btnThem, btnSua, btnNghi, btnLamMoi, btnDatLai));

        txtTim.setToolTipText("Tìm theo mã, tên hoặc số điện thoại");
        TienIch.canGiua(tbl, 2);
        TienIch.tomauTrangThai(tbl, 5);
        TienIch.doRong(tbl, 70, 170, 50, 110, 100, 110);
        JPanel phai = TienIch.phaiBang(TienIch.thanhTim(txtTim, btnTim), tbl, TienIch.ghiChu(
                "Nhân viên nghỉ việc không bị xóa khỏi CSDL, chỉ khóa tài khoản. Mật khẩu mặc định khi tạo mới: " + MAT_KHAU_MAC_DINH));
        TienIch.xepCrud(this, trai, phai);

        tbl.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tbl.getSelectedRow() >= 0) dienForm(tbl.getSelectedRow());
        });
        btnTim.addActionListener(e -> taiLai());
        txtTim.addActionListener(e -> taiLai());
        btnThem.addActionListener(e -> them());
        btnSua.addActionListener(e -> sua());
        btnNghi.addActionListener(e -> choNghiViec());
        btnLamMoi.addActionListener(e -> lamMoi());
        btnDatLai.addActionListener(e -> datLaiMatKhau());
        xoaForm();
    }

    @Override
    public void taiLai() {
        ds = dao.timKiem(txtTim.getText().trim());
        mdl.setRowCount(0);
        for (NhanVien n : ds) {
            mdl.addRow(new Object[] { n.getMaNV(), n.getHoTen(), n.getGioiTinhText(), n.getSdt(), NhanVien.tenChucVu(n.getChucVu()),
                    n.isTrangThai() ? "Đang làm" : "Nghỉ việc" });
        }
    }

    private void dienForm(int row) {
        NhanVien n = ds.get(row);
        txtMa.setText(n.getMaNV());
        txtTen.setText(n.getHoTen());
        if (n.isGioiTinh()) rdNam.setSelected(true);
        else rdNu.setSelected(true);
        DinhDangNgay.datNgay(dpNgaySinh, n.getNgaySinh());
        txtSdt.setText(n.getSdt());
        txtDiaChi.setText(n.getDiaChi() == null ? "" : n.getDiaChi());
        cboChucVu.setSelectedItem(NhanVien.tenChucVu(n.getChucVu()));
        cboTT.setSelectedIndex(n.isTrangThai() ? 0 : 1);
        TaiKhoan tk = tk_dao.getByNhanVien(n.getMaNV());
        txtTenDN.setText(tk == null ? "" : tk.getTenDangNhap());
        khoaTenDN(true);
    }

    private void xoaForm() {
        txtMa.setText(TienIch.CHUA_CO_MA);
        txtTen.setText("");
        rdNam.setSelected(true);
        DinhDangNgay.datNgay(dpNgaySinh, null);
        txtSdt.setText("");
        txtDiaChi.setText("");
        cboChucVu.setSelectedIndex(1);
        cboTT.setSelectedIndex(0);
        txtTenDN.setText("");
        khoaTenDN(false);
    }

    // Tên đăng nhập chỉ nhập khi thêm mới, khi sửa thì khóa lại
    private void khoaTenDN(boolean khoa) {
        txtTenDN.setEditable(!khoa);
        txtTenDN.setBackground(khoa ? new Color(0xF0EBE3) : Color.WHITE);
    }

    private void lamMoi() {
        tbl.clearSelection();
        txtTim.setText("");
        xoaForm();
        taiLai();
    }

    // Đọc và kiểm tra form. Trả null nếu có lỗi (đã báo cho người dùng)
    private NhanVien docForm(String ma) {
        String ten = txtTen.getText().trim().replaceAll("\\s+", " ");
        if (ten.length() < 2 || ten.length() > 50 || !ten.matches("^[\\p{L}]+([ '][\\p{L}]+)*$")) {
            JOptionPane.showMessageDialog(this, "Họ tên chỉ gồm chữ cái và khoảng trắng, dài 2 đến 50 ký tự.");
            return null;
        }
        LocalDate ngaySinh = DinhDangNgay.layNgay(dpNgaySinh);
        if (ngaySinh == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn ngày sinh.");
            return null;
        }
        if (ngaySinh.isAfter(LocalDate.now().minusYears(16))) {
            JOptionPane.showMessageDialog(this, "Nhân viên phải đủ 16 tuổi trở lên.");
            return null;
        }
        String sdt = txtSdt.getText().trim();
        if (!sdt.matches("^0\\d{9}$")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0.");
            return null;
        }
        String diaChi = txtDiaChi.getText().trim();
        if (diaChi.length() > 200) {
            JOptionPane.showMessageDialog(this, "Địa chỉ tối đa 200 ký tự.");
            return null;
        }
        return new NhanVien(ma, ten, rdNam.isSelected(), ngaySinh, sdt, diaChi,
                NhanVien.maChucVu((String) cboChucVu.getSelectedItem()), cboTT.getSelectedIndex() == 0);
    }

    private void them() {
        NhanVien nv = docForm(null);
        if (nv == null) return;
        String tenDN = txtTenDN.getText().trim();
        if (!tenDN.matches("^[a-zA-Z0-9._]{4,20}$")) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập gồm 4 đến 20 ký tự: chữ không dấu, số, dấu chấm hoặc gạch dưới.");
            return;
        }
        if (tk_dao.tonTaiTenDangNhap(tenDN)) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập này đã có người dùng.");
            return;
        }
        if (dao.themVoiTaiKhoan(nv, tenDN, TaiKhoan_DAO.bamMatKhau(MAT_KHAU_MAC_DINH))) {
            lamMoi();
            JOptionPane.showMessageDialog(this, "Đã thêm nhân viên " + nv.getMaNV() + ".\nTài khoản: " + tenDN + "  |  Mật khẩu mặc định: " + MAT_KHAU_MAC_DINH);
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
        NhanVien goc = ds.get(r);
        NhanVien nv = docForm(goc.getMaNV());
        if (nv == null) return;
        if (goc.getMaNV().equals(nvHienTai.getMaNV()) && (!NhanVien.QUAN_LY.equals(nv.getChucVu()) || !nv.isTrangThai())) {
            JOptionPane.showMessageDialog(this, "Bạn không thể tự hạ quyền hoặc cho chính mình nghỉ việc.");
            return;
        }
        if (dao.capNhat(nv)) {
            lamMoi();
            JOptionPane.showMessageDialog(this, "Đã cập nhật nhân viên " + nv.getMaNV() + ".");
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
        }
    }

    private void choNghiViec() {
        int r = tbl.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng trong bảng trước.");
            return;
        }
        NhanVien nv = ds.get(r);
        if (nv.getMaNV().equals(nvHienTai.getMaNV())) {
            JOptionPane.showMessageDialog(this, "Bạn không thể cho chính mình nghỉ việc.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Cho nhân viên này nghỉ việc? Tài khoản của họ sẽ bị khóa.", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        if (dao.choNghiViec(nv.getMaNV())) lamMoi();
        else JOptionPane.showMessageDialog(this, "Thao tác thất bại!");
    }

    private void datLaiMatKhau() {
        int r = tbl.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn nhân viên cần đặt lại mật khẩu.");
            return;
        }
        NhanVien nv = ds.get(r);
        if (JOptionPane.showConfirmDialog(this, "Đặt lại mật khẩu của " + nv.getHoTen() + " về mặc định (" + MAT_KHAU_MAC_DINH + ")?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        if (tk_dao.datLaiMatKhau(nv.getMaNV(), MAT_KHAU_MAC_DINH)) {
            JOptionPane.showMessageDialog(this, "Đã đặt lại mật khẩu về " + MAT_KHAU_MAC_DINH + ".");
        } else {
            JOptionPane.showMessageDialog(this, "Đặt lại mật khẩu thất bại!");
        }
    }
}
