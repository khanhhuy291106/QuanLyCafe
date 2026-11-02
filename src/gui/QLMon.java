package gui;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import dao.LoaiMon_DAO;
import dao.Mon_DAO;
import entity.LoaiMon;
import entity.Mon;

// Quản lý món (nằm trong TabThucDon)
public class QLMon extends JPanel implements TaiLai {
    private Mon_DAO dao = new Mon_DAO();
    private LoaiMon_DAO loai_dao = new LoaiMon_DAO();
    private ArrayList<Mon> ds = new ArrayList<>();
    private JTextField txtMa = TienIch.oMa();
    private JTextField txtTen = new JTextField(10);
    private JTextField txtGia = new JTextField(10);
    private JComboBox<LoaiMon> cboLoai = new JComboBox<>();
    private JComboBox<Object> cboLoc = new JComboBox<>();
    private JComboBox<String> cboTT = new JComboBox<>(new String[] { "Đang bán", "Ngừng bán" });
    private JLabel lblAnh = new JLabel("", SwingConstants.CENTER);
    private String tenFileAnh = null;
    private boolean dangNap = false;
    private JTextField txtTim = new JTextField(14);
    private DefaultTableModel mdl = TienIch.moHinh("Mã món", "Tên món", "Loại", "Giá cơ bản (M)", "Trạng thái");
    private JTable tbl = TienIch.bang(mdl);
    private JButton btnThem = TienIch.nut("Thêm", TienIch.GREEN);
    private JButton btnSua = TienIch.nut("Sửa", TienIch.BLUE);
    private JButton btnXoa = TienIch.nut("Xóa", TienIch.RED);
    private JButton btnLamMoi = TienIch.nut("Làm mới", new Color(0xB8A890));
    private JButton btnTim = TienIch.nut("Tìm kiếm", TienIch.BROWN);
    private JButton btnChonAnh = TienIch.nutPhu("Chọn ảnh...");

