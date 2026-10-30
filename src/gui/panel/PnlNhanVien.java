package gui.panel;

import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import entity.NhanVien;
import entity.TaiKhoan;
import gui.component.*;
import gui.util.AppColor;
import gui.util.FormatUtil;
import gui.util.HashUtil;
import gui.util.SessionManager;
import gui.util.Validator;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý nhân viên và tài khoản đăng nhập. CHỈ QUẢN LÝ được vào màn hình này.
 */
public class PnlNhanVien extends CrudPanel {
    public static final String MAT_KHAU_MAC_DINH = "123456";

    private final NhanVienDAO dao = new NhanVienDAO();
    private final TaiKhoanDAO tkDao = new TaiKhoanDAO();
    private List<NhanVien> ds = new ArrayList<>();
    private final JTextField txtMa = UI.readOnly(10);
    private final JTextField txtTen = UI.field(10);
    private final JRadioButton rdNam = UI.radio("Nam");
    private final JRadioButton rdNu = UI.radio("Nữ");
    private final DateField dfNgaySinh = new DateField();
    private final JTextField txtSdt = UI.field(10);
    private final JTextField txtDiaChi = UI.field(10);
    private final JComboBox<String> cboChucVu = UI.combo(new String[]{"Quản lý", "Thu ngân", "Pha chế"});
    private final JComboBox<String> cboTT = UI.combo(new String[]{"Đang làm", "Nghỉ việc"});
    private final JTextField txtTenDN = UI.field(10);
    private final BoGocButton btnDatLai = UI.button("Đặt lại MK", AppColor.ORANGE);

    public PnlNhanVien() {
        super("Thông tin nhân viên", "Mã NV", "Họ tên", "GT", "SĐT", "Chức vụ", "Trạng thái");
        if (!SessionManager.laQuanLy()) throw new SecurityException("Chỉ Quản lý mới được quản lý nhân viên.");
        setFormWidth(340);
        init();
        btnXoa.setText("Nghỉ việc");
        pnlButtons.add(btnDatLai);
        btnDatLai.addActionListener(e -> {
            if (table.getSelectedRow() < 0) {
                UI.warn("Hãy chọn nhân viên cần đặt lại mật khẩu.");
                return;
            }
            NhanVien nv = ds.get(table.getSelectedRow());
            if (UI.confirm("Đặt lại mật khẩu của " + nv.getHoTen() + " về mặc định (" + MAT_KHAU_MAC_DINH + ")?"))
                UI.run(() -> {
                    tkDao.datLaiMatKhau(nv.getMaNV(), HashUtil.sha256(MAT_KHAU_MAC_DINH));
                    UI.info("Đã đặt lại mật khẩu về " + MAT_KHAU_MAC_DINH + ".");
                });
        });
        UI.alignCenter(table, 2);
        UI.badge(table, 5);
        UI.widths(table, 70, 170, 50, 110, 100, 110);
        clearForm();
        txtTim.setToolTipText("Tìm theo mã, tên hoặc số điện thoại");
        pnlSouth.add(UI.muted("Nhân viên nghỉ việc không bị xóa khỏi CSDL, chỉ khóa tài khoản. Mật khẩu mặc định khi tạo mới: " + MAT_KHAU_MAC_DINH), BorderLayout.CENTER);
    }

    @Override
    protected void buildForm(FormPanel f) {
        f.row("Mã NV", txtMa);
        f.row("Họ tên", txtTen);
        ButtonGroup bg = new ButtonGroup();
        bg.add(rdNam);
        bg.add(rdNu);
        rdNam.setSelected(true);
        JPanel gt = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        gt.setOpaque(false);
        gt.add(rdNam);
        gt.add(rdNu);
        f.row("Giới tính", gt);
        f.row("Ngày sinh", dfNgaySinh);
        f.row("SĐT", txtSdt);
        f.row("Địa chỉ", txtDiaChi);
        f.row("Chức vụ", cboChucVu);
        f.row("Trạng thái", cboTT);
        f.section("Tài khoản đăng nhập");
        f.row("Tên đăng nhập", txtTenDN);
    }

    @Override
    protected void load(String keyword) {
        ds = keyword.isEmpty() ? dao.getAll() : dao.timKiem(keyword);
        model.setRowCount(0);
        for (NhanVien n : ds)
            model.addRow(new Object[]{n.getMaNV(), n.getHoTen(), n.getGioiTinhText(), n.getSdt(), NhanVien.tenChucVu(n.getChucVu()),
                    n.isTrangThai() ? "Đang làm" : "Nghỉ việc"});
    }

