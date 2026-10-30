package dao;

import connect.ConnectDB;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Các hàm dùng chung cho mọi DAO: chạy truy vấn, cập nhật, transaction, sinh mã. */
public abstract class BaseDAO {

    @FunctionalInterface
    public interface Mapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    @FunctionalInterface
    public interface TxBody {
        void run(Connection c) throws SQLException;
    }

    protected static Connection con() {
        try {
            return ConnectDB.getInstance().getConnection();
        } catch (SQLException e) {
            throw DaoException.from(e);
        }
    }

    protected static void setParams(PreparedStatement ps, Object... p) throws SQLException {
        for (int i = 0; i < p.length; i++) {
            Object v = p[i];
            int idx = i + 1;
            if (v == null) ps.setNull(idx, Types.VARCHAR);
            else if (v instanceof LocalDate) ps.setDate(idx, Date.valueOf((LocalDate) v));
            else if (v instanceof LocalDateTime) ps.setTimestamp(idx, Timestamp.valueOf((LocalDateTime) v));
            else if (v instanceof Boolean) ps.setBoolean(idx, (Boolean) v);
            else ps.setObject(idx, v);
        }
    }

    protected static <T> List<T> query(Connection c, String sql, Mapper<T> m, Object... p) throws SQLException {
        List<T> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            setParams(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(m.map(rs));
            }
        }
        return list;
    }

    protected static <T> List<T> query(String sql, Mapper<T> m, Object... p) {
        try {
            return query(con(), sql, m, p);
        } catch (SQLException e) {
            throw DaoException.from(e);
        }
    }

    protected static <T> T queryOne(String sql, Mapper<T> m, Object... p) {
        List<T> l = query(sql, m, p);
        return l.isEmpty() ? null : l.get(0);
    }

    protected static int exec(Connection c, String sql, Object... p) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            setParams(ps, p);
            return ps.executeUpdate();
        }
    }

    protected static int update(String sql, Object... p) {
        try {
            return exec(con(), sql, p);
        } catch (SQLException e) {
            throw DaoException.from(e);
        }
    }

    /** Chạy nhiều lệnh trong cùng một transaction: lỗi thì rollback toàn bộ. */
    protected static void inTransaction(TxBody body) {
        Connection c = con();
        try {
            c.setAutoCommit(false);
            try {
                body.run(c);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                if (e instanceof SQLException) throw DaoException.from((SQLException) e);
                throw (RuntimeException) e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw DaoException.from(e);
        }
    }

    /** Sinh mã tiếp theo, ví dụ prefix "NV", digits 3 -> NV008. */
    protected static String nextId(Connection c, String table, String col, String prefix, int digits) throws SQLException {
        String sql = "SELECT ISNULL(MAX(CAST(SUBSTRING(" + col + ", ?, 20) AS INT)), 0) FROM " + table
                + " WHERE " + col + " LIKE ? AND ISNUMERIC(SUBSTRING(" + col + ", ?, 20)) = 1";
        int pos = prefix.length() + 1;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, pos);
            ps.setString(2, prefix + "%");
            ps.setInt(3, pos);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return prefix + String.format("%0" + digits + "d", rs.getInt(1) + 1);
            }
        }
    }

    protected static String nextId(String table, String col, String prefix, int digits) {
        try {
            return nextId(con(), table, col, prefix, digits);
        } catch (SQLException e) {
            throw DaoException.from(e);
        }
    }

    protected static LocalDate date(ResultSet rs, String col) throws SQLException {
        Date d = rs.getDate(col);
        return d == null ? null : d.toLocalDate();
    }
}
