package com.vehiclerental.common.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * MYSQL implementation of {@link DataStore}.
 * ------------------------------------------
 * readAll  = SELECT ... FROM table ORDER BY id
 * writeAll = one TRANSACTION: DELETE FROM table, then INSERT every record
 *            (the store's contract is "make the table equal to this list",
 *             exactly like overwriting a file - that is what keeps the file
 *             and database implementations interchangeable)
 * isEmpty  = SELECT COUNT(*)
 *
 * The real SQL work (columns, row mapping, parameters) lives in the
 * {@link TableMapping} of each entity, so this class stays generic.
 */
public class MySqlStore<T> implements DataStore<T> {

    private static final Logger log = LoggerFactory.getLogger(MySqlStore.class);

    private final JdbcTemplate jdbc;
    private final TableMapping<T> mapping;

    public MySqlStore(JdbcTemplate jdbc, TableMapping<T> mapping) {
        this.jdbc = jdbc;
        this.mapping = mapping;
    }

    @Override
    public List<T> readAll() {
        String sql = "SELECT " + mapping.columns() + " FROM " + mapping.table() + " ORDER BY id";
        return jdbc.query(sql, (rs, rowNum) -> mapping.map(rs));
    }

    @Override
    public void writeAll(List<T> items) {
        int columnCount = mapping.columns().split(",").length;
        String placeholders = String.join(", ",
                java.util.Collections.nCopies(columnCount, "?"));
        String insert = "INSERT INTO " + mapping.table() + " (" + mapping.columns() + ") VALUES (" + placeholders + ")";

        jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Object>) con -> {
            boolean auto = con.getAutoCommit();
            con.setAutoCommit(false);
            try (var st = con.createStatement()) {
                st.executeUpdate("DELETE FROM " + mapping.table());
                for (T item : items) {
                    try (var ps = con.prepareStatement(insert)) {
                        Object[] values = mapping.args(item);
                        for (int i = 0; i < values.length; i++) {
                            ps.setObject(i + 1, values[i]);
                        }
                        ps.executeUpdate();
                    }
                }
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(auto);
            }
            return null;
        });
        log.debug("Synced {} record(s) to mysql table {}", items.size(), mapping.table());
    }

    @Override
    public boolean isEmpty() {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM " + mapping.table(), Long.class);
        return count == null || count == 0;
    }

    @Override
    public String describe() {
        return "mysql:" + mapping.table();
    }
}
