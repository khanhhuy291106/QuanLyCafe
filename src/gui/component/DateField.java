package gui.component;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/** Ô chọn ngày bằng lịch (thư viện JDatePicker), đọc/ghi dạng LocalDate. */
public class DateField extends JPanel {
    private final UtilDateModel model = new UtilDateModel();
    private final JDatePickerImpl picker;

    private static class Fmt extends JFormattedTextField.AbstractFormatter {
        private final SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");

        @Override
        public Object stringToValue(String text) throws ParseException {
            if (text == null || text.trim().isEmpty()) return null;
            Calendar c = Calendar.getInstance();
            c.setTime(f.parse(text));
            return c;
        }

        @Override
        public String valueToString(Object value) {
            return value == null ? "" : f.format(((Calendar) value).getTime());
        }
    }

    public DateField() {
        super(new BorderLayout());
        setOpaque(false);
        Properties p = new Properties();
        p.put("text.today", "Hôm nay");
        p.put("text.month", "Tháng");
        p.put("text.year", "Năm");
        picker = new JDatePickerImpl(new JDatePanelImpl(model, p), new Fmt());
        picker.setOpaque(false);
        add(picker, BorderLayout.CENTER);
        setPreferredSize(new Dimension(160, 30));
    }

    public DateField(LocalDate d) {
        this();
        setDate(d);
    }

    public LocalDate getDate() {
        Date d = model.getValue();
        return d == null ? null : new java.sql.Date(d.getTime()).toLocalDate();
    }

    public void setDate(LocalDate d) {
        if (d == null) {
            model.setSelected(false);
            model.setValue(null);
        } else {
            model.setValue(java.sql.Date.valueOf(d));
            model.setSelected(true);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        picker.setEnabled(enabled);
        picker.getJFormattedTextField().setEnabled(enabled);
        for (Component c : picker.getComponents()) c.setEnabled(enabled);
    }
}