    @Override
    protected void fillForm(int row) {
        NhanVien n = ds.get(row);
        txtMa.setText(n.getMaNV());
        txtTen.setText(n.getHoTen());
        (n.isGioiTinh() ? rdNam : rdNu).setSelected(true);
        dfNgaySinh.setDate(n.getNgaySinh());
        txtSdt.setText(n.getSdt());
        txtDiaChi.setText(n.getDiaChi() == null ? "" : n.getDiaChi());
        cboChucVu.setSelectedItem(NhanVien.tenChucVu(n.getChucVu()));
        cboTT.setSelectedIndex(n.isTrangThai() ? 0 : 1);
        TaiKhoan tk = null;
        try {
            tk = tkDao.getByNhanVien(n.getMaNV());
        } catch (RuntimeException ignored) {
        }
        txtTenDN.setText(tk == null ? "" : tk.getTenDangNhap());
        khoaTenDN(true);
    }

    @Override
    protected void clearForm() {
        txtMa.setText("(tự sinh khi thêm)");
        txtTen.setText("");
        rdNam.setSelected(true);
        dfNgaySinh.setDate(null);
        txtSdt.setText("");
        txtDiaChi.setText("");
        cboChucVu.setSelectedIndex(1);
        cboTT.setSelectedIndex(0);
        txtTenDN.setText("");
        khoaTenDN(false);
    }

    private void khoaTenDN(boolean khoa) {
        txtTenDN.setEditable(!khoa);
        txtTenDN.setBackground(khoa ? new Color(0xF0EBE3) : Color.WHITE);
    }

    /** Đọc và kiểm tra form. Trả null nếu có lỗi (đã báo cho người dùng). */
    private NhanVien docForm(String ma) {
        String ten = txtTen.getText().trim().replaceAll("\\s+", " ");
        if (!Validator.hoTen(ten)) {
            UI.warn("Họ tên chỉ gồm chữ cái và khoảng trắng, dài 2 đến 50 ký tự.");
            return null;
        }
        LocalDate ns = dfNgaySinh.getDate();
        if (ns == null) {
            UI.warn("Hãy chọn ngày sinh.");
            return null;
        }
        if (ns.isAfter(LocalDate.now().minusYears(16))) {
            UI.warn("Nhân viên phải đủ 16 tuổi trở lên.");
            return null;
        }
        String sdt = txtSdt.getText().trim();
        if (!Validator.sdt(sdt)) {
            UI.warn("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0.");
            return null;
        }
        String dc = txtDiaChi.getText().trim();
        if (dc.length() > 200) {
            UI.warn("Địa chỉ tối đa 200 ký tự.");
            return null;
        }
        return new NhanVien(ma, ten, rdNam.isSelected(), ns, sdt, dc, NhanVien.maChucVu((String) cboChucVu.getSelectedItem()),
                cboTT.getSelectedIndex() == 0);
    }

    @Override
    protected void them() {
        NhanVien nv = docForm(null);
        if (nv == null) return;
        String tenDN = txtTenDN.getText().trim();
        if (!Validator.tenDangNhap(tenDN)) {
            UI.warn("Tên đăng nhập gồm 4 đến 20 ký tự: chữ không dấu, số, dấu chấm hoặc gạch dưới.");
            return;
        }
        if (tkDao.tonTaiTenDangNhap(tenDN)) {
            UI.warn("Tên đăng nhập này đã có người dùng.");
            return;
        }
        dao.themVoiTaiKhoan(nv, tenDN, HashUtil.sha256(MAT_KHAU_MAC_DINH));
        lamMoi();
        UI.info("Đã thêm nhân viên " + nv.getMaNV() + ".\nTài khoản: " + tenDN + "  |  Mật khẩu mặc định: " + MAT_KHAU_MAC_DINH);
    }

    @Override
    protected void sua() {
        NhanVien goc = ds.get(table.getSelectedRow());
        NhanVien nv = docForm(goc.getMaNV());
        if (nv == null) return;
        if (goc.getMaNV().equals(SessionManager.getNhanVien().getMaNV())
                && (!NhanVien.QUAN_LY.equals(nv.getChucVu()) || !nv.isTrangThai())) {
            UI.warn("Bạn không thể tự hạ quyền hoặc cho chính mình nghỉ việc.");
            return;
        }
        dao.capNhat(nv);
        lamMoi();
        UI.info("Đã cập nhật nhân viên " + nv.getMaNV() + ".");
    }

    @Override
    protected String xacNhanXoa() {
        return "Cho nhân viên này nghỉ việc? Tài khoản của họ sẽ bị khóa.";
    }

    @Override
    protected void xoa() {
        NhanVien nv = ds.get(table.getSelectedRow());
        if (nv.getMaNV().equals(SessionManager.getNhanVien().getMaNV())) {
            UI.warn("Bạn không thể cho chính mình nghỉ việc.");
            return;
        }
        dao.choNghiViec(nv.getMaNV());
        lamMoi();
    }
}
