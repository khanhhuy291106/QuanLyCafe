package gui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import javax.swing.JFormattedTextField;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

// Ô chọn ngày bằng lịch (thư viện JDatePicker), đọc/ghi dạng LocalDate
public class DinhDangNgay extends JFormattedTextField.AbstractFormatter {
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

    public static JDatePickerImpl taoDatePicker(LocalDate d) {
        UtilDateModel m = new UtilDateModel();
        Properties prop = new Properties();
        prop.put("text.today", "Hôm nay");
        prop.put("text.month", "Tháng");
        prop.put("text.year", "Năm");
        JDatePickerImpl dp = new JDatePickerImpl(new JDatePanelImpl(m, prop), new DinhDangNgay());
        dp.setPreferredSize(new java.awt.Dimension(160, 30));
        datNgay(dp, d);
        return dp;
    }

    public static LocalDate layNgay(JDatePickerImpl dp) {
        Date d = (Date) dp.getModel().getValue();
        if (d == null) return null;
        return new java.sql.Date(d.getTime()).toLocalDate();
    }

    public static void datNgay(JDatePickerImpl dp, LocalDate d) {
        UtilDateModel m = (UtilDateModel) dp.getModel();
        if (d == null) {
            m.setSelected(false);
            m.setValue(null);
        } else {
            m.setValue(java.sql.Date.valueOf(d));
            m.setSelected(true);
        }
    }

    @Override
    public Object stringToValue(String text) throws ParseException {
        if (text == null || text.trim().isEmpty()) return null;
        Calendar c = Calendar.getInstance();
        c.setTime(dateFormatter.parse(text));
        return c;
    }

    @Override
    public String valueToString(Object value) throws ParseException {
        if (value == null) return "";
        return dateFormatter.format(((Calendar) value).getTime());
    }
}
