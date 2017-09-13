package com.shotgun.viewserver.setup.datasource;


import io.viewserver.Constants;
import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.datasource.*;
import io.viewserver.distribution.RoundRobinStripingStrategy;
import io.viewserver.execution.nodes.CalcColNode;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.operators.calccol.CalcColOperator;

import java.util.Arrays;

/**
 * Created by paulg on 31/10/2014.
 */

public class CsvDataSource {
    public static final int START_DATE_OFFSET = 0;

    public static DataSource getDataSource() {
        DataSource dataSource = new DataSource();

        Schema schema = new Schema()
                .withColumns(Arrays.asList(
                        new Column("timeStamp", "timestamp", ColumnType.DateTime),
                        new Column("dv01", "DV01", ColumnType.Int),
                        new Column("notional", "qty", ColumnType.Int),
                        new Column("tenYrEq", "TenYrEquivalent", ColumnType.Int),
                        new Column("fiveYrEq", "FiveYrEquivalent", ColumnType.Int),

                        new Column("productName", "ProductName", ColumnType.String),
                        new Column("product", "product", ColumnType.String),
                        new Column("productSubclass", "Product-subclass", ColumnType.String),
                        new Column("productClass3", "ProductClass3", ColumnType.String),
                        new Column("productClass", "product-class", ColumnType.String),
                        new Column("region", "Region", ColumnType.String),
                        new Column("countryOfRisk", "Country of Risk", ColumnType.String),
                        new Column("venue", "venue", ColumnType.String),
                        new Column("tenor", "Tenor", ColumnType.String),
                        new Column("industry", "Industry", ColumnType.String),
                        new Column("ticker", "Ticker", ColumnType.String),
                        new Column("ratings", "Ratings", ColumnType.String),
                        new Column("issuer", "Issuer", ColumnType.String),
                        new Column("subordination", "Subordination", ColumnType.String),

                        new Column("salesRegion", "SalesRegion", ColumnType.String),
                        new Column("salesPerson", "Salesperson", ColumnType.String),

                        new Column("clientId", "clientId", ColumnType.Int),
                        new Column("client", "client", ColumnType.String),
                        new Column("clientTrader", "clientTrader", ColumnType.String),
                        new Column("clientTier", "clientTier", ColumnType.Byte),
                        new Column("clientType", "ClientType", ColumnType.String),
                        new Column("clientDomicile", "ClientDomicile", ColumnType.String),
                       /* new Column("clientRank", "clientRank", ColumnType.Short),*/

                        new Column("book", "Book", ColumnType.String),
                        new Column("currency", "Currency", ColumnType.String),
                        new Column("desk", "Desk", ColumnType.String),
                        new Column("dealer", "Dealer", ColumnType.String),
                        new Column("trader", "Trader", ColumnType.String),

                        new Column("macaulayDuration", "McCauleyDuration", ColumnType.Double),
                        new Column("myPrice", "myPrc", ColumnType.Double),
                        new Column("tradePrice", "tradeprice", ColumnType.Double),
                        new Column("myLatency", "MyLatency", ColumnType.Int),
                        new Column("tradeLatency", "TradeLatency", ColumnType.Int),
                        new Column("noQuote", "NoQuote", ColumnType.NullableBool),
                        new Column("nextBestPrice", "nextbestprice", ColumnType.Double),
                        new Column("nextBest", "NextBest", ColumnType.NullableBool),
                        new Column("numCompetitors", "NumCompetitors", ColumnType.Int),
                        new Column("yield", "yield", ColumnType.Double),

                        new Column("mid7200", "mid_2hr", ColumnType.Double),
                        new Column("mid3600", "mid_1hr", ColumnType.Double),
                        new Column("mid1800", "mid_30min", ColumnType.Double),
                        new Column("mid300", "mid_5min", ColumnType.Double),
                        new Column("mid30", "mid_30sec", ColumnType.Double),
                        new Column("mid", "mid", ColumnType.Double),

                        new Column("midPre30", "midpre30sec", ColumnType.Double),
                        new Column("midPre300", "mid_pre5min", ColumnType.Double),
                        new Column("midPre1800", "mid_pre30min", ColumnType.Double),
                        new Column("midPre3600", "mid_pre1hr", ColumnType.Double),
                        new Column("midPre7200", "mid_pre2Hr", ColumnType.Double),

                        new Column("visibleFlow", "VisibleFlow", ColumnType.NullableBool),
                        new Column("tiedAndWon", "TiedAndWon", ColumnType.NullableBool),
                        new Column("tradedAway", "TradedAway", ColumnType.NullableBool),
                        new Column("tiedAndLost", "TiedAndLost", ColumnType.NullableBool),
                        new Column("traded", "Traded", ColumnType.NullableBool),
                        new Column("tied", "Tied", ColumnType.NullableBool),
                        new Column("click2Trade", "Click2Trade", ColumnType.NullableBool),

                        new Column("modDuration", "ModDuration", ColumnType.Double)
                ));

        CsvDataAdapter dataAdapter = new CsvDataAdapter();
        dataAdapter.setFileName("data/negotiations.csv");
//        dataAdapter.setMultiple(30);
        dataSource.withName("negotiations")
                .withDataLoader(
                        new DataLoader(
                                "negotiations",
                                dataAdapter,
                                null
                        )
                )
                .withDimensions(Arrays.asList(

                        //Product Dimensions
                        new Dimension("productName", Cardinality.Int, schema.getColumn("productName"))
                                .setLabel("Product Name").setPlural("Product Names").setGroup("Product"),
                        new Dimension("product", Cardinality.Int, schema.getColumn("product"))
                                .setLabel("Product").setPlural("Products").setGroup("Product"),
                        new Dimension("productClass", Cardinality.Byte, schema.getColumn("productClass"))
                                .setLabel("Product Class").setPlural("Product Classes").setGroup("Product"),
                        new Dimension("productSubclass", Cardinality.Byte, schema.getColumn("productSubclass"))
                                .setLabel("Product Sub Class").setPlural("Product Sub Classes").setGroup("Product"),
                        new Dimension("productClass3", Cardinality.Byte, schema.getColumn("productClass3"))
                                .setLabel("Product Type").setPlural("Product Types").setGroup("Product"),
                        new Dimension("book", Cardinality.Int, schema.getColumn("book"))
                                .setLabel("Book").setPlural("Books").setGroup("Product"),
                        new Dimension("currency", Cardinality.Byte, schema.getColumn("currency"))
                                .setLabel("Currency").setPlural("Currencies").setGroup("Product"),
                        new Dimension("region", Cardinality.Byte, schema.getColumn("region"))
                                .setLabel("Region").setPlural("Regions").setGroup("Product"),
                        new Dimension("countryOfRisk", Cardinality.Int, schema.getColumn("countryOfRisk"))
                                .setLabel("Country Of Risk").setPlural("Countries or Risk").setGroup("Product"),
                        new Dimension("tenor", Cardinality.Int, schema.getColumn("tenor"))
                                .setLabel("Tenor").setPlural("Tenors").setGroup("Product"),
                        new Dimension("industry", Cardinality.Int, schema.getColumn("industry"))
                                .setLabel("Industry").setPlural("Industries").setGroup("Product"),
                        new Dimension("ticker", Cardinality.Int, schema.getColumn("ticker"))
                                .setLabel("Ticker").setPlural("Tickers").setGroup("Product"),
                        new Dimension("ratings", Cardinality.Int, schema.getColumn("ratings"))
                                .setLabel("Rating").setPlural("Ratings").setGroup("Product"),
                        new Dimension("issuer", Cardinality.Int, schema.getColumn("issuer"))
                                .setLabel("Issuer").setPlural("Issuers").setGroup("Product"),
                        new Dimension("subordination", Cardinality.Int, schema.getColumn("subordination"))
                                .setLabel("Subordination").setPlural("Subordinations").setGroup("Product"),
                        new Dimension("desk", Cardinality.Int, schema.getColumn("desk"))
                                .setLabel("Desk").setPlural("Desks").setGroup("Product"),

                        //Sales Dimensions
                        new Dimension("salesRegion", Cardinality.Byte, schema.getColumn("salesRegion"))
                                .setLabel("Sales Region").setPlural("Sales Regions").setGroup("Sales & Trading"),
                        new Dimension("salesPerson", Cardinality.Int, schema.getColumn("salesPerson"))
                                .setLabel("Sales Person").setPlural("Sales People").setGroup("Sales & Trading"),
                        new Dimension("venue", Cardinality.Int, schema.getColumn("venue"))
                                .setLabel("Venue").setPlural("Venues").setGroup("Sales & Trading"),
                        new Dimension("trader", Cardinality.Int, schema.getColumn("trader"))
                                .setLabel("Trader").setPlural("Traders").setGroup("Sales & Trading"),

                        //Client Dimensions
                        new Dimension("client", Cardinality.Int, schema.getColumn("client"))
                                .setLabel("Client Name").setPlural("Client Names").setGroup("Client"),
                        new Dimension("clientTier", Cardinality.Byte, schema.getColumn("clientTier"))
                                .setLabel("Client Tier").setPlural("Client Tiers").setGroup("Client"),
                        new Dimension("clientTrader", Cardinality.Int, schema.getColumn("clientTrader"))
                                .setLabel("Client Trader").setPlural("Client Traders").setGroup("Client"),
                        new Dimension("clientType", Cardinality.Byte, schema.getColumn("clientType"))
                                .setLabel("Client Type").setPlural("Client Types").setGroup("Client"),
                        new Dimension("clientDomicile", Cardinality.Byte, schema.getColumn("clientDomicile"))
                                .setLabel("Client Domicile").setPlural("Client Domiciles").setGroup("Client"),

                        //Negotiation Dimensions
                        new Dimension("buy", Cardinality.Boolean, schema.getColumn("buy"))
                                .setLabel("Dealer Buys").setAggregator(false).setGroup("Negotiation"),
                        new Dimension("click2Trade", Cardinality.Byte, schema.getColumn("click2Trade"))
                                .setLabel("Click to Trade").setAggregator(false).setGroup("Negotiation"),
                        new Dimension("noQuote", Cardinality.Byte, schema.getColumn("noQuote"))
                                .setLabel("Quoted").setAggregator(false).setGroup("Negotiation"),
                        new Dimension("traded", Cardinality.Byte, schema.getColumn("traded"))
                                .setLabel("Traded").setAggregator(false).setGroup("Negotiation"),
                        new Dimension("tiedAndLost", Cardinality.Byte, schema.getColumn("tiedAndLost"))
                                .setLabel("Tied and Lost").setAggregator(false).setGroup("Negotiation"),
                        new Dimension("tied", Cardinality.Byte, schema.getColumn("tied"))
                                .setLabel("Tied").setAggregator(false).setGroup("Negotiation"),
                        new Dimension("tiedAndWon", Cardinality.Byte, schema.getColumn("tiedAndWon"))
                                .setLabel("Tied and Won").setAggregator(false).setGroup("Negotiation"),
                        new Dimension("tradedAway", Cardinality.Byte, schema.getColumn("tradedAway"))
                                .setLabel("Traded Away").setAggregator(false).setGroup("Negotiation"),
                        new Dimension("tradedInCompetition", Cardinality.Boolean, schema.getColumn("tradedInCompetition"))
                                .setLabel("Traded In Competition").setAggregator(false).setGroup("Negotiation"),
                        new Dimension("nextBest", Cardinality.Byte, schema.getColumn("nextBest"))
                                .setLabel("We Were Next Best").setAggregator(false).setGroup("Negotiation")
                ))
                .withSchema(schema)
                .withCalculatedColumns(
                        new CalculatedColumn("tickets", ColumnType.Int, "if(notional > 1, 1, -1)"),
                        //  new CalculatedColumn("tied", ColumnType.Bool, "(traded || tradedAway) && (tiedAndWon || tiedAndLost)"),
                        new CalculatedColumn("buy", ColumnType.Bool, "notional > 0"),
                        new CalculatedColumn("revenueCredits", ColumnType.Double, "if(traded, (abs(tradePrice - mid) / 2.0f) * abs(notional), 0)"),
                        new CalculatedColumn("tradedInCompetition", ColumnType.Bool, "numCompetitors > 0"),
                        new CalculatedColumn("rate_USD", ColumnType.Float, "1.0f")
                )
                .withDistributionMode(DistributionMode.Striped)
                .withStripingStrategy(new RoundRobinStripingStrategy())
                .withNodes(
                        new CalcColNode("negotiationsDayCalCol")
                                .withCalculations(new CalcColOperator.CalculatedColumn("day", "businessDay(timeStamp, false) - " + START_DATE_OFFSET))
                                .withConnection(dataSource.getName()),
                        new JoinNode("fxJoin")
                                .withLeftJoinColumns("day")
                                .withRightJoinColumns("day")
                                .withConnection("negotiationsDayCalCol", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getOperatorPath(FxRatesDataSource.NAME, "fxRatesFilter"), Constants.OUT, "right")
//                        new GroupByNode("#customers")
//                                .withGroupByColumns("clientId", "client")
//                                .withConnection(dataSource.getName()),
//                        new JoinNode("#btyd_join")
//                                .withLeftJoinColumns("cust")
//                                .withRightJoinColumns("clientId")
//                                .withConnection("btyd", Constants.OUT, "left")
//                                .withConnection("customers", Constants.OUT, "right")
                )
                .withOutput("fxJoin")
//                .withOutput(dataSource.getName())
                .withOptions(DataSourceOption.IsReportSource);


        return dataSource;
    }
}
