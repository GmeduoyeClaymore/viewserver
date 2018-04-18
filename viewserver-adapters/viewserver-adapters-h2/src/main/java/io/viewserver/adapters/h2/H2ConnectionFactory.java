package io.viewserver.adapters.h2;

import io.viewserver.adapters.jdbc.JdbcConnectionFactory;
import io.viewserver.core.Utils;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

public class H2ConnectionFactory extends JdbcConnectionFactory{

    String path;
    public H2ConnectionFactory(String username, String password, String path) {
        super(username, password);
        this.path = path;
    }

    @Override
    public DataSource getDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(String.format("jdbc:h2:%s;MV_STORE=FALSE;DATABASE_TO_UPPER=FALSE", Utils.replaceSystemTokens(path)));
        return dataSource;
    }
}
