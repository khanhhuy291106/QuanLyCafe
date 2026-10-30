package gui.component;

import gui.util.AppColor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Khung dùng chung cho các màn hình dạng "form bên trái + bảng bên phải" (nhân viên, khuyến mãi, nguyên liệu...).
 * Lớp con chỉ cần viết: buildForm, load, fillForm, clearForm, them, sua, xoa rồi gọi init() ở cuối constructor.
 */
public abstract class CrudPanel extends JPanel implements Refreshable {
    protected final DefaultTableModel model;
    protected final JTable table;
    protected final FormPanel form = new FormPanel();
    protected final JTextField txtTim = UI.field(18);
    protected final JPanel pnlSearch = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 4));
    protected final JPanel pnlButtons = new JPanel(new GridLayout(0, 2, 8, 8));
    protected final JPanel pnlSouth = new JPanel(new BorderLayout());
    protected final BoGocButton btnThem = UI.button("Thêm", AppColor.GREEN);
    protected final BoGocButton btnSua = UI.button("Sửa", AppColor.BLUE);
    protected final BoGocButton btnXoa = UI.button("Xóa", AppColor.RED);
    protected final BoGocButton btnLamMoi = UI.button("Làm mới", new Color(0xB8A890));
    private final String formTitle;
    private JComponent formCard;
    private int formWidth = 320;

    protected CrudPanel(String formTitle, String... columns) {
        this.formTitle = formTitle;
        this.model = UI.model(columns);
        this.table = UI.table(model);
    }

    protected void setFormWidth(int w) {
        this.formWidth = w;
    }

    protected abstract void buildForm(FormPanel f);

    protected abstract void load(String keyword);

    protected abstract void fillForm(int row);

    protected abstract void clearForm();

    protected abstract void them();

    protected abstract void sua();

    protected abstract void xoa();

    protected String xacNhanXoa() {
        return "Bạn có chắc muốn xóa dòng đang chọn?";
    }

    /** Gọi ở cuối constructor của lớp con (sau khi đã tạo các ô nhập). */
    protected void init() {
        setLayout(new BorderLayout(12, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(AppColor.BG);

        buildForm(form);
        form.glue();
        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 8), Color.WHITE, AppColor.LINE, 10);
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));
        card.setPreferredSize(new Dimension(formWidth, 100));
        card.add(UI.title(formTitle), BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        pnlButtons.setOpaque(false);
        pnlButtons.add(btnThem);
        pnlButtons.add(btnSua);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnLamMoi);
        card.add(pnlButtons, BorderLayout.SOUTH);
        formCard = card;
        add(card, BorderLayout.WEST);

        pnlSearch.setOpaque(false);
        BoGocButton btnTim = UI.button("Tìm kiếm", AppColor.BROWN);
        pnlSearch.add(txtTim);
        pnlSearch.add(btnTim);
        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setOpaque(false);
        right.add(pnlSearch, BorderLayout.NORTH);
        right.add(UI.scroll(table), BorderLayout.CENTER);
        pnlSouth.setOpaque(false);
        right.add(pnlSouth, BorderLayout.SOUTH);
        add(right, BorderLayout.CENTER);

        btnTim.addActionListener(UI.on(this::refresh));
        txtTim.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) refresh();
            }
        });
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) fillForm(table.getSelectedRow());
        });
        btnThem.addActionListener(UI.on(this::them));
        btnSua.addActionListener(e -> {
            if (chonDong()) UI.run(this::sua);
        });
        btnXoa.addActionListener(e -> {
            if (chonDong() && UI.confirm(xacNhanXoa())) UI.run(this::xoa);
        });
        btnLamMoi.addActionListener(UI.on(this::lamMoi));
    }

    /** Chế độ chỉ xem: ẩn form và các nút thêm/sửa/xóa, chỉ còn bảng dữ liệu. */
    protected void setReadOnlyMode() {
        formCard.setVisible(false);
    }

    private boolean chonDong() {
        if (table.getSelectedRow() < 0) {
            UI.warn("Hãy chọn một dòng trong bảng trước.");
            return false;
        }
        return true;
    }

    /** Xóa chọn, xóa form, nạp lại bảng. */
    protected void lamMoi() {
        table.clearSelection();
        txtTim.setText("");
        clearForm();
        load("");
    }

    @Override
    public void refresh() {
        UI.run(() -> load(txtTim.getText().trim()));
    }
}
