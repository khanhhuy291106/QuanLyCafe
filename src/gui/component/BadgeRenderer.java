package gui.component;

import gui.util.AppColor;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/** Vẽ ô trạng thái trong bảng thành "nhãn" bo tròn có màu theo trạng thái. */
public class BadgeRenderer extends JPanel implements TableCellRenderer {
    private String text = "";
    private Color rowBg = Color.WHITE;

    public BadgeRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
        text = v == null ? "" : v.toString();
        rowBg = sel ? AppColor.SELECT : (row % 2 == 0 ? Color.WHITE : AppColor.ZEBRA);
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(rowBg);
        g2.fillRect(0, 0, getWidth(), getHeight());
        if (!text.isEmpty()) {
            Font f = AppColor.FONT_B.deriveFont(11f);
            g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(text) + 18;
            int h = 18;
            int x = (getWidth() - w) / 2;
            int y = (getHeight() - h) / 2;
            g2.setColor(AppColor.statusColor(text));
            g2.fillRoundRect(x, y, w, h, h, h);
            g2.setColor(Color.WHITE);
            g2.drawString(text, x + 9, y + (h - fm.getHeight()) / 2 + fm.getAscent());
        }
        g2.dispose();
    }
}
