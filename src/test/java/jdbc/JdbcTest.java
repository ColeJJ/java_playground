package jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tun.interfaces.SqlRunnable;

public class JdbcTest {

    private Connection con;

    @BeforeEach
    public void init() {
        doInJDBC(() -> {
            con = DriverManager.getConnection("jdbc:p6spy:postgresql://localhost:5432/my_db", "admin", "p123");
            con.setAutoCommit(false);
        });
    }

    @AfterEach
    public void close() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                System.out.println("""
                    
                    ''''''''''''''''''''''''''''''''''''''''''''''
                    Connection has been successfully closed.
                    ''''''''''''''''''''''''''''''''''''''''''''''""");
            }
        } catch (Exception e) {
            rollback();
            e.printStackTrace();
        }
    }

    @Test
    public void createTable() {
        doInJDBC(() -> {
            final String sql = "CREATE TABLE IF NOT EXISTS post (id SERIAL PRIMARY KEY, title VARCHAR(100), content TEXT)";
            final Statement statement = con.createStatement();
            statement.execute(sql);
        });
    }

    @Test
    public void select() {
        doInJDBC(() -> {
            final String sql = "SELECT * FROM post";
            final Statement statement = con.createStatement();
            statement.executeQuery(sql);
        });
    }

    @Test
    public void prepareSelect() {
        doInJDBC(() -> {
            final String sql = "SELECT * FROM post";
            final PreparedStatement statement = con.prepareStatement(sql);
            statement.execute();
        });
    }

    @Test
    public void prepareBatchInsert() {
        doInJDBC(() -> {
            final String sql = "INSERT INTO post (title, content) VALUES (?, ?)";
            final PreparedStatement statement = con.prepareStatement(sql);
            statement.setString(1, "Title Whatever");
            statement.setString(1, "Hier ist ein content, der ziemlich random ist, aber egal.");
            statement.addBatch();

            statement.setString(1, "Title Whatever 2");
            statement.setString(1, "Hier ist noch mehr random content, der noch viel schlimmer ist als der andere, aber noch mehr egal.");
            statement.addBatch();
        });
    }

    @Test
    public void batchInsert() {
        doInJDBC(() -> {
            final String sql = "INSERT INTO post (title, content) VALUES ('Title Whatever','Hier ist ein content, der ziemlich random ist, aber egal.')";
            final String sql_2 = "INSERT INTO post (title, content) VALUES ('Title Whatever 2','Hier ist noch mehr random content, der noch viel schlimmer ist als der andere, aber noch mehr egal.')";
            final Statement statement = con.createStatement();
            statement.addBatch(sql);
            statement.addBatch(sql_2);
            statement.executeBatch();
        });
    }

    @Test
    public void batchStatement() {
        doInJDBC(() -> {
            final String sql_insert = "INSERT INTO post (title, content) VALUES ('Title 1', 'Content 1'), ('Title 2', 'Content 2'), ('Title 3', 'Content 3')";
            final String sql_select = "SELECT * FROM post";
            final Statement statement = con.createStatement();
            statement.addBatch(sql_insert);
            statement.addBatch(sql_select);
            statement.executeBatch();
        });
    }

    private void doInJDBC(final SqlRunnable runnable) {
        try {
            runnable.run();
            con.commit();
        } catch (Exception e) {
            rollback();
            e.printStackTrace();
        }
    }

    private void rollback() {
        try {
            if (con != null && !con.isClosed()) {
                con.rollback();
                System.out.println("Transaction has been rollbacked.");
            }
        } catch (final SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to rollback the transation: " + e.getMessage());
        }
    }
}

