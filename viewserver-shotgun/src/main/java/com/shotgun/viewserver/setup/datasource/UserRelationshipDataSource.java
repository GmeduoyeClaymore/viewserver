package com.shotgun.viewserver.setup.datasource;

import com.shotgun.viewserver.IShotgunViewServerConfiguration;
import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.adapters.firebase.FirebaseCsvDataAdapter;
import io.viewserver.datasource.*;

import java.util.Arrays;

public class UserRelationshipDataSource {
    public static final String NAME = "userRelationship";

    public static DataSource getDataSource(IShotgunViewServerConfiguration shotgunConfiguration) {
        return new DataSource()
                .withName(NAME)
                .withDataLoader(DataSourceUtils.getDataLoader(shotgunConfiguration, NAME, "data/userRelationship.csv"))
                .withSchema(new Schema()
                        .withColumns(Arrays.asList(
                                new Column("relationshipId", "relationshipId", ColumnType.String),
                                new Column("fromUserId", "fromUserId", ColumnType.String),
                                new Column("toUserId", "toUserId", ColumnType.String),
                                new Column("relationshipStatus", "relationshipStatus", ColumnType.String),
                                new Column("relationshipType", "relationshipType", ColumnType.String)
                        ))
                        .withKeyColumns("relationshipId")
                )
                .withDimensions(Arrays.asList(
                        new Dimension("relationshipType", Cardinality.Int, ColumnType.String)))
                .withOutput(NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
