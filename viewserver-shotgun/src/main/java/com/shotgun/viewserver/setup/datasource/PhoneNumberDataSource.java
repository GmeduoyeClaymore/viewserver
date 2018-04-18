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
                                        new Column("phoneNumber", ColumnType.String),
                                        new Column("orderId",  ColumnType.String),
                                        new Column("userPhoneNumber", ColumnType.String),
                                        new Column("phoneNumberStatus", ColumnType.String),
                                        new Column("assignedTime",  ColumnType.DateTime)
                                ))
                                .withKeyColumns("phoneNumber")
                )
                .withOutput(NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
