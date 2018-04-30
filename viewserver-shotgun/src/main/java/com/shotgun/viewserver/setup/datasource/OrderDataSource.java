package com.shotgun.viewserver.setup.datasource;


import io.viewserver.Constants;
import io.viewserver.datasource.*;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.operators.projection.IProjectionConfig;

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
                        new Column("paymentMethodId", ContentType.String),
                        new Column("productId", ContentType.String),
                        new Column("orderContentTypeId", ContentType.Int),
                        new Column("orderDetails", ContentType.Json),
                        new Column("totalPrice", ContentType.Int)
                ))
                .withKeyColumns("orderId");

        return new DataSource()
                .withName(NAME)
                .withSchema(schema)
                .withOutput("projectionNode")
                .withNodes(
                        new JoinNode("productJoin")
                                .withLeftJoinColumns("productId")
                                .withRightJoinColumns("productId")
                                .withColumnPrefixes("", "product_")
                                .withAlwaysResolveNames()
                                .withConnection(DataSource.TABLE_NAME, Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(ProductDataSource.NAME), Constants.OUT, "right"),
                        new JoinNode("productCategoryJoin")
                                .withLeftJoinColumns("product_categoryId")
                                .withRightJoinColumns("categoryId")
                                .withColumnPrefixes("", "productCategory_")
                                .withAlwaysResolveNames()
                                .withConnection("productJoin", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(ProductCategoryDataSource.NAME), Constants.OUT, "right"),
                        new JoinNode("contentTypeJoin")
                                .withLeftJoinColumns("orderContentTypeId")
                                .withRightJoinColumns("contentTypeId")
                                .withColumnPrefixes("", "contentType_")
                                .withAlwaysResolveNames()
                                .withConnection("productCategoryJoin", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(ContentTypeDataSource.NAME), Constants.OUT, "right"),
                        new ProjectionNode("projectionNode")
                                .withMode(IProjectionConfig.ProjectionMode.Inclusionary)
                                .withProjectionColumns(new IProjectionConfig.ProjectionColumn("orderId"))
                                .withProjectionColumns(new IProjectionConfig.ProjectionColumn("created"))
                                .withProjectionColumns(new IProjectionConfig.ProjectionColumn("lastModified"))
                                .withProjectionColumns(new IProjectionConfig.ProjectionColumn("status"))
                                .withProjectionColumns(new IProjectionConfig.ProjectionColumn("requiredDate"))
                                .withProjectionColumns(new IProjectionConfig.ProjectionColumn("orderLocation"))
                                .withProjectionColumns(new IProjectionConfig.ProjectionColumn("userId"))
                                .withProjectionColumns(new IProjectionConfig.ProjectionColumn("assignedPartnerUserId"))
                                .withProjectionColumns(new IProjectionConfig.ProjectionColumn("paymentMethodId"))
                                .withProjectionColumns(new IProjectionConfig.ProjectionColumn("productId"))
                                .withProjectionColumns(new IProjectionConfig.ProjectionColumn("orderContentTypeId"))
                                .withProjectionColumns(new IProjectionConfig.ProjectionColumn("orderDetails"))
                                .withProjectionColumns(new IProjectionConfig.ProjectionColumn("totalPrice"))
                                .withProjectionColumns(new IProjectionConfig.ProjectionColumn("productCategory_path","path"))
                                .withProjectionColumns(new IProjectionConfig.ProjectionColumn("contentType_rootProductCategory","contentTypeRootProductCategory"))
                                .withConnection("contentTypeJoin")

                )
                .withDimensions(Arrays.asList(
                        new Dimension("dimension_productId","productId", Cardinality.Byte, ContentType.String),
                        new Dimension("dimension_orderId","orderId", Cardinality.Byte, ContentType.String),
                        new Dimension("dimension_customerUserId","userId", Cardinality.Int, ContentType.String, true),
                        new Dimension("dimension_assignedPartnerUserId","assignedPartnerUserId", Cardinality.Int, ContentType.String),
                        new Dimension("dimension_status", "status",Cardinality.Int, ContentType.String),
                        new Dimension("dimension_contentTypeId", "orderContentTypeId",Cardinality.Int, ContentType.Int)
                ))
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
