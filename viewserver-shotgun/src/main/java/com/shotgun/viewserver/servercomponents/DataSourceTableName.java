package com.shotgun.viewserver.servercomponents;

import io.viewserver.core.Utils;

public class DataSourceTableName{
    private final String[] parts;
    private final String dataSourceName;
    private final String operatorName;
    private String path;

    public DataSourceTableName(String path) {
        this.path = path;
        this.parts = Utils.splitIgnoringEmpty(this.path,"/");
        if(this.parts.length != 3){
            throw new RuntimeException(path + " doesnt look like a table name. It should be in the format /datasources/%ds_name%/%operator_name%");
        }
        this.dataSourceName = this.parts[1];
        this.operatorName = this.parts[2];
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public String getOperatorName() {
        return operatorName;
    }
}
