package gui;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import dao.NguyenLieu_DAO;
import entity.NguyenLieu;

// Bảng "tồn kho hiện tại" nhỏ bên phải hai màn hình lập phiếu nhập / xuất
public class BangTonKho extends JPanel {
    private NguyenLieu_DAO dao = new NguyenLieu_DAO();
    private DefaultTableModel mdl = TienIch.moHinh("Nguyên liệu", "Tồn", "Tối thiểu");
    private JTable tbl = TienIch.bang(mdl);
    private ArrayList<NguyenLieu> ds = new ArrayList<>();

    public BangTonKho() {
        setLayout(new BorderLayout(0, 6));
        setOpaque(false);
        setPreferredSize(new Dimension(330, 100));
        add(TienIch.tieuDe("Tồn kho hiện tại (đỏ = sắp hết)"), BorderLayout.NORTH);
        TienIch.canPhai(tbl, 1, 2);
        TienIch.doRong(tbl, 120, 90, 90);
        TienIch.tomauCanhBao(tbl, r -> r < ds.size() && ds.get(r).sapHet());
        add(TienIch.cuon(tbl), BorderLayout.CENTER);
    }

    // Nạp lại từ CSDL và trả về danh sách nguyên liệu
    public ArrayList<NguyenLieu> taiLai() {
        ds = dao.getAll();
        mdl.setRowCount(0);
        for (NguyenLieu n : ds) {
            mdl.addRow(new Object[] { n.getTenNL(), TienIch.soThuc(n.getSoLuongTon()) + " " + n.getDonViTinh(),
                    TienIch.soThuc(n.getMucToiThieu()) + " " + n.getDonViTinh() });
        }
        return ds;
    }
}
