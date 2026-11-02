package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TaoMa {
    // Sinh mã tiếp theo, ví dụ tienTo "NV", soChuSo 3 -> NV008
    public static String tiepTheo(Connection con, String bang, String cot, String tienTo, int soChuSo) throws SQLException {
        int viTri = tienTo.length() + 1;
        String sql = "SELECT ISNULL(MAX(CAST(SUBSTRING(" + cot + ", " + viTri + ", 20) AS INT)), 0) FROM " + bang
                + " WHERE " + cot + " LIKE ? AND ISNUMERIC(SUBSTRING(" + cot + ", " + viTri + ", 20)) = 1";
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setString(1, tienTo + "%");
        ResultSet rs = stmt.executeQuery();
        rs.next();
        return tienTo + String.format("%0" + soChuSo + "d", rs.getInt(1) + 1);
    }
}
