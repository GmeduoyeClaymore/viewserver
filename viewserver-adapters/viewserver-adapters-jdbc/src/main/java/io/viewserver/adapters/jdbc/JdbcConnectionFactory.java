package io.viewserver.adapters.jdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class JdbcConnectionFactory {

    private String username;
    private String password;


    public JdbcConnectionFactory(String username, String password) {
        this.username = username;
        this.password = password;

    }

    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection(username, password);
    }

    public abstract DataSource getDataSource();


}
