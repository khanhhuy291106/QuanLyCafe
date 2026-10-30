package gui.component;

import gui.util.AppColor;

import javax.swing.*;
import java.awt.*;

/** Nhãn bo tròn nền màu (dùng cho trạng thái đơn hàng). */
public class PillLabel extends JLabel {
    private Color nen = AppColor.GRAY;

    public PillLabel() {
        setOpaque(false);
        setForeground(Color.WHITE);
        setFont(AppColor.FONT_B.deriveFont(11f));
        setBorder(BorderFactory.createEmptyBorder(3, 12, 3, 12));
    }

    public void setNen(Color c) {
        nen = c;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(nen);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}
