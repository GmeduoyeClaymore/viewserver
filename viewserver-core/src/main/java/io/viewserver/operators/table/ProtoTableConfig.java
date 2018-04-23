package io.viewserver.operators.table;

import io.viewserver.datasource.Column;

import java.util.List;
import java.util.stream.Collectors;

public class ProtoTableConfig implements ISchemaConfig{

    private List<String> keycolumns;
    private List<Column> columns;

    public ProtoTableConfig(io.viewserver.messages.config.ISchemaConfig schemaConfig){
        List<Column> columns = schemaConfig.getColumns();
        this.columns = columns.stream().map(c -> new Column(c.getName(),c.getType())).collect(Collectors.toList());
        keycolumns = schemaConfig.getKeyColumns();
    }


    @Override
    public TableKeyDefinition getTableKeyDefinition() {
        return new TableKeyDefinition(keycolumns.toArray(new String[keycolumns.size()]));
    }

    @Override
    public List<Column> getColumns() {
        return columns;
    }

    @Override
    public List<String> getKeyColumns() {
        return keycolumns;
    }


}
