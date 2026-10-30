package gui.component;

import gui.util.AppColor;

import javax.swing.*;
import java.awt.*;

/** Nút bo góc tự vẽ, có hiệu ứng khi rê chuột và khi bị khóa. */
public class BoGocButton extends JButton {
    private Color bg;
    private Color fg = Color.WHITE;
    private boolean outline;
    private int radius = 10;

    public BoGocButton(String text, Color bg) {
        super(text);
        this.bg = bg;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(AppColor.FONT_B);
        setMargin(new Insets(6, 14, 6, 14));
        setPreferredSize(new Dimension(Math.max(80, getPreferredSize().width), 32));
    }

    /** Kiểu nút viền, nền trắng, chữ cùng màu viền. */
    public BoGocButton outline(Color color) {
        this.outline = true;
        this.bg = color;
        this.fg = color;
        return this;
    }

    public BoGocButton radius(int r) {
        this.radius = r;
        return this;
    }

    public BoGocButton foreground(Color c) {
        this.fg = c;
        return this;
    }

    public void setBg(Color c) {
        this.bg = c;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        Color text;
        if (!isEnabled()) {
            g2.setColor(new Color(0xD8D0C4));
            g2.fillRoundRect(0, 0, w - 1, h - 1, radius, radius);
            text = new Color(0x8A8178);
        } else if (outline) {
            g2.setColor(getModel().isRollover() ? new Color(0xF7F0E6) : Color.WHITE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, radius, radius);
            g2.setColor(bg);
            g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);
            text = fg;
        } else {
            Color c = bg;
            if (getModel().isPressed()) c = bg.darker();
            else if (getModel().isRollover()) c = new Color(Math.min(255, bg.getRed() + 18), Math.min(255, bg.getGreen() + 18), Math.min(255, bg.getBlue() + 18));
            g2.setColor(c);
            g2.fillRoundRect(0, 0, w - 1, h - 1, radius, radius);
            text = fg;
        }
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(getText())) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(text);
        g2.drawString(getText(), tx, ty);
        g2.dispose();
    }
}
