package com.shotgun.viewserver.setup.datasource;


import io.viewserver.datasource.*;

import java.util.Arrays;


public class MessagesDataSource {
    public static final String NAME = "messages";

    public static DataSource getDataSource() {
        return new DataSource()
                .withName(NAME)
                .withSchema(new SchemaConfig()
                                .withColumns(Arrays.asList(
                                        new Column("messageId", ContentType.String),
                                        new Column("fromUserId",  ContentType.String),
                                        new Column("toUserId", ContentType.String),
                                        new Column("message", ContentType.Json)
                                ))
                                .withKeyColumns("messageId")
                )
                .withOutput(DataSource.TABLE_NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
