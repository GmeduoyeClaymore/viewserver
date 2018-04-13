package com.shotgun.viewserver.setup.datasource;

import com.shotgun.viewserver.IShotgunViewServerConfiguration;
import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.common.EmptyTableDataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.adapters.firebase.FirebaseCsvDataAdapter;
import io.viewserver.adapters.firebase.FirebaseDataAdapter;
import io.viewserver.datasource.IDataAdapter;
import io.viewserver.datasource.IDataLoader;

public class DataSourceUtils {
    public static IDataLoader getDataLoader(IShotgunViewServerConfiguration shotgunConfiguration, String name, String dataPath) {
        IDataAdapter dataAdapter;

        if (!shotgunConfiguration.isTest() && !shotgunConfiguration.isMock()) {
            dataAdapter = new FirebaseDataAdapter(shotgunConfiguration.getFirebaseKeyPath(), name);
        } else {
            dataAdapter = getDataAdapter(shotgunConfiguration, name, dataPath);
        }

        return new DataLoader(name, dataAdapter, null);
    }

    public static IDataAdapter getDataAdapter(IShotgunViewServerConfiguration shotgunConfiguration, String name, String dataPath) {
        return shotgunConfiguration.isMock() ? new CsvDataAdapter().withFileName(dataPath) : new FirebaseCsvDataAdapter(shotgunConfiguration.getFirebaseKeyPath(), name, dataPath);
    }
}
