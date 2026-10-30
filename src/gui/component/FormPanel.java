package gui.component;

import gui.util.AppColor;

import javax.swing.*;
import java.awt.*;

/** Panel dạng form: mỗi dòng gồm nhãn bên trái và ô nhập bên phải, dùng GridBagLayout. */
public class FormPanel extends JPanel {
    private int row = 0;
    private int labelWidth = 92;

    public FormPanel() {
        super(new GridBagLayout());
        setOpaque(false);
    }

    public FormPanel labelWidth(int w) {
        this.labelWidth = w;
        return this;
    }

    public <T extends JComponent> T row(String label, T comp) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = row++;
        g.anchor = GridBagConstraints.WEST;
        JLabel l = new JLabel(label);
        l.setFont(AppColor.FONT);
        l.setForeground(AppColor.INK);
        l.setPreferredSize(new Dimension(labelWidth, 28));
        g.gridx = 0;
        g.insets = new Insets(4, 0, 4, 6);
        add(l, g);
        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 0);
        add(comp, g);
        return comp;
    }

    /** Thêm một thành phần chiếm cả hai cột (ví dụ nhóm radio, ảnh xem trước). */
    public <T extends JComponent> T full(T comp) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = row++;
        g.gridx = 0;
        g.gridwidth = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(4, 0, 4, 0);
        add(comp, g);
        return comp;
    }

    public void section(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppColor.FONT_B);
        l.setForeground(AppColor.BROWN);
        l.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        full(l);
    }

    /** Đẩy các dòng lên trên khi form ngắn hơn khung. Gọi sau khi đã thêm hết các dòng. */
    public void glue() {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = row++;
        g.gridx = 0;
        g.gridwidth = 2;
        g.weighty = 1;
        add(Box.createVerticalGlue(), g);
    }
}