    public QLMon() {
        JPanel form = new JPanel(new GridBagLayout());
        TienIch.themDong(form, 0, "Mã món", txtMa);
        TienIch.themDong(form, 1, "Tên món", txtTen);
        TienIch.themDong(form, 2, "Loại món", cboLoai);
        TienIch.themDong(form, 3, "Giá cơ bản", txtGia);
        TienIch.themDong(form, 4, "Trạng thái", cboTT);

        lblAnh.setPreferredSize(new Dimension(130, 96));
        lblAnh.setOpaque(true);
        lblAnh.setBackground(TienIch.LATTE);
        lblAnh.setBorder(BorderFactory.createLineBorder(TienIch.LINE));
        JPanel pnlAnh = new JPanel(new BorderLayout(0, 6));
        pnlAnh.setOpaque(false);
        JPanel khungAnh = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        khungAnh.setOpaque(false);
        khungAnh.add(lblAnh);
        pnlAnh.add(khungAnh, BorderLayout.CENTER);
        JPanel khungNut = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        khungNut.setOpaque(false);
        btnChonAnh.setPreferredSize(new Dimension(130, 28));
        khungNut.add(btnChonAnh);
        pnlAnh.add(khungNut, BorderLayout.SOUTH);
        TienIch.themDong(form, 5, "Hình ảnh", pnlAnh);
        TienIch.themKhoangTrong(form, 6);

        JPanel trai = TienIch.theForm("Thông tin món", 340, form, TienIch.pnlNutCrud(btnThem, btnSua, btnXoa, btnLamMoi));
        cboLoc.setPreferredSize(new Dimension(140, 26));
        JPanel thanhTim = TienIch.thanhTim(txtTim, btnTim);
        thanhTim.add(cboLoc);
        TienIch.canPhai(tbl, 3);
        TienIch.tomauTrangThai(tbl, 4);
        TienIch.doRong(tbl, 70, 220, 110, 110, 110);
        JPanel phai = TienIch.phaiBang(thanhTim, tbl, TienIch.ghiChu(
                "Món “Ngừng bán” không hiện ở màn hình Bán hàng nhưng vẫn giữ trong hóa đơn cũ. Nên chuyển trạng thái thay vì xóa món đã từng bán."));
        TienIch.xepCrud(this, trai, phai);

        tbl.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tbl.getSelectedRow() >= 0) dienForm(tbl.getSelectedRow());
        });
        cboLoc.addActionListener(e -> {
            if (!dangNap) taiLai();
        });
        btnTim.addActionListener(e -> taiLai());
        txtTim.addActionListener(e -> taiLai());
        btnChonAnh.addActionListener(e -> chonAnh());
        btnThem.addActionListener(e -> them());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());
        btnLamMoi.addActionListener(e -> lamMoi());
        xoaForm();
    }

    @Override
    public void taiLai() {
        // Nạp lại danh sách loại cho 2 combo, cố giữ lại lựa chọn cũ
        dangNap = true;
        String maLoaiCu = cboLoai.getSelectedItem() == null ? null : ((LoaiMon) cboLoai.getSelectedItem()).getMaLoai();
        String maLoc = (cboLoc.getSelectedItem() instanceof LoaiMon) ? ((LoaiMon) cboLoc.getSelectedItem()).getMaLoai() : null;
        cboLoai.removeAllItems();
        cboLoc.removeAllItems();
        cboLoc.addItem("Tất cả loại");
        for (LoaiMon l : loai_dao.getAll()) {
            cboLoai.addItem(l);
            cboLoc.addItem(l);
            if (l.getMaLoai().equals(maLoaiCu)) cboLoai.setSelectedItem(l);
            if (l.getMaLoai().equals(maLoc)) cboLoc.setSelectedItem(l);
        }
        dangNap = false;

        String maLoai = (cboLoc.getSelectedItem() instanceof LoaiMon) ? ((LoaiMon) cboLoc.getSelectedItem()).getMaLoai() : null;
        ds = dao.timKiem(txtTim.getText().trim(), maLoai);
        mdl.setRowCount(0);
        for (Mon m : ds) {
            mdl.addRow(new Object[] { m.getMaMon(), m.getTenMon(), m.getLoaiMon().getTenLoai(), TienIch.tien(m.getGiaCoBan()),
                    m.isTrangThai() ? "Đang bán" : "Ngừng bán" });
        }
    }

    private void dienForm(int row) {
        Mon m = ds.get(row);
        txtMa.setText(m.getMaMon());
        txtTen.setText(m.getTenMon());
        txtGia.setText(TienIch.tien(m.getGiaCoBan()));
        for (int i = 0; i < cboLoai.getItemCount(); i++) {
            if (cboLoai.getItemAt(i).getMaLoai().equals(m.getLoaiMon().getMaLoai())) cboLoai.setSelectedIndex(i);
        }
        cboTT.setSelectedIndex(m.isTrangThai() ? 0 : 1);
        tenFileAnh = m.getHinhAnh();
        hienAnh();
    }

    private void xoaForm() {
        txtMa.setText(TienIch.CHUA_CO_MA);
        txtTen.setText("");
        txtGia.setText("");
        cboTT.setSelectedIndex(0);
        if (cboLoai.getItemCount() > 0) cboLoai.setSelectedIndex(0);
        tenFileAnh = null;
        hienAnh();
    }

    private void lamMoi() {
        tbl.clearSelection();
        txtTim.setText("");
        xoaForm();
        taiLai();
    }

    private void hienAnh() {
        ImageIcon icon = TienIch.docAnhMon(tenFileAnh, 126, 92);
        lblAnh.setIcon(icon != null ? icon : TienIch.bieuTuongLy(56, new Color(0xB89570)));
    }

    // Chọn ảnh từ máy, sao chép vào thư mục image/mon và chỉ lưu tên file
    private void chonAnh() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Ảnh (png, jpg, jpeg, gif)", "png", "jpg", "jpeg", "gif"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File nguon = fc.getSelectedFile();
        try {
            Path thuMuc = Paths.get("image", "mon");
            Files.createDirectories(thuMuc);
            String ten = nguon.getName();
            String duoi = ten.contains(".") ? ten.substring(ten.lastIndexOf('.')).toLowerCase() : ".png";
            String tenMoi = "mon_" + System.currentTimeMillis() + duoi;
            Files.copy(nguon.toPath(), thuMuc.resolve(tenMoi), StandardCopyOption.REPLACE_EXISTING);
            tenFileAnh = tenMoi;
            hienAnh();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không sao chép được ảnh: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Đọc và kiểm tra form. Trả null nếu có lỗi (đã báo cho người dùng)
    private Mon docForm(String ma) {
        String ten = txtTen.getText().trim().replaceAll("\\s+", " ");
        if (ten.isEmpty() || ten.length() > 100) {
            JOptionPane.showMessageDialog(this, "Tên món không được để trống và tối đa 100 ký tự.");
            return null;
        }
        double gia = TienIch.parseTien(txtGia.getText());
        if (gia < 0) {
            JOptionPane.showMessageDialog(this, "Giá cơ bản phải là số nguyên không âm, ví dụ 29000 hoặc 29.000.");
            return null;
        }
        LoaiMon loai = (LoaiMon) cboLoai.getSelectedItem();
        if (loai == null) {
            JOptionPane.showMessageDialog(this, "Chưa có loại món. Hãy tạo loại món ở thẻ “Loại món” trước.");
            return null;
        }
        return new Mon(ma, ten, gia, tenFileAnh, loai, cboTT.getSelectedIndex() == 0);
    }

    private void them() {
        Mon m = docForm(null);
        if (m == null) return;
        if (dao.them(m)) {
            lamMoi();
            JOptionPane.showMessageDialog(this, "Đã thêm món " + m.getMaMon() + " - " + m.getTenMon() + ".");
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
        Mon m = docForm(ds.get(r).getMaMon());
        if (m == null) return;
        if (dao.sua(m)) {
            lamMoi();
            JOptionPane.showMessageDialog(this, "Đã cập nhật món " + m.getMaMon() + ".");
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
        if (dao.xoa(ds.get(r).getMaMon())) {
            lamMoi();
        } else {
            JOptionPane.showMessageDialog(this, "Món này đã từng được bán nên không thể xóa. Hãy đổi trạng thái sang “Ngừng bán”.");
        }
    }
}
