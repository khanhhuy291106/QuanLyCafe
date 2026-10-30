package gui.panel;

import dao.*;
import entity.*;
import gui.component.*;
import gui.dialog.DlgLichSuPhieu;
import gui.util.AppColor;
import gui.util.FormatUtil;
import gui.util.SessionManager;
import gui.util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Kho nguyên liệu. Quản lý có đủ 4 thẻ: Tồn kho, Phiếu nhập, Phiếu xuất, Nhà cung cấp.
 * Nhân viên (thu ngân, pha chế) chỉ xem được thẻ Tồn kho, không sửa được gì.
 */
public class PnlKho extends JPanel implements Refreshable {
    private final JTabbedPane tabs = new JTabbedPane();

    public PnlKho() {
        setLayout(new BorderLayout());
        setBackground(AppColor.BG);
        tabs.setFont(AppColor.FONT_B);
        boolean ql = SessionManager.laQuanLy();
        tabs.addTab("Tồn kho", new PnlTonKho(!ql));
        if (ql) {
            tabs.addTab("Phiếu nhập", new PnlPhieuNhap());
            tabs.addTab("Phiếu xuất", new PnlPhieuXuat());
            tabs.addTab("Nhà cung cấp", new PnlNhaCungCap());
        }
        tabs.addChangeListener(e -> refresh());
        add(tabs, BorderLayout.CENTER);
    }

    /** Mở thẳng một thẻ (ví dụ nút "Lập phiếu nhập" ở trang chủ). */
    public void chonThe(String ten) {
        for (int i = 0; i < tabs.getTabCount(); i++) if (tabs.getTitleAt(i).equals(ten)) tabs.setSelectedIndex(i);
    }

    @Override
    public void refresh() {
        Component c = tabs.getSelectedComponent();
        if (c instanceof Refreshable) ((Refreshable) c).refresh();
    }

    private static String sl(double v, String dvt) {
        return FormatUtil.soThuc(v) + " " + dvt;
    }

    // =====================================================================================
    //  TỒN KHO
    // =====================================================================================
    static class PnlTonKho extends CrudPanel {
        private final NguyenLieuDAO dao = new NguyenLieuDAO();
        private List<NguyenLieu> ds = new ArrayList<>();
        private final JTextField txtMa = UI.readOnly(10);
        private final JTextField txtTen = UI.field(10);
        private final JTextField txtDvt = UI.field(10);
        private final JTextField txtTon = UI.field(10);
        private final JTextField txtToiThieu = UI.field(10);

        PnlTonKho(boolean chiXem) {
            super("Thông tin nguyên liệu", "Mã NL", "Tên nguyên liệu", "ĐVT", "Tồn kho", "Tối thiểu", "Tình trạng");
            init();
            UI.alignRight(table, 3, 4);
            UI.alignCenter(table, 2, 5);
            UI.widths(table, 70, 220, 70, 110, 110, 100);
            UI.warnRows(table, r -> r < ds.size() && ds.get(r).sapHet());
            clearForm();
            if (chiXem) {
                setReadOnlyMode();
                pnlSouth.add(UI.muted("Bạn chỉ có quyền xem tồn kho. Dòng màu đỏ là nguyên liệu sắp hết."), BorderLayout.CENTER);
            } else {
                pnlSouth.add(UI.muted("Dòng màu đỏ là nguyên liệu sắp hết. Số tồn chỉ nhập khi thêm mới; sau đó tồn thay đổi qua Phiếu nhập và Phiếu xuất."), BorderLayout.CENTER);
            }
        }

        @Override
        protected void buildForm(FormPanel f) {
            f.row("Mã NL", txtMa);
            f.row("Tên", txtTen);
            f.row("Đơn vị tính", txtDvt);
            f.row("Tồn ban đầu", txtTon);
            f.row("Mức tối thiểu", txtToiThieu);
        }

        @Override
        protected void load(String keyword) {
            ds = keyword.isEmpty() ? dao.getAll() : dao.timKiem(keyword);
            model.setRowCount(0);
            for (NguyenLieu n : ds)
                model.addRow(new Object[]{n.getMaNL(), n.getTenNL(), n.getDonViTinh(), FormatUtil.soThuc(n.getSoLuongTon()),
                        FormatUtil.soThuc(n.getMucToiThieu()), n.sapHet() ? "Sắp hết" : "Đủ"});
        }

