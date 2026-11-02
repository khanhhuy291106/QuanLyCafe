package gui;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import dao.*;
import entity.*;

// Bán hàng: chọn món bên trái, giỏ hàng + tùy chọn + thanh toán bên phải (Thu ngân và Quản lý)
public class TabBanHang extends JPanel implements TaiLai {
    private Mon_DAO mon_dao = new Mon_DAO();
    private LoaiMon_DAO loai_dao = new LoaiMon_DAO();
    private Size_DAO size_dao = new Size_DAO();
    private Topping_DAO topping_dao = new Topping_DAO();
    private KhuyenMai_DAO km_dao = new KhuyenMai_DAO();
    private HoaDon_DAO hd_dao = new HoaDon_DAO();
    private NhanVien nv;

    private ArrayList<Mon> dsMon = new ArrayList<>();
    private ArrayList<LoaiMon> dsLoai = new ArrayList<>();
    private ArrayList<Size> dsSize = new ArrayList<>();
    private ArrayList<Topping> dsTopping = new ArrayList<>();
    private ArrayList<KhuyenMai> dsKM = new ArrayList<>();
    private ArrayList<ChiTietHoaDon> gio = new ArrayList<>();
    private String locLoai = null;
    private boolean dangNap = false;

    // bên trái
    private JTextField txtTim;
    private JPanel pnlChips, pnlLuoi;

    // bên phải
    private JLabel lblDon, lblMonChon, lblTamTinh, lblGiam, lblTong;
    private DefaultTableModel mdlGio;
    private JTable tblGio;
    private JPanel pnlSize, pnlTopping;
    private ButtonGroup nhomSize = new ButtonGroup();
    private ArrayList<JRadioButton> dsRdSize = new ArrayList<>();
    private ArrayList<JCheckBox> dsChkTopping = new ArrayList<>();
    private JComboBox<String> cboDuong, cboDa, cboKM;
    private JSpinner spSL;
    private JTextField txtGhiChu;

