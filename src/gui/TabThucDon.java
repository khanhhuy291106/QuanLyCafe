package gui;

import java.awt.*;
import javax.swing.*;

// Thực đơn: quản lý món, loại món, size, topping. CHỈ QUẢN LÝ được vào màn hình này.
public class TabThucDon extends JPanel implements TaiLai {
    private JTabbedPane tabs = new JTabbedPane();

    public TabThucDon() {
        setLayout(new BorderLayout());
        setBackground(TienIch.BG);
        tabs.setFont(TienIch.FONT_B);
        tabs.addTab("Món", new QLMon());
        tabs.addTab("Loại món", new QLLoaiMon());
        tabs.addTab("Size", new QLSize());
        tabs.addTab("Topping", new QLTopping());
        tabs.addChangeListener(e -> taiLai());
        add(tabs, BorderLayout.CENTER);
    }

    @Override
    public void taiLai() {
        Component c = tabs.getSelectedComponent();
        if (c instanceof TaiLai) ((TaiLai) c).taiLai();
    }
}
