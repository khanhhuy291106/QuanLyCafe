package gui.component;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File; // Thêm import này

/** Biểu tượng ly cà phê vẽ bằng Java2D hoặc load từ file ảnh. */
public class CupIcon implements Icon {
    private final int size;
    private final Color color;
    private final boolean steam;
    private Image img = null; // <-- THÊM 1: Biến chứa ảnh nếu có

    // Constructor cũ giữ nguyên
    public CupIcon(int size, Color color, boolean steam) {
        this.size = size;
        this.color = color;
        this.steam = steam;
    }

    // <-- THÊM 2: Constructor mới chỉ nhận (size, tên_file_ảnh)
    public CupIcon(int size, String tenFileAnh) {
        this.size = size;
        this.color = new Color(140, 90, 45);
        this.steam = true;
        if (tenFileAnh != null && !tenFileAnh.trim().isEmpty()) {
            File f = new File("resources/images/mon/" + tenFileAnh.trim());
            if (f.exists()) {
                this.img = new ImageIcon(f.getAbsolutePath()).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            }
        }
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        // <-- THÊM 3: Nếu có ảnh trong folder images thì vẽ ảnh ra và kết thúc
        if (img != null) {
            g.drawImage(img, x, y, c);
            return;
        }

        // Toàn bộ code vẽ 2D ban đầu của bạn giữ nguyên bên dưới:
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        double s = size * 0.62;
        double cx = x + size / 2.0 - s * 0.08;
        double top = y + size * 0.3;
        g2.fill(new RoundRectangle2D.Double(cx - s / 2, top, s, s * 0.62, s * 0.18, s * 0.18));
        g2.setStroke(new BasicStroke(Math.max(1.5f, size * 0.06f)));
        g2.draw(new Arc2D.Double(cx + s / 2 - s * 0.14, top + s * 0.1, s * 0.34, s * 0.34, -90, 180, Arc2D.OPEN));
        g2.fill(new RoundRectangle2D.Double(cx - s * 0.7, top + s * 0.66, s * 1.4, s * 0.07, 3, 3));
        if (steam) {
            g2.setStroke(new BasicStroke(Math.max(1.2f, size * 0.04f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = -1; i <= 1; i++) {
                double sx = cx + i * s * 0.22;
                g2.draw(new CubicCurve2D.Double(sx, top - 2, sx - s * 0.1, top - s * 0.12, sx + s * 0.1, top - s * 0.2, sx, top - s * 0.3));
            }
        }
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}