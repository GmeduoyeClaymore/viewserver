package com.shotgun.viewserver.setup.datasource;


import io.viewserver.Constants;
import io.viewserver.datasource.*;
import io.viewserver.execution.nodes.CalcColNode;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.operators.calccol.CalcColOperator;
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
                        new JoinNode("customerJoin")
                                .withLeftJoinColumns("userId")
                                .withRightJoinColumns("userId")
                                .withColumnPrefixes("", "customer_")
                                .withAlwaysResolveNames()
                                .withConnection(DataSource.TABLE_NAME, Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(UserDataSource.NAME), Constants.OUT, "right"),
                        new JoinNode("partnerJoin")
                                .withLeftJoinColumns("assignedPartnerUserId")
                                .withLeftJoinOuter()
                                .withRightJoinColumns("userId")
                                .withColumnPrefixes("", "partner_")
                                .withAlwaysResolveNames()
                                .withConnection("customerJoin", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(UserDataSource.NAME), Constants.OUT, "right"),
                        new JoinNode("ratingJoin")
                                .withLeftJoinColumns("orderId", "assignedPartnerUserId")
                                .withLeftJoinOuter()
                                .withRightJoinColumns("orderId", "userId")
                                .withConnection("partnerJoin", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(RatingDataSource.NAME), Constants.OUT, "right"),
                        new JoinNode("productJoin")
                                .withLeftJoinColumns("productId")
                                .withRightJoinColumns("productId")
                                .withColumnPrefixes("", "product_")
                                .withAlwaysResolveNames()
                                .withConnection("ratingJoin", Constants.OUT, "left")
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
                                .withProjectionColumns(
                                        new IProjectionConfig.ProjectionColumn("orderId"),
                                        new IProjectionConfig.ProjectionColumn("created"),
                                        new IProjectionConfig.ProjectionColumn("lastModified"),
                                        new IProjectionConfig.ProjectionColumn("status"),
                                        new IProjectionConfig.ProjectionColumn("requiredDate"),
                                        new IProjectionConfig.ProjectionColumn("orderLocation"),
                                        new IProjectionConfig.ProjectionColumn("userId"),
                                        new IProjectionConfig.ProjectionColumn("customer_firstName"),
                                        new IProjectionConfig.ProjectionColumn("customer_lastName"),
                                        new IProjectionConfig.ProjectionColumn("customer_ratingAvg"),
                                        new IProjectionConfig.ProjectionColumn("assignedPartnerUserId"),
                                        new IProjectionConfig.ProjectionColumn("partner_latitude"),
                                        new IProjectionConfig.ProjectionColumn("partner_longitude"),
                                        new IProjectionConfig.ProjectionColumn("partner_firstName"),
                                        new IProjectionConfig.ProjectionColumn("partner_lastName"),
                                        new IProjectionConfig.ProjectionColumn("partner_email"),
                                        new IProjectionConfig.ProjectionColumn("partner_imageUrl"),
                                        new IProjectionConfig.ProjectionColumn("partner_online"),
                                        new IProjectionConfig.ProjectionColumn("partner_range"),
                                        new IProjectionConfig.ProjectionColumn("partner_userStatus"),
                                        new IProjectionConfig.ProjectionColumn("partner_statusMessage"),
                                        new IProjectionConfig.ProjectionColumn("partner_ratingAvg"),
                                        new IProjectionConfig.ProjectionColumn("paymentMethodId"),
                                        new IProjectionConfig.ProjectionColumn("productId"),
                                        new IProjectionConfig.ProjectionColumn("orderContentTypeId"),
                                        new IProjectionConfig.ProjectionColumn("orderDetails"),
                                        new IProjectionConfig.ProjectionColumn("totalPrice"),
                                        new IProjectionConfig.ProjectionColumn("contentType_dimension_contentTypeId", "dimension_contentTypeId"),
                                        new IProjectionConfig.ProjectionColumn("product_name", "productName"),
                                        new IProjectionConfig.ProjectionColumn("productCategory_path", "path"),
                                        new IProjectionConfig.ProjectionColumn("contentType_rootProductCategory", "contentTypeRootProductCategory"))
                                .withConnection("contentTypeJoin")

                )
                .withDimensions(Arrays.asList(
                        new Dimension("dimension_productId","productId", Cardinality.Int, ContentType.String, true),
                        new Dimension("dimension_orderId","orderId", Cardinality.Int, ContentType.String, true),
                        new Dimension("dimension_customerUserId","userId", Cardinality.Int, ContentType.String, true),
                        new Dimension("dimension_assignedPartnerUserId","assignedPartnerUserId", Cardinality.Int, ContentType.String),
                        new Dimension("dimension_status", "status",Cardinality.Int, ContentType.String,true),
                        new Dimension("dimension_contentTypeId", "orderContentTypeId",Cardinality.Int, ContentType.Int,true).withImported()
                ))
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
