package gui.panel;

import dao.*;
import entity.*;
import gui.component.*;
import gui.dialog.DlgHoaDon;
import gui.dialog.DlgThanhToan;
import gui.util.AppColor;
import gui.util.FormatUtil;
import gui.util.SessionManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

/**
 * Màn hình bán hàng: chọn món bên trái, giỏ hàng + tùy chọn + thanh toán bên phải.
 * Dành cho Thu ngân và Quản lý (Pha chế không có màn hình này).
 */
public class PnlBanHang extends JPanel implements Refreshable {
    private final MonDAO monDAO = new MonDAO();
    private final LoaiMonDAO loaiDAO = new LoaiMonDAO();
    private final SizeDAO sizeDAO = new SizeDAO();
    private final ToppingDAO toppingDAO = new ToppingDAO();
    private final KhuyenMaiDAO kmDAO = new KhuyenMaiDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();

    private List<Mon> dsMon = new ArrayList<>();
    private List<LoaiMon> dsLoai = new ArrayList<>();
    private List<Size> dsSize = new ArrayList<>();
    private List<Topping> dsTopping = new ArrayList<>();
    private final List<ChiTietHoaDon> gio = new ArrayList<>();
    private String locLoai = null;
    private boolean dangNap = false;
    private final Map<String, Image> cacheAnh = new HashMap<>();

    // --- bên trái
    private final JTextField txtTim = UI.field(16);
    private final JPanel pnlChips = new JPanel(new WrapLayout(FlowLayout.LEFT, 6, 4));
    private final JPanel pnlLuoi = new JPanel(new GridLayout(0, 3, 10, 10));

    // --- bên phải
    private final JLabel lblDon = new JLabel("Đơn #001  —  Mang đi");
    private final DefaultTableModel mdlGio = UI.model("Món", "Size", "SL", "Thành tiền");
    private final JTable tblGio = UI.table(mdlGio);
    private final JLabel lblMonChon = UI.bold("Chưa chọn món");
    private final JPanel pnlSize = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private ButtonGroup grpSize = new ButtonGroup();
    private final List<JRadioButton> rdSize = new ArrayList<>();
    private final JComboBox<String> cboDuong = UI.combo(new String[]{"100%", "70%", "50%", "30%", "0%"});
    private final JComboBox<String> cboDa = UI.combo(new String[]{"Nhiều", "Vừa", "Ít", "Không"});
    private final JSpinner spSL = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private final JPanel pnlTopping = new JPanel(new GridLayout(0, 2, 4, 2));
    private final List<JCheckBox> chkTopping = new ArrayList<>();
    private final JTextField txtGhiChu = UI.field(10);
    private final JComboBox<Object> cboKM = new JComboBox<>();
    private final JLabel lblTamTinh = UI.label("0đ");
    private final JLabel lblGiam = UI.label("0đ");
    private final JLabel lblTong = new JLabel("0đ");

