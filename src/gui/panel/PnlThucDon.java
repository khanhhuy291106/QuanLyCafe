package gui.panel;

import dao.*;
import entity.*;
import gui.component.*;
import gui.util.AppColor;
import gui.util.FormatUtil;
import gui.util.SessionManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực đơn: quản lý món, loại món, size, topping.
 * CHỈ QUẢN LÝ được vào màn hình này (nhân viên chỉ chọn món ở màn hình Bán hàng).
 */
public class PnlThucDon extends JPanel implements Refreshable {
    private final JTabbedPane tabs = new JTabbedPane();

    public PnlThucDon() {
        if (!SessionManager.laQuanLy()) throw new SecurityException("Chỉ Quản lý mới được quản lý thực đơn.");
        setLayout(new BorderLayout());
        setBackground(AppColor.BG);
        tabs.setFont(AppColor.FONT_B);
        tabs.addTab("Món", new PnlMon());
        tabs.addTab("Loại món", new PnlLoaiMon());
        tabs.addTab("Size", new PnlSize());
        tabs.addTab("Topping", new PnlTopping());
        tabs.addChangeListener(e -> refresh());
        add(tabs, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        Component c = tabs.getSelectedComponent();
        if (c instanceof Refreshable) ((Refreshable) c).refresh();
    }

    /** Nếu lỗi là do khóa ngoại thì đổi sang lời nhắc dễ hiểu hơn. */
    static DaoException fk(DaoException e, String msg) {
        return e.getMessage() != null && e.getMessage().startsWith("Dữ liệu này đang") ? new DaoException(msg) : e;
    }

    // =====================================================================================
    //  MÓN
    // =====================================================================================
    static class PnlMon extends CrudPanel {
        private final MonDAO dao = new MonDAO();
        private final LoaiMonDAO loaiDAO = new LoaiMonDAO();
        private List<Mon> ds = new ArrayList<>();
        private final JTextField txtMa = UI.readOnly(10);
        private final JTextField txtTen = UI.field(10);
        private final JTextField txtGia = UI.field(10);
        private final JComboBox<LoaiMon> cboLoai = new JComboBox<>();
        private final JComboBox<Object> cboLoc = new JComboBox<>();
        private final JComboBox<String> cboTT = UI.combo(new String[]{"Đang bán", "Ngừng bán"});
        private final JLabel lblAnh = new JLabel("", SwingConstants.CENTER);
        private String duongDanAnh;
        private boolean dangNap;

        PnlMon() {
            super("Thông tin món", "Mã món", "Tên món", "Loại", "Giá cơ bản (M)", "Trạng thái");
            setFormWidth(340);
            init();
            cboLoai.setFont(AppColor.FONT);
            cboLoai.setPreferredSize(new Dimension(100, 30));
            cboLoc.setFont(AppColor.FONT);
            cboLoc.setPreferredSize(new Dimension(140, 30));
            pnlSearch.add(cboLoc);
            cboLoc.addActionListener(e -> {
                if (!dangNap) refresh();
            });
            UI.alignRight(table, 3);
            UI.badge(table, 4);
            UI.widths(table, 70, 220, 110, 110, 110);
            clearForm();
            JLabel note = UI.muted("Món “Ngừng bán” không hiện ở màn hình Bán hàng nhưng vẫn giữ trong hóa đơn cũ. Nên chuyển trạng thái thay vì xóa món đã từng bán.");
            pnlSouth.add(note, BorderLayout.CENTER);
        }

        @Override
        protected void buildForm(FormPanel f) {
            f.row("Mã món", txtMa);
            f.row("Tên món", txtTen);
            f.row("Loại món", cboLoai);
            f.row("Giá cơ bản", txtGia);
            f.row("Trạng thái", cboTT);
            lblAnh.setPreferredSize(new Dimension(130, 96));
            lblAnh.setOpaque(true);
            lblAnh.setBackground(AppColor.LATTE);
            lblAnh.setBorder(BorderFactory.createLineBorder(AppColor.LINE));
            JPanel pa = new JPanel(new BorderLayout(0, 6));
            pa.setOpaque(false);
            JPanel box = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            box.setOpaque(false);
            box.add(lblAnh);
            pa.add(box, BorderLayout.CENTER);
            BoGocButton b = new BoGocButton("Chọn ảnh...", AppColor.LATTE).foreground(AppColor.BROWN);
            b.setPreferredSize(new Dimension(130, 28));
            JPanel bb = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            bb.setOpaque(false);
            bb.add(b);
            pa.add(bb, BorderLayout.SOUTH);
            b.addActionListener(e -> chonAnh());
            f.row("Hình ảnh", pa);
        }

        @Override
        protected void load(String keyword) {
            dangNap = true;
            try {
                List<LoaiMon> dsLoai = loaiDAO.getAll();
                Object locCu = cboLoc.getSelectedItem();
                LoaiMon loaiCu = (LoaiMon) cboLoai.getSelectedItem();
                cboLoai.removeAllItems();
                cboLoc.removeAllItems();
                cboLoc.addItem("Tất cả loại");
                for (LoaiMon l : dsLoai) {
                    cboLoai.addItem(l);
                    cboLoc.addItem(l);
                }
                if (loaiCu != null) cboLoai.setSelectedItem(loaiCu);
                if (locCu != null) cboLoc.setSelectedItem(locCu);
            } finally {
                dangNap = false;
            }
            Object loc = cboLoc.getSelectedItem();
            ds = dao.timKiem(keyword, loc instanceof LoaiMon ? ((LoaiMon) loc).getMaLoai() : null);
            model.setRowCount(0);
            for (Mon m : ds)
                model.addRow(new Object[]{m.getMaMon(), m.getTenMon(), m.getLoaiMon().getTenLoai(), FormatUtil.tien(m.getGiaCoBan()),
                        m.isTrangThai() ? "Đang bán" : "Ngừng bán"});
        }

        @Override
        protected void fillForm(int row) {
            Mon m = ds.get(row);
            txtMa.setText(m.getMaMon());
            txtTen.setText(m.getTenMon());
            txtGia.setText(FormatUtil.tien(m.getGiaCoBan()));
            cboLoai.setSelectedItem(m.getLoaiMon());
            cboTT.setSelectedIndex(m.isTrangThai() ? 0 : 1);
            duongDanAnh = m.getHinhAnh();
            hienAnh();
        }

        @Override
        protected void clearForm() {
            txtMa.setText("(tự sinh khi thêm)");
            txtTen.setText("");
            txtGia.setText("");
            cboTT.setSelectedIndex(0);
            if (cboLoai.getItemCount() > 0) cboLoai.setSelectedIndex(0);
            duongDanAnh = null;
            hienAnh();
        }

        private void hienAnh() {
            File f = duongDanAnh == null ? null : new File(duongDanAnh);
            if (f != null && f.exists()) {
                Image img = new ImageIcon(duongDanAnh).getImage();
                if (img.getWidth(null) > 0) {
                    double sc = Math.min(126.0 / img.getWidth(null), 92.0 / img.getHeight(null));
                    lblAnh.setIcon(new ImageIcon(img.getScaledInstance((int) (img.getWidth(null) * sc), (int) (img.getHeight(null) * sc), Image.SCALE_SMOOTH)));
                    return;
                }
            }
            lblAnh.setIcon(new CupIcon(56, new Color(0xB89570), true));
        }

        private void chonAnh() {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Ảnh (png, jpg, jpeg, gif)", "png", "jpg", "jpeg", "gif"));
            if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
            File src = fc.getSelectedFile();
            try {
                Path dir = Paths.get("resources", "images", "mon");
                Files.createDirectories(dir);
                String n = src.getName();
                String ext = n.contains(".") ? n.substring(n.lastIndexOf('.')).toLowerCase() : ".png";
                String name = "mon_" + System.currentTimeMillis() + ext;
                Files.copy(src.toPath(), dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
                duongDanAnh = "resources/images/mon/" + name;
                hienAnh();
            } catch (IOException ex) {
                UI.error("Không sao chép được ảnh: " + ex.getMessage());
            }
        }

        /** Đọc form, kiểm tra hợp lệ. Trả null nếu có lỗi (đã báo cho người dùng). */
        private Mon docForm(String ma) {
            String ten = txtTen.getText().trim().replaceAll("\\s+", " ");
            if (ten.isEmpty() || ten.length() > 100) {
                UI.warn("Tên món không được để trống và tối đa 100 ký tự.");
                return null;
            }
            double gia = FormatUtil.parseTien(txtGia.getText());
            if (gia < 0) {
                UI.warn("Giá cơ bản phải là số nguyên không âm, ví dụ 29000 hoặc 29.000.");
                return null;
            }
            LoaiMon loai = (LoaiMon) cboLoai.getSelectedItem();
            if (loai == null) {
                UI.warn("Chưa có loại món. Hãy tạo loại món ở thẻ “Loại món” trước.");
                return null;
            }
            return new Mon(ma, ten, gia, duongDanAnh, loai, cboTT.getSelectedIndex() == 0);
        }

        @Override
        protected void them() {
            Mon m = docForm(null);
            if (m == null) return;
            dao.them(m);
            lamMoi();
            UI.info("Đã thêm món " + m.getMaMon() + " - " + m.getTenMon() + ".");
        }

        @Override
        protected void sua() {
            Mon m = docForm(ds.get(table.getSelectedRow()).getMaMon());
            if (m == null) return;
            dao.sua(m);
            lamMoi();
            UI.info("Đã cập nhật món " + m.getMaMon() + ".");
        }

        @Override
        protected void xoa() {
            String ma = ds.get(table.getSelectedRow()).getMaMon();
            try {
                dao.xoa(ma);
            } catch (DaoException e) {
                throw fk(e, "Món này đã từng được bán nên không thể xóa. Hãy đổi trạng thái sang “Ngừng bán”.");
            }
            lamMoi();
        }
    }

    // =====================================================================================
    //  LOẠI MÓN
    // =====================================================================================
    static class PnlLoaiMon extends CrudPanel {
        private final LoaiMonDAO dao = new LoaiMonDAO();
        private List<LoaiMon> ds = new ArrayList<>();
        private final JTextField txtMa = UI.readOnly(10);
        private final JTextField txtTen = UI.field(10);

        PnlLoaiMon() {
            super("Thông tin loại món", "Mã loại", "Tên loại");
            init();
            UI.widths(table, 100, 300);
            clearForm();
            pnlSouth.add(UI.muted("Loại “Bánh” là loại đặc biệt: món thuộc loại này không có size, đường, đá và topping."), BorderLayout.CENTER);
        }

        @Override
        protected void buildForm(FormPanel f) {
            f.row("Mã loại", txtMa);
            f.row("Tên loại", txtTen);
        }

        @Override
        protected void load(String keyword) {
            ds = new ArrayList<>();
            for (LoaiMon l : dao.getAll())
                if (keyword.isEmpty() || l.getTenLoai().toLowerCase().contains(keyword.toLowerCase())) ds.add(l);
            model.setRowCount(0);
            for (LoaiMon l : ds) model.addRow(new Object[]{l.getMaLoai(), l.getTenLoai()});
        }

        @Override
        protected void fillForm(int row) {
            txtMa.setText(ds.get(row).getMaLoai());
            txtTen.setText(ds.get(row).getTenLoai());
        }

        @Override
        protected void clearForm() {
            txtMa.setText("(tự sinh khi thêm)");
            txtTen.setText("");
        }

        private String ten() {
            String t = txtTen.getText().trim().replaceAll("\\s+", " ");
            if (t.isEmpty() || t.length() > 50) {
                UI.warn("Tên loại không được để trống và tối đa 50 ký tự.");
                return null;
            }
            return t;
        }

        @Override
        protected void them() {
            String t = ten();
            if (t == null) return;
            dao.them(new LoaiMon(null, t));
            lamMoi();
        }

        @Override
        protected void sua() {
            String t = ten();
            if (t == null) return;
            dao.sua(new LoaiMon(ds.get(table.getSelectedRow()).getMaLoai(), t));
            lamMoi();
        }

        @Override
        protected void xoa() {
            try {
                dao.xoa(ds.get(table.getSelectedRow()).getMaLoai());
            } catch (DaoException e) {
                throw fk(e, "Loại món này đang có món thuộc về nó nên không thể xóa.");
            }
            lamMoi();
        }
    }

    // =====================================================================================
    //  SIZE
    // =====================================================================================
    static class PnlSize extends CrudPanel {
        private final SizeDAO dao = new SizeDAO();
        private List<Size> ds = new ArrayList<>();
        private final JTextField txtMa = UI.readOnly(10);
        private final JTextField txtTen = UI.field(10);
        private final JTextField txtPhuThu = UI.field(10);

        PnlSize() {
            super("Thông tin size", "Mã size", "Tên size", "Phụ thu");
            init();
            UI.alignRight(table, 2);
            UI.widths(table, 100, 150, 150);
            clearForm();
            pnlSouth.add(UI.muted("Giá một ly = giá cơ bản của món (size M) + phụ thu của size. Phụ thu có thể âm, ví dụ size S: -3.000."), BorderLayout.CENTER);
        }

        @Override
        protected void buildForm(FormPanel f) {
            f.row("Mã size", txtMa);
            f.row("Tên size", txtTen);
            f.row("Phụ thu (đ)", txtPhuThu);
        }

        static double parseCoDau(String s) {
            String t = s == null ? "" : s.trim().replace(".", "");
            return t.matches("-?\\d{1,7}") ? Double.parseDouble(t) : Double.NaN;
        }

        static String hienPhuThu(double v) {
            return v > 0 ? "+" + FormatUtil.tien(v) : (v < 0 ? "-" + FormatUtil.tien(-v) : "0");
        }

        @Override
        protected void load(String keyword) {
            ds = new ArrayList<>();
            for (Size s : dao.getAll()) if (keyword.isEmpty() || s.getTenSize().toLowerCase().contains(keyword.toLowerCase())) ds.add(s);
            model.setRowCount(0);
            for (Size s : ds) model.addRow(new Object[]{s.getMaSize(), s.getTenSize(), hienPhuThu(s.getPhuThu())});
        }

        @Override
        protected void fillForm(int row) {
            Size s = ds.get(row);
            txtMa.setText(s.getMaSize());
            txtTen.setText(s.getTenSize());
            txtPhuThu.setText(String.valueOf((long) s.getPhuThu()));
        }

        @Override
        protected void clearForm() {
            txtMa.setText("(tự sinh khi thêm)");
            txtTen.setText("");
            txtPhuThu.setText("0");
        }

        private Size docForm(String ma) {
            String t = txtTen.getText().trim();
            if (!t.matches("[\\p{L}0-9 ]{1,10}")) {
                UI.warn("Tên size gồm 1 đến 10 chữ cái hoặc chữ số, ví dụ S, M, L, XL.");
                return null;
            }
            double p = parseCoDau(txtPhuThu.getText());
            if (Double.isNaN(p)) {
                UI.warn("Phụ thu phải là số nguyên, ví dụ 5000 hoặc -3000.");
                return null;
            }
            return new Size(ma, t, p);
        }

        @Override
        protected void them() {
            Size s = docForm(null);
            if (s == null) return;
            dao.them(s);
            lamMoi();
        }

        @Override
        protected void sua() {
            Size s = docForm(ds.get(table.getSelectedRow()).getMaSize());
            if (s == null) return;
            dao.sua(s);
            lamMoi();
        }

        @Override
        protected void xoa() {
            try {
                dao.xoa(ds.get(table.getSelectedRow()).getMaSize());
            } catch (DaoException e) {
                throw fk(e, "Size này đã được dùng trong hóa đơn nên không thể xóa.");
            }
            lamMoi();
        }
    }

    // =====================================================================================
    //  TOPPING
    // =====================================================================================
    static class PnlTopping extends CrudPanel {
        private final ToppingDAO dao = new ToppingDAO();
        private List<Topping> ds = new ArrayList<>();
        private final JTextField txtMa = UI.readOnly(10);
        private final JTextField txtTen = UI.field(10);
        private final JTextField txtGia = UI.field(10);
        private final JComboBox<String> cboTT = UI.combo(new String[]{"Đang bán", "Ngừng bán"});

        PnlTopping() {
            super("Thông tin topping", "Mã", "Tên topping", "Giá", "Trạng thái");
            init();
            UI.alignRight(table, 2);
            UI.badge(table, 3);
            UI.widths(table, 80, 220, 100, 120);
            clearForm();
        }

        @Override
        protected void buildForm(FormPanel f) {
            f.row("Mã topping", txtMa);
            f.row("Tên topping", txtTen);
            f.row("Giá (đ)", txtGia);
            f.row("Trạng thái", cboTT);
        }

        @Override
        protected void load(String keyword) {
            ds = new ArrayList<>();
            for (Topping t : dao.getAll()) if (keyword.isEmpty() || t.getTenTopping().toLowerCase().contains(keyword.toLowerCase())) ds.add(t);
            model.setRowCount(0);
            for (Topping t : ds)
                model.addRow(new Object[]{t.getMaTopping(), t.getTenTopping(), FormatUtil.tien(t.getGia()), t.isTrangThai() ? "Đang bán" : "Ngừng bán"});
        }

        @Override
        protected void fillForm(int row) {
            Topping t = ds.get(row);
            txtMa.setText(t.getMaTopping());
            txtTen.setText(t.getTenTopping());
            txtGia.setText(FormatUtil.tien(t.getGia()));
            cboTT.setSelectedIndex(t.isTrangThai() ? 0 : 1);
        }

        @Override
        protected void clearForm() {
            txtMa.setText("(tự sinh khi thêm)");
            txtTen.setText("");
            txtGia.setText("");
            cboTT.setSelectedIndex(0);
        }

        private Topping docForm(String ma) {
            String t = txtTen.getText().trim().replaceAll("\\s+", " ");
            if (t.isEmpty() || t.length() > 50) {
                UI.warn("Tên topping không được để trống và tối đa 50 ký tự.");
                return null;
            }
            double g = FormatUtil.parseTien(txtGia.getText());
            if (g < 0) {
                UI.warn("Giá topping phải là số nguyên không âm.");
                return null;
            }
            return new Topping(ma, t, g, cboTT.getSelectedIndex() == 0);
        }

        @Override
        protected void them() {
            Topping t = docForm(null);
            if (t == null) return;
            dao.them(t);
            lamMoi();
        }

        @Override
        protected void sua() {
            Topping t = docForm(ds.get(table.getSelectedRow()).getMaTopping());
            if (t == null) return;
            dao.sua(t);
            lamMoi();
        }

        @Override
        protected void xoa() {
            try {
                dao.xoa(ds.get(table.getSelectedRow()).getMaTopping());
            } catch (DaoException e) {
                throw fk(e, "Topping này đã được dùng trong hóa đơn nên không thể xóa. Hãy đổi trạng thái sang “Ngừng bán”.");
            }
            lamMoi();
        }
    }
}
