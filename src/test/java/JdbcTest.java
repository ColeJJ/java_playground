import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tun.interfaces.SqlRunnable;

public class JdbcTest {

    private Connection con;

    @BeforeEach
    public void init() {
        handleSql(() -> con = DriverManager.getConnection("jdbc:p6spy:postgresql://localhost:5432/my_db", "admin", "p123"));
    }

    @AfterEach
    public void close() {
        handleSql(() -> {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        });
    }

    @Test
    public void createTable() {
        handleSql(() -> {
            final String sql = "CREATE TABLE IF NOT EXISTS post (id SERIAL PRIMARY KEY, title VARCHAR(100), content TEXT)";
            final Statement statement = con.createStatement();
            statement.execute(sql);
        });
    }

    @Test
    public void batch() {
        handleSql(() -> {
            final String sql_insert = "INSERT INTO post (title, content) VALUES ('Title 1', 'Content 1'), ('Title 2', 'Content 2'), ('Title 3', 'Content 3')";
            final String sql_select = "SELECT * FROM post";
            final Statement statement = con.createStatement();
            statement.addBatch(sql_insert);
            statement.addBatch(sql_select);
            statement.executeBatch();
        });
    }

    private void handleSql(final SqlRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

