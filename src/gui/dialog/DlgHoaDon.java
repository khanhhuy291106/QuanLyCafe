package gui.dialog;

import entity.HoaDon;
import gui.component.BoGocButton;
import gui.component.UI;
import gui.util.AppColor;
import gui.util.ExportUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Xem trước hóa đơn dạng giấy in và cho phép in. */
public class DlgHoaDon extends JDialog {

    /** khachDua âm = không hiện dòng khách đưa/tiền thừa (in lại hóa đơn cũ). */
    public DlgHoaDon(Window owner, HoaDon hd, double khachDua, double tienThua) {
        super(owner, "Hóa đơn " + hd.getMaHD(), ModalityType.APPLICATION_MODAL);
        setIconImage(UI.appIcon());
        List<String> lines = ExportUtil.noiDungHoaDon(hd, khachDua, tienThua);
        JTextArea ta = new JTextArea(String.join("\n", lines));
        ta.setEditable(false);
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        ta.setBackground(Color.WHITE);
        ta.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        JScrollPane sp = new JScrollPane(ta);
        sp.setBorder(BorderFactory.createLineBorder(AppColor.LINE));

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        root.setBackground(AppColor.BG);
        root.add(sp, BorderLayout.CENTER);
        JPanel bt = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bt.setOpaque(false);
        BoGocButton in = UI.button("In hóa đơn", AppColor.BROWN);
        BoGocButton dong = new BoGocButton("Đóng", AppColor.BROWN).outline(AppColor.BROWN);
        bt.add(dong);
        bt.add(in);
        root.add(bt, BorderLayout.SOUTH);
        setContentPane(root);
        in.addActionListener(e -> ExportUtil.inVanBan(lines, this));
        dong.addActionListener(e -> dispose());
        setSize(440, 620);
        setLocationRelativeTo(owner);
    }
}