        @Override
        protected void fillForm(int row) {
            NguyenLieu n = ds.get(row);
            txtMa.setText(n.getMaNL());
            txtTen.setText(n.getTenNL());
            txtDvt.setText(n.getDonViTinh());
            txtTon.setText(FormatUtil.soThuc(n.getSoLuongTon()));
            txtToiThieu.setText(FormatUtil.soThuc(n.getMucToiThieu()));
            txtTon.setEditable(false);
            txtTon.setBackground(new Color(0xF0EBE3));
        }

        @Override
        protected void clearForm() {
            txtMa.setText("(tự sinh khi thêm)");
            txtTen.setText("");
            txtDvt.setText("");
            txtTon.setText("0");
            txtToiThieu.setText("0");
            txtTon.setEditable(true);
            txtTon.setBackground(Color.WHITE);
        }

        private NguyenLieu docForm(String ma, double tonCu) {
            String ten = txtTen.getText().trim().replaceAll("\\s+", " ");
            String dvt = txtDvt.getText().trim();
            if (ten.isEmpty() || ten.length() > 100) {
                UI.warn("Tên nguyên liệu không được để trống và tối đa 100 ký tự.");
                return null;
            }
            if (dvt.isEmpty() || dvt.length() > 20) {
                UI.warn("Đơn vị tính không được để trống và tối đa 20 ký tự (kg, lít, hộp, cái...).");
                return null;
            }
            double ton = ma == null ? FormatUtil.parseSoThuc(txtTon.getText()) : tonCu;
            if (ton < 0) {
                UI.warn("Tồn ban đầu phải là số không âm (tối đa 2 chữ số thập phân).");
                return null;
            }
            double mt = FormatUtil.parseSoThuc(txtToiThieu.getText());
            if (mt < 0) {
                UI.warn("Mức tối thiểu phải là số không âm (tối đa 2 chữ số thập phân).");
                return null;
            }
            return new NguyenLieu(ma, ten, dvt, ton, mt);
        }

        @Override
        protected void them() {
            NguyenLieu n = docForm(null, 0);
            if (n == null) return;
            dao.them(n);
            lamMoi();
        }

        @Override
        protected void sua() {
            NguyenLieu goc = ds.get(table.getSelectedRow());
            NguyenLieu n = docForm(goc.getMaNL(), goc.getSoLuongTon());
            if (n == null) return;
            dao.sua(n);
            lamMoi();
        }

        @Override
        protected void xoa() {
            try {
                dao.xoa(ds.get(table.getSelectedRow()).getMaNL());
            } catch (DaoException e) {
                throw PnlThucDon.fk(e, "Nguyên liệu này đã có phiếu nhập hoặc phiếu xuất nên không thể xóa.");
            }
            lamMoi();
        }
    }

    // =====================================================================================
    //  NHÀ CUNG CẤP
    // =====================================================================================
    static class PnlNhaCungCap extends CrudPanel {
        private final NhaCungCapDAO dao = new NhaCungCapDAO();
        private List<NhaCungCap> ds = new ArrayList<>();
        private final JTextField txtMa = UI.readOnly(10);
        private final JTextField txtTen = UI.field(10);
        private final JTextField txtSdt = UI.field(10);
        private final JTextField txtDiaChi = UI.field(10);

        PnlNhaCungCap() {
            super("Thông tin nhà cung cấp", "Mã NCC", "Tên nhà cung cấp", "SĐT", "Địa chỉ");
            init();
            UI.widths(table, 70, 220, 110, 240);
            clearForm();
        }

        @Override
        protected void buildForm(FormPanel f) {
            f.row("Mã NCC", txtMa);
            f.row("Tên", txtTen);
            f.row("SĐT", txtSdt);
            f.row("Địa chỉ", txtDiaChi);
        }

        @Override
        protected void load(String keyword) {
            ds = keyword.isEmpty() ? dao.getAll() : dao.timKiem(keyword);
            model.setRowCount(0);
            for (NhaCungCap n : ds) model.addRow(new Object[]{n.getMaNCC(), n.getTenNCC(), n.getSdt(), n.getDiaChi()});
        }

