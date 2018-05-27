package com.shotgun.viewserver.setup.datasource;


import io.viewserver.datasource.*;

import java.util.Arrays;

/**
 *
 * int totalPrice,
 int chargePercentage,
 String paymentId,
 String customerId,
 String accountId,
 String description
 * Created by bennett on 26/09/17.
 */
public class PaymentDataSource {
    public static final String NAME = "payments";

    public static DataSource getDataSource() {
        SchemaConfig schema = new SchemaConfig()
                .withColumns(Arrays.asList(
                        new Column("totalPrice", ContentType.Int),
                        new Column("version", ContentType.Int),
                        new Column("chargePercentage", ContentType.Int),
                        new Column("chargeId", ContentType.String),
                        new Column("paymentId", ContentType.String),
                        new Column("paymentMethodId", ContentType.String),
                        new Column("paidFromUserId", ContentType.String),
                        new Column("paidToUserId", ContentType.String),
                        new Column("accountId", ContentType.String),
                        new Column("created", ContentType.String)
                ))
                .withKeyColumns("paymentId");

        return new DataSource()
                .withName(NAME)
                .withSchema(schema)
                .withDimensions(Arrays.asList(
                        new Dimension("dimension_paidFromUserId","paidFromUserId", Cardinality.Byte, ContentType.String),
                        new Dimension("dimension_paidToUserId","paidToUserId", Cardinality.Byte, ContentType.String)
                ))
                .withOutput(DataSource.TABLE_NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
