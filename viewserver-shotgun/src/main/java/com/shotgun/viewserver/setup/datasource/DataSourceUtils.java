package com.shotgun.viewserver.setup.datasource;

import com.shotgun.viewserver.IShotgunViewServerConfiguration;
import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.adapters.firebase.FirebaseCsvDataAdapter;
import io.viewserver.adapters.firebase.FirebaseDataAdapter;
import io.viewserver.datasource.IDataAdapter;
import io.viewserver.datasource.IDataLoader;

public class DataSourceUtils {
    //TODO - add flag to say whether to reset data
    public static IDataLoader getDataLoader(IShotgunViewServerConfiguration shotgunConfiguration, String name, String dataPath) {
        IDataAdapter dataAdapter;

        if (!shotgunConfiguration.isTest() && !shotgunConfiguration.isMock()) {
            dataAdapter = new FirebaseDataAdapter(shotgunConfiguration.getFirebaseKeyPath(), name);
        } else {
            dataAdapter = getCsvDataAdapter(shotgunConfiguration, name, dataPath, true);
        }

        return new DataLoader(name, dataAdapter, null);
    }

    public static IDataAdapter getCsvDataAdapter(IShotgunViewServerConfiguration shotgunConfiguration, String name, String dataPath, boolean resetData) {
        return shotgunConfiguration.isMock() ? new CsvDataAdapter().withFileName(dataPath) : new FirebaseCsvDataAdapter(shotgunConfiguration.getFirebaseKeyPath(), name, dataPath, resetData);
    }
}