        @Override
        protected void fillForm(int row) {
            NhaCungCap n = ds.get(row);
            txtMa.setText(n.getMaNCC());
            txtTen.setText(n.getTenNCC());
            txtSdt.setText(n.getSdt() == null ? "" : n.getSdt());
            txtDiaChi.setText(n.getDiaChi() == null ? "" : n.getDiaChi());
        }

        @Override
        protected void clearForm() {
            txtMa.setText("(tự sinh khi thêm)");
            txtTen.setText("");
            txtSdt.setText("");
            txtDiaChi.setText("");
        }

        private NhaCungCap docForm(String ma) {
            String ten = txtTen.getText().trim().replaceAll("\\s+", " ");
            if (ten.isEmpty() || ten.length() > 100) {
                UI.warn("Tên nhà cung cấp không được để trống và tối đa 100 ký tự.");
                return null;
            }
            String sdt = txtSdt.getText().trim();
            if (!sdt.matches("^0\\d{9,10}$")) {
                UI.warn("Số điện thoại gồm 10 hoặc 11 chữ số và bắt đầu bằng số 0.");
                return null;
            }
            String dc = txtDiaChi.getText().trim();
            if (dc.length() > 200) {
                UI.warn("Địa chỉ tối đa 200 ký tự.");
                return null;
            }
            return new NhaCungCap(ma, ten, sdt, dc);
        }

        @Override
        protected void them() {
            NhaCungCap n = docForm(null);
            if (n == null) return;
            dao.them(n);
            lamMoi();
        }

        @Override
        protected void sua() {
            NhaCungCap n = docForm(ds.get(table.getSelectedRow()).getMaNCC());
            if (n == null) return;
            dao.sua(n);
            lamMoi();
        }

        @Override
        protected void xoa() {
            try {
                dao.xoa(ds.get(table.getSelectedRow()).getMaNCC());
            } catch (DaoException e) {
                throw PnlThucDon.fk(e, "Nhà cung cấp này đã có phiếu nhập nên không thể xóa.");
            }
            lamMoi();
        }
    }

    // =====================================================================================
    //  KHUNG CHUNG CHO PHIẾU NHẬP / PHIẾU XUẤT
    // =====================================================================================
    /** Bảng "tồn kho hiện tại" nhỏ bên phải hai màn hình lập phiếu. */
    private static class MiniTon extends JPanel {
        private final NguyenLieuDAO dao = new NguyenLieuDAO();
        private final DefaultTableModel mdl = UI.model("Nguyên liệu", "Tồn", "Tối thiểu");
        private final JTable tbl = UI.table(mdl);
        private List<NguyenLieu> ds = new ArrayList<>();

        MiniTon() {
            super(new BorderLayout(0, 6));
            setOpaque(false);
            setPreferredSize(new Dimension(330, 100));
            JLabel t = UI.title("Tồn kho hiện tại (đỏ = sắp hết)");
            add(t, BorderLayout.NORTH);
            UI.alignRight(tbl, 1, 2);
            UI.widths(tbl, 120, 90, 90);
            UI.warnRows(tbl, r -> r < ds.size() && ds.get(r).sapHet());
            add(UI.scroll(tbl), BorderLayout.CENTER);
        }

        List<NguyenLieu> load() {
            ds = dao.getAll();
            mdl.setRowCount(0);
            for (NguyenLieu n : ds)
                mdl.addRow(new Object[]{n.getTenNL(), sl(n.getSoLuongTon(), n.getDonViTinh()), sl(n.getMucToiThieu(), n.getDonViTinh())});
            return ds;
        }
    }

    private static <T> JComboBox<T> comboDong() {
        JComboBox<T> c = new JComboBox<>();
        c.setFont(AppColor.FONT);
        c.setPreferredSize(new Dimension(190, 30));
        return c;
    }

