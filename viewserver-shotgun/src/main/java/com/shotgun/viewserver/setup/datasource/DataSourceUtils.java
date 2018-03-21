package com.shotgun.viewserver.setup.datasource;

import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.adapters.firebase.FirebaseCsvDataAdapter;
import io.viewserver.datasource.IDataAdapter;

public class DataSourceUtils {
    public static IDataAdapter get(String configKey, String name, String dataPath){
        if("MOCK".equals(configKey)){
            return new CsvDataAdapter().withFileName(dataPath);
        }
        return new FirebaseCsvDataAdapter(configKey, name, dataPath);
    }
}
