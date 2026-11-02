package gui;

import java.awt.*;
import javax.swing.*;
import entity.NhanVien;

// Kho nguyên liệu. Quản lý có đủ 4 thẻ: Tồn kho, Phiếu nhập, Phiếu xuất, Nhà cung cấp.
// Nhân viên (thu ngân, pha chế) chỉ xem được thẻ Tồn kho, không sửa được gì.
public class TabKho extends JPanel implements TaiLai {
    private JTabbedPane tabs = new JTabbedPane();

    public TabKho(NhanVien nv) {
        setLayout(new BorderLayout());
        setBackground(TienIch.BG);
        tabs.setFont(TienIch.FONT_B);
        tabs.addTab("Tồn kho", new QLTonKho(!nv.laQuanLy()));
        if (nv.laQuanLy()) {
            tabs.addTab("Phiếu nhập", new QLPhieuNhap(nv));
            tabs.addTab("Phiếu xuất", new QLPhieuXuat(nv));
            tabs.addTab("Nhà cung cấp", new QLNhaCungCap());
        }
        tabs.addChangeListener(e -> taiLai());
        add(tabs, BorderLayout.CENTER);
    }

    // Mở thẳng một thẻ (ví dụ nút "Lập phiếu nhập" ở trang chủ)
    public void chonThe(String ten) {
        for (int i = 0; i < tabs.getTabCount(); i++) {
            if (tabs.getTitleAt(i).equals(ten)) tabs.setSelectedIndex(i);
        }
    }

    @Override
    public void taiLai() {
        Component c = tabs.getSelectedComponent();
        if (c instanceof TaiLai) ((TaiLai) c).taiLai();
    }
}
