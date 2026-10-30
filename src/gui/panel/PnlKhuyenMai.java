package gui.panel;

import dao.KhuyenMaiDAO;
import entity.KhuyenMai;
import gui.component.*;
import gui.util.FormatUtil;
import gui.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Cài đặt khuyến mãi. CHỈ QUẢN LÝ được vào màn hình này;
 * nhân viên chỉ được chọn khuyến mãi có sẵn khi bán hàng.
 */
public class PnlKhuyenMai extends CrudPanel {
    private final KhuyenMaiDAO dao = new KhuyenMaiDAO();
    private List<KhuyenMai> ds = new ArrayList<>();
    private final JTextField txtMa = UI.readOnly(10);
    private final JTextField txtTen = UI.field(10);
    private final JComboBox<String> cboLoai = UI.combo(new String[]{"Phần trăm (%)", "Số tiền (đ)"});
    private final JTextField txtGiaTri = UI.field(10);
    private final JTextField txtToiThieu = UI.field(10);
    private final DateField dfBD = new DateField();
    private final DateField dfKT = new DateField();
    private final JComboBox<String> cboTT = UI.combo(new String[]{"Đang bật", "Tạm tắt"});

    public PnlKhuyenMai() {
        super("Thông tin khuyến mãi", "Mã", "Tên chương trình", "Giảm", "Đơn tối thiểu", "Hiệu lực", "Trạng thái");
        if (!SessionManager.laQuanLy()) throw new SecurityException("Chỉ Quản lý mới được cài khuyến mãi.");
        setFormWidth(340);
        init();
        UI.alignRight(table, 2, 3);
        UI.alignCenter(table, 4);
        UI.badge(table, 5);
        UI.widths(table, 60, 200, 80, 100, 130, 110);
        clearForm();
        pnlSouth.add(UI.muted("<html>Màn hình Bán hàng chỉ liệt kê khuyến mãi đang bật, còn hạn và có đơn tối thiểu ≤ tạm tính. "
                + "Mỗi hóa đơn áp tối đa một khuyến mãi.</html>"), BorderLayout.CENTER);
    }

    @Override
    protected void buildForm(FormPanel f) {
        f.row("Mã KM", txtMa);
        f.row("Tên", txtTen);
        f.row("Loại giảm", cboLoai);
        f.row("Giá trị", txtGiaTri);
        f.row("Đơn tối thiểu", txtToiThieu);
        f.row("Từ ngày", dfBD);
        f.row("Đến ngày", dfKT);
        f.row("Trạng thái", cboTT);
    }

    private static String hieuLuc(KhuyenMai k) {
        return k.isTrangThai() ? k.tenHieuLuc(LocalDate.now()) : "Tạm tắt";
    }

    @Override
    protected void load(String keyword) {
        ds = keyword.isEmpty() ? dao.getAll() : dao.timKiem(keyword);
        model.setRowCount(0);
        for (KhuyenMai k : ds)
            model.addRow(new Object[]{k.getMaKM(), k.getTenKM(), k.getGiaTriText(), FormatUtil.tien(k.getDonToiThieu()),
                    k.getNgayBD().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")) + " – " + FormatUtil.ngay(k.getNgayKT()),
                    hieuLuc(k)});
    }

    @Override
    protected void fillForm(int row) {
        KhuyenMai k = ds.get(row);
        txtMa.setText(k.getMaKM());
        txtTen.setText(k.getTenKM());
        cboLoai.setSelectedIndex(KhuyenMai.PHAN_TRAM.equals(k.getLoaiGiam()) ? 0 : 1);
        txtGiaTri.setText(String.valueOf((long) k.getGiaTri()));
        txtToiThieu.setText(FormatUtil.tien(k.getDonToiThieu()));
        dfBD.setDate(k.getNgayBD());
        dfKT.setDate(k.getNgayKT());
        cboTT.setSelectedIndex(k.isTrangThai() ? 0 : 1);
    }

    @Override
    protected void clearForm() {
        txtMa.setText("(tự sinh khi thêm)");
        txtTen.setText("");
        cboLoai.setSelectedIndex(0);
        txtGiaTri.setText("");
        txtToiThieu.setText("0");
        dfBD.setDate(LocalDate.now());
        dfKT.setDate(LocalDate.now().plusDays(30));
        cboTT.setSelectedIndex(0);
    }

    private KhuyenMai docForm(String ma) {
        String ten = txtTen.getText().trim().replaceAll("\\s+", " ");
        if (ten.isEmpty() || ten.length() > 100) {
            UI.warn("Tên chương trình không được để trống và tối đa 100 ký tự.");
            return null;
        }
        boolean phanTram = cboLoai.getSelectedIndex() == 0;
        double gt = FormatUtil.parseTien(txtGiaTri.getText());
        if (gt <= 0) {
            UI.warn("Giá trị giảm phải là số nguyên lớn hơn 0.");
            return null;
        }
        if (phanTram && gt > 100) {
            UI.warn("Giảm theo phần trăm không được quá 100.");
            return null;
        }
        double toiThieu = FormatUtil.parseTien(txtToiThieu.getText());
        if (toiThieu < 0) {
            UI.warn("Đơn tối thiểu phải là số nguyên không âm (nhập 0 nếu không yêu cầu).");
            return null;
        }
        LocalDate bd = dfBD.getDate(), kt = dfKT.getDate();
        if (bd == null || kt == null) {
            UI.warn("Hãy chọn đủ ngày bắt đầu và ngày kết thúc.");
            return null;
        }
        if (kt.isBefore(bd)) {
            UI.warn("Ngày kết thúc không được trước ngày bắt đầu.");
            return null;
        }
        return new KhuyenMai(ma, ten, phanTram ? KhuyenMai.PHAN_TRAM : KhuyenMai.SO_TIEN, gt, toiThieu, bd, kt, cboTT.getSelectedIndex() == 0);
    }

    @Override
    protected void them() {
        KhuyenMai k = docForm(null);
        if (k == null) return;
        dao.them(k);
        lamMoi();
        UI.info("Đã thêm khuyến mãi " + k.getMaKM() + ".");
    }

    @Override
    protected void sua() {
        KhuyenMai k = docForm(ds.get(table.getSelectedRow()).getMaKM());
        if (k == null) return;
        dao.sua(k);
        lamMoi();
        UI.info("Đã cập nhật khuyến mãi " + k.getMaKM() + ".");
    }

    @Override
    protected void xoa() {
        try {
            dao.xoa(ds.get(table.getSelectedRow()).getMaKM());
        } catch (dao.DaoException e) {
            throw PnlThucDon.fk(e, "Khuyến mãi này đã được áp dụng cho hóa đơn nên không thể xóa. Hãy chọn “Tạm tắt” thay vì xóa.");
        }
        lamMoi();
    }
}