    // =====================================================================================
    //  PHIẾU NHẬP
    // =====================================================================================
    static class PnlPhieuNhap extends JPanel implements Refreshable {
        private final PhieuNhapDAO dao = new PhieuNhapDAO();
        private final NhaCungCapDAO nccDAO = new NhaCungCapDAO();
        private final JTextField txtMa = UI.readOnly(10);
        private final DateField dfNgay = new DateField(LocalDate.now());
        private final JTextField txtNV = UI.readOnly(10);
        private final JComboBox<NhaCungCap> cboNCC = comboDong();
        private final JComboBox<NguyenLieu> cboNL = comboDong();
        private final JTextField txtSL = UI.field(5);
        private final JTextField txtGia = UI.field(8);
        private final DefaultTableModel mdl = UI.model("Nguyên liệu", "ĐVT", "Số lượng", "Đơn giá", "Thành tiền");
        private final JTable tbl = UI.table(mdl);
        private final JLabel lblTong = new JLabel("0đ");
        private final List<ChiTietPhieuNhap> ct = new ArrayList<>();
        private final MiniTon mini = new MiniTon();

        PnlPhieuNhap() {
            if (!SessionManager.laQuanLy()) throw new SecurityException("Chỉ Quản lý mới được lập phiếu nhập.");
            setLayout(new BorderLayout(12, 0));
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            setBackground(AppColor.BG);
            txtNV.setText(SessionManager.getNhanVien().getHoTen());

            RoundedPanel card = new RoundedPanel(new BorderLayout(0, 10), Color.WHITE, AppColor.LINE, 10);
            card.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));
            JPanel north = new JPanel(new BorderLayout(0, 8));
            north.setOpaque(false);
            north.add(UI.title("Lập phiếu nhập"), BorderLayout.NORTH);
            JPanel dong1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            dong1.setOpaque(false);
            txtMa.setPreferredSize(new Dimension(90, 30));
            txtNV.setPreferredSize(new Dimension(150, 30));
            dong1.add(UI.label("Mã phiếu"));
            dong1.add(txtMa);
            dong1.add(UI.label("Ngày nhập"));
            dong1.add(dfNgay);
            dong1.add(UI.label("Nhân viên"));
            dong1.add(txtNV);
            JPanel dong2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            dong2.setOpaque(false);
            dong2.add(UI.label("Nhà cung cấp"));
            cboNCC.setPreferredSize(new Dimension(260, 30));
            dong2.add(cboNCC);
            JPanel dong3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            dong3.setOpaque(false);
            dong3.add(UI.label("Nguyên liệu"));
            dong3.add(cboNL);
            dong3.add(UI.label("SL"));
            dong3.add(txtSL);
            dong3.add(UI.label("Đơn giá"));
            dong3.add(txtGia);
            BoGocButton them = UI.button("+ Thêm", AppColor.GREEN);
            dong3.add(them);
            JPanel dongs = new JPanel(new GridLayout(0, 1, 0, 6));
            dongs.setOpaque(false);
            dongs.add(dong1);
            dongs.add(dong2);
            dongs.add(dong3);
            north.add(dongs, BorderLayout.CENTER);
            card.add(north, BorderLayout.NORTH);

            UI.alignCenter(tbl, 1);
            UI.alignRight(tbl, 2, 3, 4);
            UI.widths(tbl, 190, 60, 90, 100, 120);
            card.add(UI.scroll(tbl), BorderLayout.CENTER);

            JPanel south = new JPanel(new BorderLayout(0, 8));
            south.setOpaque(false);
            JPanel tong = new JPanel(new BorderLayout());
            tong.setOpaque(false);
            BoGocButton xoaDong = new BoGocButton("Xóa dòng chọn", AppColor.LATTE).foreground(AppColor.BROWN);
            tong.add(xoaDong, BorderLayout.WEST);
            JPanel tr = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            tr.setOpaque(false);
            tr.add(UI.bold("Tổng tiền phiếu"));
            lblTong.setFont(new Font(AppColor.FONT_NAME, Font.BOLD, 20));
            lblTong.setForeground(AppColor.GREEN);
            tr.add(lblTong);
            tong.add(tr, BorderLayout.EAST);
            south.add(tong, BorderLayout.NORTH);
            JPanel bt = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            bt.setOpaque(false);
            BoGocButton luu = UI.button("Lưu phiếu nhập", AppColor.GREEN);
            luu.setPreferredSize(new Dimension(150, 36));
            BoGocButton lm = new BoGocButton("Làm mới", AppColor.LATTE).foreground(AppColor.BROWN);
            BoGocButton ls = new BoGocButton("Lịch sử phiếu nhập", AppColor.BROWN).outline(AppColor.BROWN);
            ls.setPreferredSize(new Dimension(160, 32));
            bt.add(luu);
            bt.add(lm);
            bt.add(ls);
            south.add(bt, BorderLayout.SOUTH);
            card.add(south, BorderLayout.SOUTH);
            add(card, BorderLayout.CENTER);
            add(mini, BorderLayout.EAST);