    public TabBanHang(NhanVien nv) {
        this.nv = nv;
        setLayout(new BorderLayout(12, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(TienIch.BG);
        add(taoBenTrai(), BorderLayout.CENTER);
        add(taoBenPhai(), BorderLayout.EAST);
        taiLai();
    }

    // =========================================================== dựng giao diện
    private JPanel taoBenTrai() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        JPanel pnlTren = new JPanel(new BorderLayout(0, 8));
        pnlTren.setOpaque(false);
        JPanel pnlTim = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlTim.setOpaque(false);
        txtTim = new JTextField(16);
        pnlTim.add(txtTim);
        pnlTim.add(TienIch.ghiChu("Gõ tên món để lọc, bấm vào món để thêm vào đơn"));
        pnlTren.add(pnlTim, BorderLayout.NORTH);
        pnlChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        pnlChips.setOpaque(false);
        pnlTren.add(pnlChips, BorderLayout.CENTER);
        p.add(pnlTren, BorderLayout.NORTH);

        pnlLuoi = new JPanel(new GridLayout(0, 3, 10, 10));
        pnlLuoi.setOpaque(false);
        JPanel pnlBoc = new JPanel(new BorderLayout());
        pnlBoc.setOpaque(false);
        pnlBoc.add(pnlLuoi, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(pnlBoc);
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

    private JPanel taoBenPhai() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(392, 100));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(0, 0, 8, 0);

        // tiêu đề đơn
        JPanel pnlDau = new JPanel(new BorderLayout());
        pnlDau.setBackground(TienIch.BROWN);
        pnlDau.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        lblDon = new JLabel("Đơn #001  —  Mang đi");
        lblDon.setFont(TienIch.FONT_B);
        lblDon.setForeground(Color.WHITE);
        pnlDau.add(lblDon, BorderLayout.WEST);
        JLabel lblNV = new JLabel("NV: " + nv.getHoTen());
        lblNV.setFont(TienIch.FONT_SMALL);
        lblNV.setForeground(TienIch.LATTE);
        pnlDau.add(lblNV, BorderLayout.EAST);
        g.gridy = 0;
        g.weighty = 0;
        p.add(pnlDau, g);

        // giỏ hàng
        mdlGio = TienIch.moHinh("Món", "Size", "SL", "Thành tiền");
        tblGio = TienIch.bang(mdlGio);
        TienIch.canGiua(tblGio, 1, 2);
        TienIch.canPhai(tblGio, 3);
        TienIch.doRong(tblGio, 150, 40, 36, 80);
        tblGio.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) napTuyChon();
        });
        JScrollPane spGio = TienIch.cuon(tblGio);
        spGio.setPreferredSize(new Dimension(100, 150));
        g.gridy = 1;
        g.weighty = 1;
        p.add(spGio, g);
        g.weighty = 0;

        // tùy chọn
        g.gridy = 2;
        p.add(taoTuyChon(), g);

        // khuyến mãi
        JPanel pnlKM = new JPanel(new BorderLayout(8, 0));
        pnlKM.setOpaque(false);
        pnlKM.add(new JLabel("Khuyến mãi"), BorderLayout.WEST);
        cboKM = new JComboBox<>();
        cboKM.setPreferredSize(new Dimension(100, 30));
        pnlKM.add(cboKM, BorderLayout.CENTER);
        g.gridy = 3;
        p.add(pnlKM, g);
        cboKM.addActionListener(e -> {
            if (!dangNap) tinhTong();
        });

        // tổng tiền
        lblTamTinh = new JLabel("0đ");
        lblGiam = new JLabel("0đ");
        lblGiam.setForeground(TienIch.RED);
        lblTong = new JLabel("0đ");
        lblTong.setFont(new Font(TienIch.FONT_NAME, Font.BOLD, 22));
        lblTong.setForeground(TienIch.GREEN);
        JPanel pnlTong = new JPanel(new GridLayout(0, 1, 0, 3));
        pnlTong.setOpaque(false);
        pnlTong.add(taoDongTong("Tạm tính", lblTamTinh));
        pnlTong.add(taoDongTong("Giảm giá", lblGiam));
        JPanel pnlCong = new JPanel(new BorderLayout());
        pnlCong.setOpaque(false);
        pnlCong.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, TienIch.LINE));
        JLabel lblCong = new JLabel("TỔNG CỘNG");
        lblCong.setFont(TienIch.FONT_H2);
        pnlCong.add(lblCong, BorderLayout.WEST);
        pnlCong.add(lblTong, BorderLayout.EAST);
        pnlTong.add(pnlCong);
        g.gridy = 4;
        p.add(pnlTong, g);

        // nút
        JPanel pnlNut = new JPanel(new GridLayout(1, 3, 6, 0));
        pnlNut.setOpaque(false);
        JButton btnXoa = TienIch.nutPhu("Xóa món");
        JButton btnLamMoi = TienIch.nutPhu("Làm mới");
        JButton btnHuy = TienIch.nut("Hủy đơn", TienIch.RED);
        pnlNut.add(btnXoa);
        pnlNut.add(btnLamMoi);
        pnlNut.add(btnHuy);
        g.gridy = 5;
        p.add(pnlNut, g);
        JButton btnTT = TienIch.nut("THANH TOÁN", TienIch.GREEN);
        btnTT.setFont(new Font(TienIch.FONT_NAME, Font.BOLD, 16));
        btnTT.setPreferredSize(new Dimension(100, 46));
        g.gridy = 6;
        g.insets = new Insets(0, 0, 0, 0);
        p.add(btnTT, g);

        btnXoa.addActionListener(e -> xoaMon());
        btnLamMoi.addActionListener(e -> taiLai());
        btnHuy.addActionListener(e -> huyDon());
        btnTT.addActionListener(e -> thanhToan());
        return p;
    }

    private JPanel taoDongTong(String ten, JLabel gt) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(new JLabel(ten), BorderLayout.WEST);
        p.add(gt, BorderLayout.EAST);
        return p;
    }

    private JPanel taoTuyChon() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(TienIch.LINE),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 0, 3, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        lblMonChon = new JLabel("Chưa chọn món");
        lblMonChon.setFont(TienIch.FONT_B);
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 4;
        card.add(lblMonChon, g);
        g.gridwidth = 1;

        g.gridy = 1;
        g.gridx = 0;
        card.add(new JLabel("Size"), g);
        pnlSize = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlSize.setOpaque(false);
        g.gridx = 1;
        g.gridwidth = 3;
        card.add(pnlSize, g);
        g.gridwidth = 1;

        cboDuong = new JComboBox<>(new String[] { "100%", "70%", "50%", "30%", "0%" });
        cboDa = new JComboBox<>(new String[] { "Nhiều", "Vừa", "Ít", "Không" });
        g.gridy = 2;
        g.gridx = 0;
        card.add(new JLabel("Đường"), g);
        g.gridx = 1;
        g.weightx = 1;
        card.add(cboDuong, g);
        g.weightx = 0;
        g.gridx = 2;
        card.add(new JLabel("Đá"), g);
        g.gridx = 3;
        g.weightx = 1;
        card.add(cboDa, g);
        g.weightx = 0;

        spSL = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        spSL.setPreferredSize(new Dimension(70, 30));
        g.gridy = 3;
        g.gridx = 0;
        card.add(new JLabel("Số lượng"), g);
        g.gridx = 1;
        g.fill = GridBagConstraints.NONE;
        card.add(spSL, g);
        g.fill = GridBagConstraints.HORIZONTAL;

        pnlTopping = new JPanel(new GridLayout(0, 2, 4, 2));
        pnlTopping.setOpaque(false);
        g.gridy = 4;
        g.gridx = 0;
        g.anchor = GridBagConstraints.NORTHWEST;
        card.add(new JLabel("Topping"), g);
        g.gridx = 1;
        g.gridwidth = 3;
        card.add(pnlTopping, g);
        g.gridwidth = 1;
        g.anchor = GridBagConstraints.WEST;

        txtGhiChu = new JTextField(10);
        g.gridy = 5;
        g.gridx = 0;
        card.add(new JLabel("Ghi chú"), g);
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
    public void taiLai() {
        dsMon = mon_dao.getDangBan();
        dsLoai = loai_dao.getAll();
        dsSize = size_dao.getAll();
        dsTopping = topping_dao.getDangBan();
        dungChips();
        dungTuyChon();
        locMon();
        napTuyChon();
        lblDon.setText("Đơn #" + String.format("%03d", hd_dao.soThuTuTiepTheo()) + "  —  Mang đi");
    }

    private void dungChips() {
        pnlChips.removeAll();
        pnlChips.add(taoChip("Tất cả", null));
        for (LoaiMon l : dsLoai) {
            pnlChips.add(taoChip(l.getTenLoai(), l.getMaLoai()));
        }
        pnlChips.revalidate();
        pnlChips.repaint();
    }

    private JButton taoChip(String ten, String maLoai) {
        boolean chon = Objects.equals(maLoai, locLoai);
        JButton b = TienIch.nut(ten, chon ? TienIch.ACCENT : new Color(0xEFE6D8));
        b.setForeground(chon ? Color.WHITE : TienIch.BROWN);
        b.addActionListener(e -> {
            locLoai = maLoai;
            dungChips();
            locMon();
        });
        return b;
    }

    private void dungTuyChon() {
        pnlSize.removeAll();
        dsRdSize.clear();
        nhomSize = new ButtonGroup();
        for (Size s : dsSize) {
            JRadioButton r = new JRadioButton(s.getTenSize());
            r.addActionListener(e -> capNhatTuyChon());
            nhomSize.add(r);
            dsRdSize.add(r);
            pnlSize.add(r);
        }
        pnlTopping.removeAll();
        dsChkTopping.clear();
        for (Topping t : dsTopping) {
            String gia = (t.getGia() % 1000 == 0) ? ((long) t.getGia() / 1000) + "k" : TienIch.tienD(t.getGia());
            JCheckBox c = new JCheckBox(t.getTenTopping() + " +" + gia);
            c.addActionListener(e -> capNhatTuyChon());
            dsChkTopping.add(c);
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
            pnlLuoi.add(taoTheMon(m));
        }
        // Giữ lưới luôn đủ 3 cột để thẻ không bị kéo giãn khi ít món
        int du = (3 - pnlLuoi.getComponentCount() % 3) % 3;
        for (int i = 0; i < du; i++) {
            JPanel trong = new JPanel();
            trong.setOpaque(false);
            pnlLuoi.add(trong);
        }
        pnlLuoi.revalidate();
        pnlLuoi.repaint();
    }

    // Thẻ món: ảnh + tên + giá, bấm vào để thêm vào đơn
    private JPanel taoTheMon(Mon m) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(TienIch.LINE),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        p.setPreferredSize(new Dimension(130, 112));
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));
        p.setToolTipText(m.getTenMon() + " - " + TienIch.tienD(m.getGiaCoBan()));

        JLabel lblAnh = new JLabel("", SwingConstants.CENTER);
        lblAnh.setOpaque(true);
        lblAnh.setBackground(TienIch.LATTE);
        lblAnh.setPreferredSize(new Dimension(100, 56));
        ImageIcon anh = TienIch.docAnhMon(m.getHinhAnh(), 118, 56);
        lblAnh.setIcon(anh != null ? anh : TienIch.bieuTuongLy(44, new Color(0xB89570)));
        p.add(lblAnh, BorderLayout.NORTH);

        JLabel lblTen = new JLabel(m.getTenMon(), SwingConstants.CENTER);
        lblTen.setFont(TienIch.FONT_B);
        JLabel lblGia = new JLabel(TienIch.tienD(m.getGiaCoBan()), SwingConstants.CENTER);
        lblGia.setForeground(TienIch.ACCENT);
        lblGia.setFont(TienIch.FONT_B);
        JPanel pnlChu = new JPanel(new GridLayout(2, 1));
        pnlChu.setOpaque(false);
        pnlChu.add(lblTen);
        pnlChu.add(lblGia);
        p.add(pnlChu, BorderLayout.CENTER);

        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                themMon(m);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                p.setBackground(new Color(0xFFF6E5));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                p.setBackground(Color.WHITE);
            }
        });
        return p;
    }

    // =========================================================== giỏ hàng
    private Size sizeMacDinh() {
        for (Size s : dsSize) {
            if ("M".equalsIgnoreCase(s.getTenSize())) return s;
        }
        for (Size s : dsSize) {
            if (s.getPhuThu() == 0) return s;
        }
        return dsSize.isEmpty() ? null : dsSize.get(0);
    }

    private double giaDon(Mon m, Size s) {
        return Math.max(0, m.getGiaCoBan() + (s == null ? 0 : s.getPhuThu()));
    }

    private boolean cungSize(Size a, Size b) {
        if (a == null || b == null) return a == b;
        return a.getMaSize().equals(b.getMaSize());
    }

    private void themMon(Mon m) {
        boolean uong = m.laDoUong();
        Size sz = uong ? sizeMacDinh() : null;
        String duong = uong ? "70%" : null;
        String da = uong ? "Vừa" : null;
        // Món giống hệt (cùng size, đường, đá, chưa topping/ghi chú) thì tăng số lượng
        for (int i = 0; i < gio.size(); i++) {
            ChiTietHoaDon x = gio.get(i);
            if (x.getMon().getMaMon().equals(m.getMaMon()) && cungSize(x.getSize(), sz) && Objects.equals(x.getMucDuong(), duong)
                    && Objects.equals(x.getMucDa(), da) && x.getDsTopping().isEmpty() && x.getGhiChu().isEmpty()) {
                x.setSoLuong(Math.min(99, x.getSoLuong() + 1));
                hienGio(i);
                return;
            }
        }
        ChiTietHoaDon ct = new ChiTietHoaDon();
        ct.setMon(m);
        ct.setSize(sz);
        ct.setSoLuong(1);
        ct.setDonGia(giaDon(m, sz));
        ct.setMucDuong(duong);
        ct.setMucDa(da);
        ct.setGhiChu("");
        gio.add(ct);
        hienGio(gio.size() - 1);
    }

    // Vẽ lại bảng giỏ hàng, chọn dòng "chon" (-1 là không chọn)
    private void hienGio(int chon) {
        mdlGio.setRowCount(0);
        for (ChiTietHoaDon ct : gio) {
            String ten = ct.getMon().getTenMon();
            if (!ct.getDsTopping().isEmpty()) ten += " (+" + ct.getDsTopping().size() + ")";
            mdlGio.addRow(new Object[] { ten, ct.getSize() == null ? "—" : ct.getSize().getTenSize(), ct.getSoLuong(),
                    TienIch.tien(ct.tinhThanhTien()) });
        }
        if (chon >= 0 && chon < gio.size()) tblGio.setRowSelectionInterval(chon, chon);
        napKhuyenMai();
        tinhTong();
    }

    private double tamTinh() {
        double s = 0;
        for (ChiTietHoaDon ct : gio) {
            s += ct.tinhThanhTien();
        }
        return s;
    }

    // Nạp lại danh sách khuyến mãi dùng được cho tạm tính hiện tại, cố giữ lại lựa chọn cũ
    private void napKhuyenMai() {
        KhuyenMai cu = kmDangChon();
        dangNap = true;
        cboKM.removeAllItems();
        cboKM.addItem("Không áp dụng");
        dsKM = new ArrayList<>();
        double tam = tamTinh();
        if (tam > 0) dsKM = km_dao.getApDung(tam, LocalDate.now());
        int chon = 0;
        for (int i = 0; i < dsKM.size(); i++) {
            cboKM.addItem(dsKM.get(i).toString());
            if (cu != null && cu.getMaKM().equals(dsKM.get(i).getMaKM())) chon = i + 1;
        }
        cboKM.setSelectedIndex(chon);
        dangNap = false;
    }

    private KhuyenMai kmDangChon() {
        int i = cboKM.getSelectedIndex();
        if (i > 0 && i - 1 < dsKM.size()) return dsKM.get(i - 1);
        return null;
    }

    private void tinhTong() {
        double tam = tamTinh();
        KhuyenMai km = kmDangChon();
        double giam = (km == null) ? 0 : km.tinhGiam(tam);
        lblTamTinh.setText(TienIch.tienD(tam));
        lblGiam.setText(giam > 0 ? "-" + TienIch.tienD(giam) : "0đ");
        lblTong.setText(TienIch.tienD(Math.max(0, tam - giam)));
    }

    private boolean coTopping(ChiTietHoaDon ct, Topping t) {
        for (Topping x : ct.getDsTopping()) {
            if (x.getMaTopping().equals(t.getMaTopping())) return true;
        }
        return false;
    }

    // Đưa tùy chọn của dòng đang chọn trong giỏ lên form tùy chọn
    private void napTuyChon() {
        int r = tblGio.getSelectedRow();
        boolean co = r >= 0 && r < gio.size();
        ChiTietHoaDon ct = co ? gio.get(r) : null;
        boolean uong = co && ct.getMon().laDoUong();
        dangNap = true;
        lblMonChon.setText(co ? "Tùy chọn cho: " + ct.getMon().getTenMon() : "Chưa chọn món (bấm vào món bên trái)");
        for (JRadioButton b : dsRdSize) b.setEnabled(uong);
        for (JCheckBox c : dsChkTopping) c.setEnabled(uong);
        cboDuong.setEnabled(uong);
        cboDa.setEnabled(uong);
        spSL.setEnabled(co);
        txtGhiChu.setEnabled(co);
        nhomSize.clearSelection();
        for (JCheckBox c : dsChkTopping) c.setSelected(false);
        if (co) {
            spSL.setValue(ct.getSoLuong());
            txtGhiChu.setText(ct.getGhiChu() == null ? "" : ct.getGhiChu());
            if (uong) {
                for (int i = 0; i < dsRdSize.size(); i++) {
                    if (cungSize(dsSize.get(i), ct.getSize())) dsRdSize.get(i).setSelected(true);
                }
                cboDuong.setSelectedItem(ct.getMucDuong());
                cboDa.setSelectedItem(ct.getMucDa());
                for (int i = 0; i < dsChkTopping.size(); i++) {
                    dsChkTopping.get(i).setSelected(coTopping(ct, dsTopping.get(i)));
                }
            }
        } else {
            spSL.setValue(1);
            txtGhiChu.setText("");
        }
        dangNap = false;
    }

    // Ghi các tùy chọn đang hiện trên form vào dòng đang chọn của giỏ hàng
    private void capNhatTuyChon() {
        if (dangNap) return;
        int r = tblGio.getSelectedRow();
        if (r < 0 || r >= gio.size()) return;
        ChiTietHoaDon ct = gio.get(r);
        if (ct.getMon().laDoUong()) {
            Size sz = ct.getSize();
            for (int i = 0; i < dsRdSize.size(); i++) {
                if (dsRdSize.get(i).isSelected()) sz = dsSize.get(i);
            }
            ct.setSize(sz);
            ct.setDonGia(giaDon(ct.getMon(), sz));
            ct.setMucDuong((String) cboDuong.getSelectedItem());
            ct.setMucDa((String) cboDa.getSelectedItem());
            ArrayList<Topping> ds = new ArrayList<>();
            for (int i = 0; i < dsChkTopping.size(); i++) {
                if (dsChkTopping.get(i).isSelected()) ds.add(dsTopping.get(i));
            }
            ct.setDsTopping(ds);
        }
        ct.setSoLuong((Integer) spSL.getValue());
        ct.setGhiChu(txtGhiChu.getText().trim());
        hienGio(r);
    }

    private void xoaMon() {
        int r = tblGio.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn món trong giỏ hàng cần xóa.");
            return;
        }
        gio.remove(r);
        hienGio(Math.min(r, gio.size() - 1));
    }

    private void huyDon() {
        if (gio.isEmpty()) return;
        if (JOptionPane.showConfirmDialog(this, "Hủy đơn đang tạo? Toàn bộ món trong giỏ sẽ bị xóa.", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        gio.clear();
        hienGio(-1);
    }

    private void thanhToan() {
        if (gio.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống, hãy chọn món trước.");
            return;
        }
        HoaDon hd = new HoaDon();
        hd.setNhanVien(nv);
        hd.setDsChiTiet(new ArrayList<>(gio));
        KhuyenMai km = kmDangChon();
        hd.setKhuyenMai(km);
        hd.setGiamGia(km == null ? 0 : km.tinhGiam(hd.tinhTamTinh()));

        Window owner = SwingUtilities.getWindowAncestor(this);
        PhieuThanhToan dlg = new PhieuThanhToan(owner, hd, hd_dao.soThuTuTiepTheo());
        dlg.setVisible(true);
        if (!dlg.isDaThanhToan()) return;

        hd.setPhuongThucTT(dlg.getPhuongThuc());
        double khachDua = dlg.getKhachDua();
        double tienThua = dlg.getTienThua();
        if (!hd_dao.luuHoaDon(hd)) {
            JOptionPane.showMessageDialog(this, "Lưu hóa đơn thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        gio.clear();
        hienGio(-1);
        lblDon.setText("Đơn #" + String.format("%03d", hd_dao.soThuTuTiepTheo()) + "  —  Mang đi");
        if (dlg.isInHoaDon()) new XemHoaDon(owner, hd, khachDua, tienThua).setVisible(true);
        else JOptionPane.showMessageDialog(this, "Đã lưu đơn #" + String.format("%03d", hd.getSoThuTu()) + ".");
    }
}
