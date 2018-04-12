package com.shotgun.viewserver.setup.datasource;


import com.shotgun.viewserver.IShotgunViewServerConfiguration;
import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.adapters.firebase.FirebaseCsvDataAdapter;
import io.viewserver.datasource.*;

import java.util.Arrays;


public class PhoneNumberDataSource {
    public static final String NAME = "phoneNumber";

    public static DataSource getDataSource(IShotgunViewServerConfiguration shotgunConfiguration) {
        return new DataSource()
                .withName(NAME)
                .withDataLoader(
                        new DataLoader(
                                NAME,
                                DataSourceUtils.getDataAdapter(shotgunConfiguration, NAME, "data/phoneNumber.csv"),
                                null
                        )
                )
                .withSchema(new Schema()
                                .withColumns(Arrays.asList(
                                        new Column("phoneNumber", "phoneNumber", ColumnType.String),
                                        new Column("orderId", "orderId", ColumnType.String),
                                        new Column("userPhoneNumber", "userPhoneNumber", ColumnType.String),
                                        new Column("phoneNumberStatus", "phoneNumberStatus", ColumnType.String),
                                        new Column("assignedTime", "assignedTime", ColumnType.DateTime)
                                ))
                                .withKeyColumns("phoneNumber")
                )
                .withOutput(NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
