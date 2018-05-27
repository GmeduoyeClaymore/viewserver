package com.shotgun.viewserver.setup.datasource;


import io.viewserver.datasource.*;

import java.util.Arrays;


public class PhoneNumberDataSource {
    public static final String NAME = "phoneNumber";

    public static DataSource getDataSource() {
        return new DataSource()
                .withName(NAME)
                .withSchema(new SchemaConfig()
                                .withColumns(Arrays.asList(
                                        new Column("phoneNumber", ContentType.String),
                                        new Column("fromUserId",  ContentType.String),
                                        new Column("toUserId",  ContentType.String),
                                        new Column("version",  ContentType.Int),
                                        new Column("userPhoneNumber", ContentType.String),
                                        new Column("phoneNumberStatus", ContentType.String),
                                        new Column("assignedTime",  ContentType.DateTime)
                                ))
                                .withKeyColumns("phoneNumber")
                )
                .withOutput(DataSource.TABLE_NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
