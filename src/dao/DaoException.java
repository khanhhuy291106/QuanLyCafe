package dao;

import java.sql.SQLException;

/** Lỗi khi làm việc với CSDL, kèm thông báo tiếng Việt dễ hiểu để hiện cho người dùng. */
public class DaoException extends RuntimeException {
    public DaoException(String message) {
        super(message);
    }

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public static DaoException from(SQLException e) {
        int code = e.getErrorCode();
        String msg;
        if (code == 547) msg = "Dữ liệu này đang được sử dụng ở nơi khác nên không thể xóa hoặc sửa theo cách này.";
        else if (code == 2627 || code == 2601) msg = "Dữ liệu bị trùng (mã hoặc tên đã tồn tại).";
        else if (e.getSQLState() != null && e.getSQLState().startsWith("08"))
            msg = "Không kết nối được SQL Server. Hãy kiểm tra config/config.properties và SQL Server đã chạy chưa.\n(" + e.getMessage() + ")";
        else msg = "Lỗi cơ sở dữ liệu: " + e.getMessage();
        return new DaoException(msg, e);
    }
}
