package gui.component;

import javax.swing.*;
import java.awt.*;

/** Panel nền bo góc có viền, dùng làm "thẻ" (card) trên giao diện. */
public class RoundedPanel extends JPanel {
    private Color fill;
    private Color border;
    private int arc;

    public RoundedPanel(LayoutManager lm, Color fill, Color border, int arc) {
        super(lm);
        this.fill = fill;
        this.border = border;
        this.arc = arc;
        setOpaque(false);
    }

    public RoundedPanel(Color fill, Color border, int arc) {
        this(new BorderLayout(), fill, border, arc);
    }

    public void setFill(Color c) {
        this.fill = c;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        if (border != null) {
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
