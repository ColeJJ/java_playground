package org.tun.interfaces;

import java.sql.SQLException;

@FunctionalInterface
public interface SqlRunnable {
    void run() throws SQLException;
}
