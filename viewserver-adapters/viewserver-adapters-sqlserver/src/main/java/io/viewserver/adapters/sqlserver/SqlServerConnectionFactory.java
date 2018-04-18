package io.viewserver.adapters.sqlserver;

import io.viewserver.util.ViewServerException;
import net.sourceforge.jtds.jdbcx.JtdsDataSource;

import javax.sql.DataSource;

public class SqlServerConnectionFactory{

    private JtdsDataSource dataSource;
    private String server;
    private Integer port;
    private String instance;
    private String database;

    public SqlServerConnectionFactory(String server, Integer port, String instance, String database, String username, String password) {
        this.server = server;
        this.port = port;
        this.instance = instance;
        this.database = database;
    }
    protected DataSource createDataSource() {
        try {
            dataSource = new JtdsDataSource();
            dataSource.setServerName(server);

            if(this.instance != null) {
                dataSource.setInstance(instance);
            }else{
                dataSource.setPortNumber(port);
            }

            dataSource.setDatabaseName(database);

            //TODO - set best settings see here https://msdn.microsoft.com/en-us/library/ms378988(v=sql.110).aspx

            return dataSource;
        } catch (Throwable e) {
            throw new ViewServerException(e);
        }
    }
}
