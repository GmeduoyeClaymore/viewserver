package com.shotgun.viewserver.setup.datasource;


import io.viewserver.datasource.*;

import java.util.Arrays;
/**
 * Created by bennett on 26/09/17.
 */
public class OrderDataSource {
    public static final String NAME = "order";

    public static DataSource getDataSource() {
        SchemaConfig schema = new SchemaConfig()
                .withColumns(Arrays.asList(
                        new Column("orderId", ContentType.String),
                        new Column("created", ContentType.DateTime),
                        new Column("lastModified", ContentType.DateTime),
                        new Column("status", ContentType.String),
                        new Column("requiredDate", ContentType.DateTime),
                        new Column("orderLocation", ContentType.Json),
                        new Column("userId", ContentType.String),
                        new Column("assignedPartnerUserId", ContentType.String),
                        new Column("paymentId", ContentType.String),
                        new Column("orderContentTypeId", ContentType.Int),
                        new Column("orderDetails", ContentType.Json),
                        new Column("totalPrice", ContentType.Int)
                ))
                .withKeyColumns("orderId");

        return new DataSource()
                .withName(NAME)
                .withSchema(schema)
                .withOutput(DataSource.TABLE_NAME)
                .withDimensions(Arrays.asList(
                        new Dimension("dimension_orderId","orderId", Cardinality.Byte, ContentType.String),
                        new Dimension("dimension_customerUserId","userId", Cardinality.Int, ContentType.String),
                        new Dimension("dimension_assignedPartnerUserId","assignedPartnerUserId", Cardinality.Int, ContentType.String),
                        new Dimension("dimension_status", "status",Cardinality.Int, ContentType.String),
                        new Dimension("dimension_contentTypeId", "orderContentTypeId",Cardinality.Int, ContentType.Int)
                ))
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
