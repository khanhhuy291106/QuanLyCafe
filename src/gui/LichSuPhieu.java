package gui;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import dao.PhieuNhap_DAO;
import dao.PhieuXuat_DAO;
import entity.*;

// Xem lại các phiếu nhập hoặc phiếu xuất đã lập, kèm chi tiết từng phiếu
public class LichSuPhieu extends JDialog {
    private boolean nhap;
    private PhieuNhap_DAO nhap_dao = new PhieuNhap_DAO();
    private PhieuXuat_DAO xuat_dao = new PhieuXuat_DAO();
    private ArrayList<PhieuNhap> dsNhap = new ArrayList<>();
    private ArrayList<PhieuXuat> dsXuat = new ArrayList<>();
    private DefaultTableModel mdlPhieu, mdlCT;
    private JTable tblPhieu, tblCT;

    public LichSuPhieu(Window owner, boolean nhap) {
        super(owner, nhap ? "Lịch sử phiếu nhập" : "Lịch sử phiếu xuất", ModalityType.APPLICATION_MODAL);
        this.nhap = nhap;
        setSize(820, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        if (nhap) {
            mdlPhieu = TienIch.moHinh("Mã PN", "Ngày nhập", "Nhân viên", "Nhà cung cấp", "Tổng tiền");
            mdlCT = TienIch.moHinh("Nguyên liệu", "ĐVT", "Số lượng", "Đơn giá", "Thành tiền");
        } else {
            mdlPhieu = TienIch.moHinh("Mã PX", "Ngày xuất", "Nhân viên", "Lý do");
            mdlCT = TienIch.moHinh("Nguyên liệu", "ĐVT", "Số lượng xuất");
        }
        tblPhieu = TienIch.bang(mdlPhieu);
        tblCT = TienIch.bang(mdlCT);
        if (nhap) {
            TienIch.canPhai(tblPhieu, 4);
            TienIch.canPhai(tblCT, 2, 3, 4);
        } else {
            TienIch.canPhai(tblCT, 2);
        }

        JPanel pnlTren = new JPanel(new BorderLayout(0, 6));
        pnlTren.setOpaque(false);
        pnlTren.add(TienIch.tieuDe(nhap ? "Các phiếu nhập" : "Các phiếu xuất"), BorderLayout.NORTH);
        pnlTren.add(TienIch.cuon(tblPhieu), BorderLayout.CENTER);
        JPanel pnlDuoi = new JPanel(new BorderLayout(0, 6));
        pnlDuoi.setOpaque(false);
        pnlDuoi.add(TienIch.tieuDe("Chi tiết phiếu đang chọn"), BorderLayout.NORTH);
        pnlDuoi.add(TienIch.cuon(tblCT), BorderLayout.CENTER);
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pnlTren, pnlDuoi);
        sp.setResizeWeight(0.5);
        sp.setBorder(null);
        add(sp, BorderLayout.CENTER);

        if (nhap) {
            dsNhap = nhap_dao.getAll();
            for (PhieuNhap p : dsNhap) {
                mdlPhieu.addRow(new Object[] { p.getMaPN(), TienIch.ngay(p.getNgayNhap()), p.getNhanVien().getHoTen(),
                        p.getNhaCungCap().getTenNCC(), TienIch.tienD(p.getTongTien()) });
            }
        } else {
            dsXuat = xuat_dao.getAll();
            for (PhieuXuat p : dsXuat) {
                mdlPhieu.addRow(new Object[] { p.getMaPX(), TienIch.ngay(p.getNgayXuat()), p.getNhanVien().getHoTen(), p.getLyDo() });
            }
        }
        tblPhieu.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblPhieu.getSelectedRow() >= 0) hienChiTiet(tblPhieu.getSelectedRow());
        });
        if (mdlPhieu.getRowCount() > 0) tblPhieu.setRowSelectionInterval(0, 0);
    }

    private void hienChiTiet(int row) {
        mdlCT.setRowCount(0);
        if (nhap) {
            for (ChiTietPhieuNhap ct : nhap_dao.layChiTiet(dsNhap.get(row).getMaPN())) {
                mdlCT.addRow(new Object[] { ct.getNguyenLieu().getTenNL(), ct.getNguyenLieu().getDonViTinh(), TienIch.soThuc(ct.getSoLuong()),
                        TienIch.tien(ct.getDonGia()), TienIch.tien(ct.thanhTien()) });
            }
        } else {
            for (ChiTietPhieuXuat ct : xuat_dao.layChiTiet(dsXuat.get(row).getMaPX())) {
                mdlCT.addRow(new Object[] { ct.getNguyenLieu().getTenNL(), ct.getNguyenLieu().getDonViTinh(), TienIch.soThuc(ct.getSoLuong()) });
            }
        }
    }
}
