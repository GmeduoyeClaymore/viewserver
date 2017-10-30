package com.shotgun.viewserver.setup.datasource;


import io.viewserver.Constants;
import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.datasource.*;
import io.viewserver.execution.nodes.CalcColNode;
import io.viewserver.execution.nodes.FilterNode;
import io.viewserver.execution.nodes.GroupByNode;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.operators.calccol.CalcColOperator;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class OrderItemsDataSource {
        public static final String NAME = "orderItem";

        public static DataSource getDataSource() {
                CsvDataAdapter dataAdapter = new CsvDataAdapter();
                dataAdapter.setFileName("data/orderItem.csv");
                return new DataSource()
                        .withName(NAME)
                        .withDataLoader(
                                new DataLoader(
                                        NAME,
                                        dataAdapter,
                                        null
                                )
                        )
                        .withSchema(new Schema()
                                        .withColumns(Arrays.asList(
                                                new Column("orderId", "orderId", ColumnType.String),
                                                new Column("customerId", "customerId", ColumnType.String),
                                                new Column("productId", "productId", ColumnType.String),
                                                new Column("quantity", "quantity", ColumnType.Int)
                                        ))
                        ).withNodes(
                                new JoinNode("productJoin")
                                        .withLeftJoinColumns("productId")
                                        .withRightJoinColumns("productId")
                                        .withConnection(NAME, Constants.OUT, "left")
                                        .withConnection(IDataSourceRegistry.getOperatorPath(ProductDataSource.NAME, ProductDataSource.NAME), Constants.OUT, "right"),
                                new CalcColNode("totalPriceCalcCol")
                                        .withCalculations(new CalcColOperator.CalculatedColumn("totalPrice", "quantity * price"))
                                        .withConnection("productJoin"),
                                new FilterNode("cartItems")
                                        .withExpression("orderId == null")
                                        .withConnection("totalPriceCalcCol")
                        )
                .withOutput("cartItems")
                .withOptions(DataSourceOption.IsReportSource);
        }
}