            them.addActionListener(e -> themDong());
            xoaDong.addActionListener(e -> {
                int r = tbl.getSelectedRow();
                if (r < 0) {
                    UI.warn("Hãy chọn dòng cần xóa trong bảng.");
                    return;
                }
                ct.remove(r);
                hienBang();
            });
            luu.addActionListener(UI.on(this::luuPhieu));
            lm.addActionListener(UI.on(() -> {
                ct.clear();
                hienBang();
                refresh();
            }));
            ls.addActionListener(e -> new DlgLichSuPhieu(SwingUtilities.getWindowAncestor(this), true).setVisible(true));
        }

        @Override
        public void refresh() {
            UI.run(() -> {
                NhaCungCap nccCu = (NhaCungCap) cboNCC.getSelectedItem();
                NguyenLieu nlCu = (NguyenLieu) cboNL.getSelectedItem();
                cboNCC.removeAllItems();
                for (NhaCungCap n : nccDAO.getAll()) cboNCC.addItem(n);
                cboNL.removeAllItems();
                for (NguyenLieu n : mini.load()) cboNL.addItem(n);
                if (nccCu != null) cboNCC.setSelectedItem(nccCu);
                if (nlCu != null) cboNL.setSelectedItem(nlCu);
                txtMa.setText(dao.taoMa());
            });
        }

        private void themDong() {
            NguyenLieu nl = (NguyenLieu) cboNL.getSelectedItem();
            if (nl == null) {
                UI.warn("Chưa có nguyên liệu. Hãy thêm nguyên liệu ở thẻ Tồn kho trước.");
                return;
            }
            double sl = FormatUtil.parseSoThuc(txtSL.getText());
            if (sl <= 0) {
                UI.warn("Số lượng phải là số lớn hơn 0 (tối đa 2 chữ số thập phân), ví dụ 10 hoặc 2,5.");
                return;
            }
            double gia = FormatUtil.parseTien(txtGia.getText());
            if (gia < 0) {
                UI.warn("Đơn giá phải là số nguyên không âm, ví dụ 180000 hoặc 180.000.");
                return;
            }
            for (ChiTietPhieuNhap x : ct) {
                if (x.getNguyenLieu().equals(nl)) {
                    x.setSoLuong(x.getSoLuong() + sl);
                    x.setDonGia(gia);
                    hienBang();
                    return;
                }
            }
            ChiTietPhieuNhap d = new ChiTietPhieuNhap();
            d.setNguyenLieu(nl);
            d.setSoLuong(sl);
            d.setDonGia(gia);
            ct.add(d);
            hienBang();
            txtSL.setText("");
            txtGia.setText("");
        }

        private void hienBang() {
            mdl.setRowCount(0);
            double tong = 0;
            for (ChiTietPhieuNhap d : ct) {
                tong += d.thanhTien();
                mdl.addRow(new Object[]{d.getNguyenLieu().getTenNL(), d.getNguyenLieu().getDonViTinh(), FormatUtil.soThuc(d.getSoLuong()),
                        FormatUtil.tien(d.getDonGia()), FormatUtil.tien(d.thanhTien())});
            }
            lblTong.setText(FormatUtil.tienD(tong));
        }

        private void luuPhieu() {
            NhaCungCap ncc = (NhaCungCap) cboNCC.getSelectedItem();
            if (ncc == null) {
                UI.warn("Hãy chọn nhà cung cấp (thêm ở thẻ Nhà cung cấp nếu chưa có).");
                return;
            }
            LocalDate ngay = dfNgay.getDate();
            if (ngay == null || ngay.isAfter(LocalDate.now())) {
                UI.warn("Ngày nhập không được để trống hoặc ở tương lai.");
                return;
            }
            if (ct.isEmpty()) {
                UI.warn("Phiếu nhập chưa có nguyên liệu nào.");
                return;
            }
            PhieuNhap pn = new PhieuNhap();
            pn.setNgayNhap(ngay);
            pn.setNhanVien(SessionManager.getNhanVien());
            pn.setNhaCungCap(ncc);
            pn.setDsChiTiet(new ArrayList<>(ct));
            dao.luu(pn);
            UI.info("Đã lưu phiếu " + pn.getMaPN() + ", tổng tiền " + FormatUtil.tienD(pn.getTongTien()) + ". Tồn kho đã được cộng thêm.");
            ct.clear();
            hienBang();
            refresh();
        }
    }

    // =====================================================================================
    //  PHIẾU XUẤT
    // =====================================================================================
    static class PnlPhieuXuat extends JPanel implements Refreshable {
        private final PhieuXuatDAO dao = new PhieuXuatDAO();
        private final JTextField txtMa = UI.readOnly(10);
        private final DateField dfNgay = new DateField(LocalDate.now());
        private final JTextField txtNV = UI.readOnly(10);
        private final JTextField txtLyDo = UI.field(30);
        private final JComboBox<NguyenLieu> cboNL = comboDong();
        private final JTextField txtSL = UI.field(5);
        private final DefaultTableModel mdl = UI.model("Nguyên liệu", "ĐVT", "Số lượng xuất", "Tồn hiện tại");
        private final JTable tbl = UI.table(mdl);
        private final List<ChiTietPhieuXuat> ct = new ArrayList<>();
        private final MiniTon mini = new MiniTon();

        PnlPhieuXuat() {
            if (!SessionManager.laQuanLy()) throw new SecurityException("Chỉ Quản lý mới được lập phiếu xuất.");
            setLayout(new BorderLayout(12, 0));
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            setBackground(AppColor.BG);
            txtNV.setText(SessionManager.getNhanVien().getHoTen());

            RoundedPanel card = new RoundedPanel(new BorderLayout(0, 10), Color.WHITE, AppColor.LINE, 10);
            card.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));
            JPanel north = new JPanel(new BorderLayout(0, 8));
            north.setOpaque(false);
            north.add(UI.title("Lập phiếu xuất (hao hụt, đổ bỏ, kiểm kê)"), BorderLayout.NORTH);
            JPanel dong1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            dong1.setOpaque(false);
            txtMa.setPreferredSize(new Dimension(90, 30));
            txtNV.setPreferredSize(new Dimension(150, 30));
            dong1.add(UI.label("Mã phiếu"));
            dong1.add(txtMa);
            dong1.add(UI.label("Ngày xuất"));
            dong1.add(dfNgay);
            dong1.add(UI.label("Nhân viên"));
            dong1.add(txtNV);
            JPanel dong2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            dong2.setOpaque(false);
            dong2.add(UI.label("Lý do xuất"));
            txtLyDo.setPreferredSize(new Dimension(380, 30));
            dong2.add(txtLyDo);
            JPanel dong3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            dong3.setOpaque(false);
            dong3.add(UI.label("Nguyên liệu"));
            dong3.add(cboNL);
            dong3.add(UI.label("SL xuất"));
            dong3.add(txtSL);
            BoGocButton them = UI.button("+ Thêm", AppColor.GREEN);
            dong3.add(them);
            JPanel dongs = new JPanel(new GridLayout(0, 1, 0, 6));
            dongs.setOpaque(false);
            dongs.add(dong1);
            dongs.add(dong2);
            dongs.add(dong3);
            north.add(dongs, BorderLayout.CENTER);
            card.add(north, BorderLayout.NORTH);

            UI.alignCenter(tbl, 1);
            UI.alignRight(tbl, 2, 3);
            UI.widths(tbl, 220, 70, 120, 120);
            card.add(UI.scroll(tbl), BorderLayout.CENTER);

            JPanel bt = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            bt.setOpaque(false);
            BoGocButton xoaDong = new BoGocButton("Xóa dòng chọn", AppColor.LATTE).foreground(AppColor.BROWN);
            BoGocButton luu = UI.button("Lưu phiếu xuất", AppColor.GREEN);
            luu.setPreferredSize(new Dimension(150, 36));
            BoGocButton lm = new BoGocButton("Làm mới", AppColor.LATTE).foreground(AppColor.BROWN);
            BoGocButton ls = new BoGocButton("Lịch sử phiếu xuất", AppColor.BROWN).outline(AppColor.BROWN);
            ls.setPreferredSize(new Dimension(160, 32));
            bt.add(luu);
            bt.add(xoaDong);
            bt.add(lm);
            bt.add(ls);
            card.add(bt, BorderLayout.SOUTH);
            add(card, BorderLayout.CENTER);
            add(mini, BorderLayout.EAST);

            them.addActionListener(e -> themDong());
            xoaDong.addActionListener(e -> {
                int r = tbl.getSelectedRow();
                if (r < 0) {
                    UI.warn("Hãy chọn dòng cần xóa trong bảng.");
                    return;
                }
                ct.remove(r);
                hienBang();
            });
            luu.addActionListener(UI.on(this::luuPhieu));
            lm.addActionListener(UI.on(() -> {
                ct.clear();
                txtLyDo.setText("");
                hienBang();
                refresh();
            }));
            ls.addActionListener(e -> new DlgLichSuPhieu(SwingUtilities.getWindowAncestor(this), false).setVisible(true));
        }

        @Override
        public void refresh() {
            UI.run(() -> {
                NguyenLieu nlCu = (NguyenLieu) cboNL.getSelectedItem();
                cboNL.removeAllItems();
                for (NguyenLieu n : mini.load()) cboNL.addItem(n);
                if (nlCu != null) cboNL.setSelectedItem(nlCu);
                txtMa.setText(dao.taoMa());
            });
        }

        private void themDong() {
            NguyenLieu nl = (NguyenLieu) cboNL.getSelectedItem();
            if (nl == null) {
                UI.warn("Chưa có nguyên liệu để xuất.");
                return;
            }
            double sl = FormatUtil.parseSoThuc(txtSL.getText());
            if (sl <= 0) {
                UI.warn("Số lượng xuất phải là số lớn hơn 0 (tối đa 2 chữ số thập phân).");
                return;
            }
            double daCo = 0;
            ChiTietPhieuXuat gop = null;
            for (ChiTietPhieuXuat x : ct) if (x.getNguyenLieu().equals(nl)) {
                gop = x;
                daCo = x.getSoLuong();
            }
            if (daCo + sl > nl.getSoLuongTon()) {
                UI.warn("Không đủ tồn kho. " + nl.getTenNL() + " chỉ còn " + sl(nl.getSoLuongTon(), nl.getDonViTinh()) + ".");
                return;
            }
            if (gop != null) gop.setSoLuong(daCo + sl);
            else {
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
            for (ChiTietPhieuXuat d : ct)
                mdl.addRow(new Object[]{d.getNguyenLieu().getTenNL(), d.getNguyenLieu().getDonViTinh(), FormatUtil.soThuc(d.getSoLuong()),
                        FormatUtil.soThuc(d.getNguyenLieu().getSoLuongTon())});
        }

        private void luuPhieu() {
            LocalDate ngay = dfNgay.getDate();
            if (ngay == null || ngay.isAfter(LocalDate.now())) {
                UI.warn("Ngày xuất không được để trống hoặc ở tương lai.");
                return;
            }
            String ly = txtLyDo.getText().trim();
            if (!Validator.khongRong(ly) || ly.length() > 200) {
                UI.warn("Hãy nhập lý do xuất (tối đa 200 ký tự).");
                return;
            }
            if (ct.isEmpty()) {
                UI.warn("Phiếu xuất chưa có nguyên liệu nào.");
                return;
            }
            PhieuXuat px = new PhieuXuat();
            px.setNgayXuat(ngay);
            px.setNhanVien(SessionManager.getNhanVien());
            px.setLyDo(ly);
            px.setDsChiTiet(new ArrayList<>(ct));
            dao.luu(px);
            UI.info("Đã lưu phiếu " + px.getMaPX() + ". Tồn kho đã được trừ.");
            ct.clear();
            txtLyDo.setText("");
            hienBang();
            refresh();
        }
    }
}