    public PnlBanHang() {
        if (SessionManager.laPhaChe()) throw new SecurityException("Pha chế không có quyền bán hàng.");
        setLayout(new BorderLayout(12, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(AppColor.BG);
        add(buildTrai(), BorderLayout.CENTER);
        add(buildPhai(), BorderLayout.EAST);
        loadOptions();
        taiDuLieu();
    }

    // =========================================================== dựng giao diện
    private JComponent buildTrai() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        JPanel tim = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tim.setOpaque(false);
        txtTim.setToolTipText("Gõ tên món để lọc");
        tim.add(txtTim);
        tim.add(UI.muted("Gõ tên món để lọc, bấm vào món để thêm vào đơn"));
        top.add(tim, BorderLayout.NORTH);
        pnlChips.setOpaque(false);
        top.add(pnlChips, BorderLayout.CENTER);
        p.add(top, BorderLayout.NORTH);

        pnlLuoi.setOpaque(false);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(pnlLuoi, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(wrap);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        p.add(sp, BorderLayout.CENTER);

        txtTim.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                locMon();
            }

            public void removeUpdate(DocumentEvent e) {
                locMon();
            }

            public void changedUpdate(DocumentEvent e) {
                locMon();
            }
        });
        return p;
    }

    private JComponent buildPhai() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(392, 100));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(0, 0, 8, 0);

        // tiêu đề đơn
        RoundedPanel head = new RoundedPanel(new BorderLayout(), AppColor.BROWN, null, 8);
        head.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        lblDon.setFont(AppColor.FONT_B);
        lblDon.setForeground(Color.WHITE);
        head.add(lblDon, BorderLayout.WEST);
        JLabel nv = new JLabel("NV: " + SessionManager.getNhanVien().getHoTen());
        nv.setFont(AppColor.FONT_SMALL);
        nv.setForeground(AppColor.LATTE);
        head.add(nv, BorderLayout.EAST);
        g.gridy = 0;
        g.weighty = 0;
        p.add(head, g);

        // giỏ hàng
        UI.alignCenter(tblGio, 1, 2);
        UI.alignRight(tblGio, 3);
        UI.widths(tblGio, 150, 40, 36, 80);
        tblGio.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadOptions();
        });
        JScrollPane spGio = UI.scroll(tblGio);
        spGio.setPreferredSize(new Dimension(100, 150));
        g.gridy = 1;
        g.weighty = 1;
        p.add(spGio, g);
        g.weighty = 0;

        // tùy chọn
        g.gridy = 2;
        p.add(buildTuyChon(), g);

        // khuyến mãi + tổng
        JPanel km = new JPanel(new BorderLayout(8, 0));
        km.setOpaque(false);
        km.add(UI.label("Khuyến mãi"), BorderLayout.WEST);
        cboKM.setFont(AppColor.FONT);
        cboKM.setPreferredSize(new Dimension(100, 30));
        km.add(cboKM, BorderLayout.CENTER);
        g.gridy = 3;
        p.add(km, g);
        cboKM.addActionListener(e -> {
            if (!dangNap) tinhTong();
        });

        JPanel tong = new JPanel(new GridLayout(0, 1, 0, 3));
        tong.setOpaque(false);
        tong.add(dongTong("Tạm tính", lblTamTinh));
        lblGiam.setForeground(AppColor.RED);
        tong.add(dongTong("Giảm giá", lblGiam));
        JPanel tc = new JPanel(new BorderLayout());
        tc.setOpaque(false);
        tc.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppColor.LINE));
        JLabel tt = new JLabel("TỔNG CỘNG");
        tt.setFont(AppColor.FONT_H2);
        tc.add(tt, BorderLayout.WEST);
        lblTong.setFont(new Font(AppColor.FONT_NAME, Font.BOLD, 22));
        lblTong.setForeground(AppColor.GREEN);
        tc.add(lblTong, BorderLayout.EAST);
        tong.add(tc);
        g.gridy = 4;
        p.add(tong, g);

        // nút
        JPanel bt = new JPanel(new GridLayout(1, 3, 6, 0));
        bt.setOpaque(false);
        BoGocButton btnXoa = new BoGocButton("Xóa món", AppColor.LATTE).foreground(AppColor.BROWN);
        BoGocButton btnLamMoi = new BoGocButton("Làm mới menu", AppColor.LATTE).foreground(AppColor.BROWN);
        BoGocButton btnHuy = UI.button("Hủy đơn", AppColor.RED);
        bt.add(btnXoa);
        bt.add(btnLamMoi);
        bt.add(btnHuy);
        g.gridy = 5;
        p.add(bt, g);
        BoGocButton btnTT = UI.button("THANH TOÁN", AppColor.GREEN);
        btnTT.setFont(new Font(AppColor.FONT_NAME, Font.BOLD, 16));
        btnTT.setPreferredSize(new Dimension(100, 46));
        g.gridy = 6;
        g.insets = new Insets(0, 0, 0, 0);
        p.add(btnTT, g);

        btnXoa.addActionListener(e -> xoaMon());
        btnLamMoi.addActionListener(UI.on(this::taiDuLieu));
        btnHuy.addActionListener(e -> huyDon());
        btnTT.addActionListener(e -> thanhToan());
        return p;
    }

    private JPanel dongTong(String ten, JLabel gt) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(UI.label(ten), BorderLayout.WEST);
        p.add(gt, BorderLayout.EAST);
        return p;
    }

    private JComponent buildTuyChon() {
        RoundedPanel card = new RoundedPanel(new GridBagLayout(), Color.WHITE, AppColor.LINE, 8);
        card.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 0, 3, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 4;
        card.add(lblMonChon, g);
        g.gridwidth = 1;

        g.gridy = 1;
        g.gridx = 0;
        card.add(UI.label("Size"), g);
        pnlSize.setOpaque(false);
        g.gridx = 1;
        g.gridwidth = 3;
        card.add(pnlSize, g);
        g.gridwidth = 1;

        g.gridy = 2;
        g.gridx = 0;
        card.add(UI.label("Đường"), g);
        g.gridx = 1;
        g.weightx = 1;
        card.add(cboDuong, g);
        g.weightx = 0;
        g.gridx = 2;
        card.add(UI.label("Đá"), g);
        g.gridx = 3;
        g.weightx = 1;
        card.add(cboDa, g);
        g.weightx = 0;

        g.gridy = 3;
        g.gridx = 0;
        card.add(UI.label("Số lượng"), g);
        spSL.setFont(AppColor.FONT);
        spSL.setPreferredSize(new Dimension(70, 30));
        g.gridx = 1;
        g.fill = GridBagConstraints.NONE;
        card.add(spSL, g);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridy = 4;
        g.gridx = 0;
        g.anchor = GridBagConstraints.NORTHWEST;
        card.add(UI.label("Topping"), g);
        pnlTopping.setOpaque(false);
        g.gridx = 1;
        g.gridwidth = 3;
        card.add(pnlTopping, g);
        g.gridwidth = 1;
        g.anchor = GridBagConstraints.WEST;

        g.gridy = 5;
        g.gridx = 0;
        card.add(UI.label("Ghi chú"), g);
        g.gridx = 1;
        g.gridwidth = 3;
        card.add(txtGhiChu, g);

        cboDuong.addActionListener(e -> capNhatTuyChon());
        cboDa.addActionListener(e -> capNhatTuyChon());
        spSL.addChangeListener(e -> capNhatTuyChon());
        txtGhiChu.addActionListener(e -> capNhatTuyChon());
        txtGhiChu.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                capNhatTuyChon();
            }
        });
        return card;
    }

    // =========================================================== dữ liệu
    @Override
    public void refresh() {
        taiDuLieu();
    }

    private void taiDuLieu() {
        boolean ok = UI.run(() -> {
            dsMon = monDAO.getDangBan();
            dsLoai = loaiDAO.getAll();
            dsSize = sizeDAO.getAll();
            dsTopping = toppingDAO.getDangBan();
        });
        if (!ok) return;
        dungChips();
        dungTuyChon();
        locMon();
        loadOptions();
        capNhatSoDon();
    }

    private void capNhatSoDon() {
        try {
            lblDon.setText("Đơn #" + String.format("%03d", hoaDonDAO.soThuTuTiepTheo()) + "  —  Mang đi");
        } catch (DaoException e) {
            lblDon.setText("Đơn mới  —  Mang đi");
        }
    }

    private void dungChips() {
        pnlChips.removeAll();
        pnlChips.add(chip("Tất cả", null));
        for (LoaiMon l : dsLoai) pnlChips.add(chip(l.getTenLoai(), l.getMaLoai()));
        pnlChips.revalidate();
        pnlChips.repaint();
    }

    private BoGocButton chip(String ten, String maLoai) {
        boolean active = Objects.equals(maLoai, locLoai);
        BoGocButton b = new BoGocButton(ten, active ? AppColor.ACCENT : new Color(0xEFE6D8)).radius(26);
        b.foreground(active ? Color.WHITE : AppColor.BROWN);
        b.setPreferredSize(new Dimension(Math.max(64, b.getFontMetrics(b.getFont()).stringWidth(ten) + 28), 28));
        b.addActionListener(e -> {
            locLoai = maLoai;
            dungChips();
            locMon();
        });
        return b;
    }

    private void dungTuyChon() {
        pnlSize.removeAll();
        rdSize.clear();
        grpSize = new ButtonGroup();
        for (Size s : dsSize) {
            JRadioButton r = UI.radio(s.getTenSize());
            r.putClientProperty("size", s);
            r.addActionListener(e -> capNhatTuyChon());
            grpSize.add(r);
            rdSize.add(r);
            pnlSize.add(r);
        }
        pnlTopping.removeAll();
        chkTopping.clear();
        for (Topping t : dsTopping) {
            JCheckBox c = UI.check(t.getTenTopping() + " +" + (t.getGia() % 1000 == 0 ? ((long) t.getGia() / 1000) + "k" : FormatUtil.tienD(t.getGia())));
            c.putClientProperty("topping", t);
            c.addActionListener(e -> capNhatTuyChon());
            chkTopping.add(c);
            pnlTopping.add(c);
        }
        pnlSize.revalidate();
        pnlTopping.revalidate();
        pnlSize.repaint();
        pnlTopping.repaint();
    }

    private void locMon() {
        pnlLuoi.removeAll();
        String k = txtTim.getText().trim().toLowerCase();
        for (Mon m : dsMon) {
            if (locLoai != null && !locLoai.equals(m.getLoaiMon().getMaLoai())) continue;
            if (!k.isEmpty() && !m.getTenMon().toLowerCase().contains(k)) continue;
            pnlLuoi.add(new MonCard(m));
        }
        // giữ lưới luôn đủ 3 cột để thẻ không bị kéo giãn khi ít món
        int du = (3 - pnlLuoi.getComponentCount() % 3) % 3;
        for (int i = 0; i < du; i++) {
            JPanel trong = new JPanel();
            trong.setOpaque(false);
            pnlLuoi.add(trong);
        }
        pnlLuoi.revalidate();
        pnlLuoi.repaint();
    }

    // =========================================================== giỏ hàng
    private Size sizeMacDinh() {
        for (Size s : dsSize) if ("M".equalsIgnoreCase(s.getTenSize())) return s;
        for (Size s : dsSize) if (s.getPhuThu() == 0) return s;
        return dsSize.isEmpty() ? null : dsSize.get(0);
    }

    private double giaDon(Mon m, Size s) {
        return Math.max(0, m.getGiaCoBan() + (s == null ? 0 : s.getPhuThu()));
    }

    private void themMon(Mon m) {
        boolean uong = m.laDoUong();
        Size sz = uong ? sizeMacDinh() : null;
        ChiTietHoaDon ct = new ChiTietHoaDon();
        ct.setMon(m);
        ct.setSize(sz);
        ct.setSoLuong(1);
        ct.setDonGia(giaDon(m, sz));
        ct.setMucDuong(uong ? "70%" : null);
        ct.setMucDa(uong ? "Vừa" : null);
        ct.setGhiChu("");
        for (int i = 0; i < gio.size(); i++) {
            ChiTietHoaDon x = gio.get(i);
            boolean giong = x.getMon().equals(m) && Objects.equals(x.getSize(), sz) && Objects.equals(x.getMucDuong(), ct.getMucDuong())
                    && Objects.equals(x.getMucDa(), ct.getMucDa()) && x.getDsTopping().isEmpty() && x.getGhiChu().isEmpty();
            if (giong) {
                x.setSoLuong(Math.min(99, x.getSoLuong() + 1));
                refreshGio(i);
                return;
            }
        }
        gio.add(ct);
        refreshGio(gio.size() - 1);
    }

    private void refreshGio(int chon) {
        mdlGio.setRowCount(0);
        for (ChiTietHoaDon ct : gio) {
            String ten = ct.getMon().getTenMon() + (ct.getDsTopping().isEmpty() ? "" : " (+" + ct.getDsTopping().size() + ")");
            mdlGio.addRow(new Object[]{ten, ct.getSize() == null ? "—" : ct.getSize().getTenSize(), ct.getSoLuong(), FormatUtil.tien(ct.tinhThanhTien())});
        }
        if (chon >= 0 && chon < gio.size()) tblGio.setRowSelectionInterval(chon, chon);
        capNhatKM();
        tinhTong();
    }

    private double tamTinh() {
        double s = 0;
        for (ChiTietHoaDon ct : gio) s += ct.tinhThanhTien();
        return s;
    }

    private void capNhatKM() {
        double tam = tamTinh();
        Object cur = cboKM.getSelectedItem();
        dangNap = true;
        cboKM.removeAllItems();
        cboKM.addItem("Không áp dụng");
        if (tam > 0) {
            try {
                for (KhuyenMai k : kmDAO.getApDung(tam, LocalDate.now())) cboKM.addItem(k);
            } catch (DaoException ignored) {
                // mất kết nối thì chỉ không hiện khuyến mãi, lỗi sẽ báo khi thanh toán
            }
        }
        int idx = 0;
        if (cur instanceof KhuyenMai) {
            for (int i = 0; i < cboKM.getItemCount(); i++) if (cboKM.getItemAt(i).equals(cur)) idx = i;
        }
        cboKM.setSelectedIndex(idx);
        dangNap = false;
    }

    private KhuyenMai kmDangChon() {
        Object o = cboKM.getSelectedItem();
        return o instanceof KhuyenMai ? (KhuyenMai) o : null;
    }

    private void tinhTong() {
        double tam = tamTinh();
        KhuyenMai km = kmDangChon();
        double giam = km == null ? 0 : km.tinhGiam(tam);
        lblTamTinh.setText(FormatUtil.tienD(tam));
        lblGiam.setText(giam > 0 ? "-" + FormatUtil.tienD(giam) : "0đ");
        lblTong.setText(FormatUtil.tienD(Math.max(0, tam - giam)));
    }

    private void loadOptions() {
        int r = tblGio.getSelectedRow();
        boolean co = r >= 0 && r < gio.size();
        ChiTietHoaDon ct = co ? gio.get(r) : null;
        boolean uong = co && ct.getMon().laDoUong();
        dangNap = true;
        lblMonChon.setText(co ? "Tùy chọn cho: " + ct.getMon().getTenMon() : "Chưa chọn món (bấm vào món bên trái)");
        for (JRadioButton b : rdSize) b.setEnabled(uong);
        for (JCheckBox c : chkTopping) c.setEnabled(uong);
        cboDuong.setEnabled(uong);
        cboDa.setEnabled(uong);
        spSL.setEnabled(co);
        txtGhiChu.setEnabled(co);
        grpSize.clearSelection();
        for (JCheckBox c : chkTopping) c.setSelected(false);
        if (co) {
            spSL.setValue(ct.getSoLuong());
            txtGhiChu.setText(ct.getGhiChu() == null ? "" : ct.getGhiChu());
            if (uong) {
                for (JRadioButton b : rdSize) if (b.getClientProperty("size").equals(ct.getSize())) b.setSelected(true);
                cboDuong.setSelectedItem(ct.getMucDuong());
                cboDa.setSelectedItem(ct.getMucDa());
                for (JCheckBox c : chkTopping) c.setSelected(ct.getDsTopping().contains((Topping) c.getClientProperty("topping")));
            }
        } else {
            spSL.setValue(1);
            txtGhiChu.setText("");
        }
        dangNap = false;
    }

    /** Ghi các tùy chọn đang hiện trên form vào dòng đang chọn của giỏ hàng. */
    private void capNhatTuyChon() {
        if (dangNap) return;
        int r = tblGio.getSelectedRow();
        if (r < 0 || r >= gio.size()) return;
        ChiTietHoaDon ct = gio.get(r);
        if (ct.getMon().laDoUong()) {
            Size sz = ct.getSize();
            for (JRadioButton b : rdSize) if (b.isSelected()) sz = (Size) b.getClientProperty("size");
            ct.setSize(sz);
            ct.setDonGia(giaDon(ct.getMon(), sz));
            ct.setMucDuong((String) cboDuong.getSelectedItem());
            ct.setMucDa((String) cboDa.getSelectedItem());
            List<Topping> ds = new ArrayList<>();
            for (JCheckBox c : chkTopping) if (c.isSelected()) ds.add((Topping) c.getClientProperty("topping"));
            ct.setDsTopping(ds);
        }
        ct.setSoLuong((Integer) spSL.getValue());
        ct.setGhiChu(txtGhiChu.getText().trim());
        refreshGio(r);
    }

    private void xoaMon() {
        int r = tblGio.getSelectedRow();
        if (r < 0) {
            UI.warn("Hãy chọn món trong giỏ hàng cần xóa.");
            return;
        }
        gio.remove(r);
        refreshGio(Math.min(r, gio.size() - 1));
    }

    private void huyDon() {
        if (gio.isEmpty()) return;
        if (!UI.confirm("Hủy đơn đang tạo? Toàn bộ món trong giỏ sẽ bị xóa.")) return;
        gio.clear();
        refreshGio(-1);
    }

    private void thanhToan() {
        if (gio.isEmpty()) {
            UI.warn("Giỏ hàng đang trống, hãy chọn món trước.");
            return;
        }
        HoaDon hd = new HoaDon();
        hd.setNhanVien(SessionManager.getNhanVien());
        hd.setDsChiTiet(new ArrayList<>(gio));
        for (ChiTietHoaDon ct : hd.getDsChiTiet()) ct.setHoaDon(hd);
        KhuyenMai km = kmDangChon();
        hd.setKhuyenMai(km);
        hd.setGiamGia(km == null ? 0 : km.tinhGiam(hd.tinhTamTinh()));
        int stt;
        try {
            stt = hoaDonDAO.soThuTuTiepTheo();
        } catch (DaoException e) {
            UI.error(e.getMessage());
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        DlgThanhToan dlg = new DlgThanhToan(owner, hd, stt);
        dlg.setVisible(true);
        if (!dlg.isDaThanhToan()) return;
        hd.setPhuongThucTT(dlg.getPhuongThuc());
        double khachDua = dlg.getKhachDua();
        double tienThua = dlg.getTienThua();
        if (!UI.run(() -> hoaDonDAO.luuHoaDon(hd))) return;
        gio.clear();
        refreshGio(-1);
        capNhatSoDon();
        if (dlg.isInHoaDon()) new DlgHoaDon(owner, hd, khachDua, tienThua).setVisible(true);
        else UI.info("Đã lưu đơn #" + String.format("%03d", hd.getSoThuTu()) + ".");
    }

    // =========================================================== thẻ món
 // =========================================================== thẻ món
 // =========================================================== thẻ món
    private Image anh(String path) {
        if (path == null || path.trim().isEmpty()) return null;
        String tenFile = path.trim();
        if (!tenFile.contains(".")) {
            tenFile += ".png";
        }
        if (cacheAnh.containsKey(tenFile)) return cacheAnh.get(tenFile);

        Image img = null;

        // Danh sách các đường dẫn có thể xảy ra khi chạy trên Eclipse/Windows
        String[] duongDanThuNghiem = {
            "resources/images/mon/" + tenFile,
            "QuanLyCafe/resources/images/mon/" + tenFile,
            "bin/resources/images/mon/" + tenFile,
            "src/resources/images/mon/" + tenFile
        };

        File fileHopLe = null;
        for (String p : duongDanThuNghiem) {
            File f = new File(p);
            if (f.exists() && f.isFile()) {
                fileHopLe = f;
                break;
            }
        }

        // Đọc ảnh an toàn bằng javax.imageio.ImageIO (đọc đồng bộ, không bị lỗi width = -1)
        if (fileHopLe != null) {
            try {
                img = javax.imageio.ImageIO.read(fileHopLe);
                System.out.println(">>> ĐÃ TẢI THÀNH CÔNG ẢNH: " + fileHopLe.getAbsolutePath());
            } catch (Exception e) {
                System.err.println(">>> LỖI ĐỌC FILE ẢNH: " + e.getMessage());
            }
        } else {
            // Thử thêm ClassLoader dự phòng
            try {
                java.net.URL u = getClass().getResource("/images/mon/" + tenFile);
                if (u == null) u = getClass().getResource("/resources/images/mon/" + tenFile);
                if (u != null) {
                    img = javax.imageio.ImageIO.read(u);
                    System.out.println(">>> TẢI ẢNH QUA CLASSPATH THÀNH CÔNG: " + u);
                }
            } catch (Exception ignored) {}
        }

        if (img == null) {
            System.err.println(">>> KHÔNG TÌM THẤY ẢNH: " + tenFile 
                + " | Đang đứng tại thư mục: " + new File(".").getAbsolutePath());
        }

        cacheAnh.put(tenFile, img);
        return img;
    }

    private class MonCard extends JPanel {
        private final Mon mon;
        private boolean hover;

        MonCard(Mon m) {
            this.mon = m;
            setOpaque(false);
            setPreferredSize(new Dimension(130, 112));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(m.getTenMon() + " - " + FormatUtil.tienD(m.getGiaCoBan()));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    themMon(mon);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(hover ? new Color(0xFFF6E5) : Color.WHITE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g2.setColor(hover ? AppColor.ACCENT : AppColor.LINE);
            g2.setStroke(new BasicStroke(hover ? 1.6f : 1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
            // khung ảnh
            int ax = 6, ay = 6, aw = w - 12, ah = 56;
            Shape clip = new java.awt.geom.RoundRectangle2D.Double(ax, ay, aw, ah, 8, 8);
            g2.setColor(AppColor.LATTE);
            g2.fill(clip);
            Image img = anh(mon.getHinhAnh());
            if (img != null) {
                Shape old = g2.getClip();
                g2.setClip(clip);

                int iwOrig = img.getWidth(null);
                int ihOrig = img.getHeight(null);

                // Giữ nguyên tỉ lệ để ảnh vừa khít toàn bộ trong khung aw x ah
                double sc = Math.min((double) aw / iwOrig, (double) ah / ihOrig);
                int iw = (int) (iwOrig * sc);
                int ih = (int) (ihOrig * sc);

                // Căn giữa theo cả chiều ngang (X) lẫn chiều dọc (Y)
                int ix = ax + (aw - iw) / 2;
                int iy = ay + (ah - ih) / 2;

                g2.drawImage(img, ix, iy, iw, ih, null);
                g2.setClip(old);
            } else {
                new CupIcon(44, new Color(0xB89570), true).paintIcon(this, g2, (w - 44) / 2, ay + 6);
            }
            g2.setFont(AppColor.FONT_B.deriveFont(12f));
            FontMetrics fm = g2.getFontMetrics();
            String ten = mon.getTenMon();
            while (fm.stringWidth(ten) > w - 12 && ten.length() > 3) ten = ten.substring(0, ten.length() - 2) + "…";
            g2.setColor(AppColor.INK);
            g2.drawString(ten, (w - fm.stringWidth(ten)) / 2, ay + ah + 18);
            String gia = FormatUtil.tienD(mon.getGiaCoBan());
            g2.setColor(AppColor.ACCENT);
            g2.drawString(gia, (w - fm.stringWidth(gia)) / 2, ay + ah + 34);
            g2.dispose();
        }
    }
}
