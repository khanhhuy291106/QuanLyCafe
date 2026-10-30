package gui.dialog;

import dao.PhieuNhapDAO;
import dao.PhieuXuatDAO;
import entity.ChiTietPhieuNhap;
import entity.ChiTietPhieuXuat;
import entity.PhieuNhap;
import entity.PhieuXuat;
import gui.component.UI;
import gui.util.AppColor;
import gui.util.FormatUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Xem lại các phiếu nhập hoặc phiếu xuất đã lập, kèm chi tiết từng phiếu. */
public class DlgLichSuPhieu extends JDialog {
    private final boolean nhap;
    private final PhieuNhapDAO nhapDAO = new PhieuNhapDAO();
    private final PhieuXuatDAO xuatDAO = new PhieuXuatDAO();
    private List<PhieuNhap> dsNhap = new ArrayList<>();
    private List<PhieuXuat> dsXuat = new ArrayList<>();
    private final DefaultTableModel mdlPhieu;
    private final DefaultTableModel mdlCT;

    public DlgLichSuPhieu(Window owner, boolean nhap) {
        super(owner, nhap ? "Lịch sử phiếu nhập" : "Lịch sử phiếu xuất", ModalityType.APPLICATION_MODAL);
        this.nhap = nhap;
        setIconImage(UI.appIcon());
        mdlPhieu = nhap ? UI.model("Mã PN", "Ngày nhập", "Nhân viên", "Nhà cung cấp", "Tổng tiền")
                : UI.model("Mã PX", "Ngày xuất", "Nhân viên", "Lý do");
        mdlCT = nhap ? UI.model("Nguyên liệu", "ĐVT", "Số lượng", "Đơn giá", "Thành tiền")
                : UI.model("Nguyên liệu", "ĐVT", "Số lượng xuất");
        JTable tPhieu = UI.table(mdlPhieu);
        JTable tCT = UI.table(mdlCT);
        if (nhap) {
            UI.alignRight(tPhieu, 4);
            UI.alignRight(tCT, 2, 3, 4);
        } else {
            UI.alignRight(tCT, 2);
        }
        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.setOpaque(false);
        top.add(UI.title(nhap ? "Các phiếu nhập" : "Các phiếu xuất"), BorderLayout.NORTH);
        top.add(UI.scroll(tPhieu), BorderLayout.CENTER);
        JPanel bot = new JPanel(new BorderLayout(0, 6));
        bot.setOpaque(false);
        bot.add(UI.title("Chi tiết phiếu đang chọn"), BorderLayout.NORTH);
        bot.add(UI.scroll(tCT), BorderLayout.CENTER);
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bot);
        sp.setResizeWeight(0.5);
        sp.setBorder(null);
        sp.setOpaque(false);
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        root.setBackground(AppColor.BG);
        root.add(sp, BorderLayout.CENTER);
        setContentPane(root);

        UI.run(() -> {
            if (nhap) {
                dsNhap = nhapDAO.getAll();
                for (PhieuNhap p : dsNhap)
                    mdlPhieu.addRow(new Object[]{p.getMaPN(), FormatUtil.ngay(p.getNgayNhap()), p.getNhanVien().getHoTen(),
                            p.getNhaCungCap().getTenNCC(), FormatUtil.tienD(p.getTongTien())});
            } else {
                dsXuat = xuatDAO.getAll();
                for (PhieuXuat p : dsXuat)
                    mdlPhieu.addRow(new Object[]{p.getMaPX(), FormatUtil.ngay(p.getNgayXuat()), p.getNhanVien().getHoTen(), p.getLyDo()});
            }
        });
        tPhieu.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tPhieu.getSelectedRow() >= 0) UI.run(() -> hienChiTiet(tPhieu.getSelectedRow()));
        });
        if (mdlPhieu.getRowCount() > 0) tPhieu.setRowSelectionInterval(0, 0);
        setSize(820, 560);
        setLocationRelativeTo(owner);
    }

    private void hienChiTiet(int row) {
        mdlCT.setRowCount(0);
        if (nhap) {
            for (ChiTietPhieuNhap ct : nhapDAO.layChiTiet(dsNhap.get(row).getMaPN()))
                mdlCT.addRow(new Object[]{ct.getNguyenLieu().getTenNL(), ct.getNguyenLieu().getDonViTinh(), FormatUtil.soThuc(ct.getSoLuong()),
                        FormatUtil.tien(ct.getDonGia()), FormatUtil.tien(ct.thanhTien())});
        } else {
            for (ChiTietPhieuXuat ct : xuatDAO.layChiTiet(dsXuat.get(row).getMaPX()))
                mdlCT.addRow(new Object[]{ct.getNguyenLieu().getTenNL(), ct.getNguyenLieu().getDonViTinh(), FormatUtil.soThuc(ct.getSoLuong())});
        }
    }
}
