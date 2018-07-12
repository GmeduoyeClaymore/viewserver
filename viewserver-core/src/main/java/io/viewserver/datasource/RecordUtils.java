package io.viewserver.datasource;

import io.viewserver.core.ExecutionContext;
import io.viewserver.operators.table.*;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

public class RecordUtils{

    private static final Logger logger = LoggerFactory.getLogger(RecordUtils.class);

    public static void addRecordToTableOperator(Observable<KeyedTable> operator, IRecord rec) {
        operator.subscribe(kt -> addRecordToTableOperator(kt,rec));
    }

    public static void addRecordToTableOperator(KeyedTable operator, IRecord rec) {
        if(!Thread.currentThread().getName().startsWith("reactor-")){
            logger.debug("Scheduling addition to operator {}", operator.getPath());
            operator.getExecutionContext().submit(() -> actualyAddRecord(operator,rec,true), 0);
            return;
        }
        actualyAddRecord(operator, rec,false);
    }

    public static void actualyAddRecord(KeyedTable operator, IRecord rec, boolean scheduled) {
        ExecutionContext.AssertUpdateThread();
        logger.info(String.format("{} Actually adding record rec to operator {} - %s",rec.getValue(operator.getTableKeyDefinition().getKeyName())),(scheduled ? "SCHEDULED - " : "") ,operator.getPath());
        TableKeyDefinition tableKeyDefinition = operator.getTableKeyDefinition();
        if(tableKeyDefinition == null ){
            throw new RuntimeException(String.format("Cannot map record as no key columns are defined on schema - %s", rec));
        }
        TableKey key = getTableKey(rec, tableKeyDefinition);
        ITableRowUpdater rowUpdater = new ITableRowUpdater() {
            public Object getValue(String columnName) {
                return rec.getValue(columnName);
            }
            @Override
            public void setValues(ITableRow row) {
                for (ColumnHolder holder : operator.getOutput().getSchema().getColumnHolders()) {
                    if(rec.hasValue(holder.getName())) {
                        ColumnHolderUtils.setValue(holder, row.getRowId(), rec.getValue(holder.getName()));
                    }
                }
            }
        };
        int row = operator.getRow(key);
        if(row == -1){
            operator.addRow(rowUpdater);
        }else{
            operator.updateRow(row, rowUpdater);
        }
        logger.debug("Finished adding record rec to operator " + operator.getPath());
    }

    public static TableKey getTableKey(IRecord rec, TableKeyDefinition tableKeyDefinition) {
        String[] elements = new String[tableKeyDefinition.size()];
        int counter = 0;
        for(String col : tableKeyDefinition.getKeys()){
            Object keyElement = rec.getValue(col);
            if(keyElement == null){
                throw new RuntimeException(String.format("Cannot map record as one of key elements %s is empty %s",col, rec));
            }
            elements[counter++] = keyElement + "";
        }
        return new TableKey(elements);
    }

}
