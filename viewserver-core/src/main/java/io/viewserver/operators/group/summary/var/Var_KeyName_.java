// :_KeyName_=Byte,_KeyType_=byte;_KeyName_=Short,_KeyType_=short;_KeyName_=Int,_KeyType_=int;_KeyName_=Long,_KeyType_=long;_KeyName_=Float,_KeyType_=float;_KeyName_=Double,_KeyType_=double

/*
 * Copyright 2016 Claymore Minds Limited and Niche Solutions (UK) Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.viewserver.operators.group.summary.var;

import io.viewserver.core._KeyType_;
import io.viewserver.operators.group.ISummary;
import io.viewserver.operators.group.summary.ISummaryContext;
import io.viewserver.schema.column.*;
import gnu.trove.list.array.TDoubleArrayList;

/**
 * Created by nickc on 02/10/2014.
 */
public class Var_KeyName_ implements ISummary {
    private final String name;
    private final String column;
    private String countColumn;
    private ColumnHolder_KeyName_ columnHolder;
    private ColumnHolder summaryColumn;
    private ISummaryContext context;
    private TDoubleArrayList sums = new TDoubleArrayList();
    private TDoubleArrayList sumOfSquares = new TDoubleArrayList();
    private ColumnHolderInt countColumnHolder;

    public Var_KeyName_(String name, String column, String countColumn) {
        this.name = name;
        this.column = column;
        this.countColumn = countColumn;
    }

    @Override
    public void initialise(ISummaryContext context) {
        this.context = context;
        this.summaryColumn = ColumnHolderUtils.createColumnHolder(name, getType());
        context.getTableStorage().initialiseColumn(summaryColumn);
        context.setResultColumn(summaryColumn);
        columnHolder = (ColumnHolder_KeyName_) context.getInboundSchema().getColumnHolder(column);
        if (countColumn != null) {
            countColumnHolder = (ColumnHolderInt)context.getOutboundSchema().getColumnHolder(countColumn);
        } else {
            countColumnHolder = context.getCountHolder();
        }
        ((IWritableColumn)columnHolder.getColumn()).storePreviousValues();
    }

    @Override
    public void onGroupAdd(int groupId) {
    }

    @Override
    public void onGroupRemove(int groupId) {
        sums.setQuick(groupId, 0);
        sumOfSquares.setQuick(groupId, 0);
    }

    @Override
    public void onGroupEnter(int groupId, int rowId) {
        _KeyType_ value = columnHolder.get_KeyName_(rowId);

        double newVariance = getUpdatedValue(groupId, value, (_KeyType_)0);
        ((IWritableColumnDouble)summaryColumn.getColumn()).setDouble(groupId, newVariance);
        context.markDirty(groupId);
    }

    @Override
    public void onGroupLeave(int groupId, int rowId) {
        _KeyType_ value = columnHolder.getPrevious_KeyName_(rowId);

        double newVariance = getUpdatedValue(groupId, (_KeyType_)0, value);
        ((IWritableColumnDouble)summaryColumn.getColumn()).setDouble(groupId, newVariance);
        context.markDirty(groupId);
    }

    @Override
    public void onRowUpdate(int groupId, int rowId) {
        _KeyType_ newValue = columnHolder.get_KeyName_(rowId);
        _KeyType_ oldValue = columnHolder.getPrevious_KeyName_(rowId);
        if (newValue != oldValue) {
            double newVariance = getUpdatedValue(groupId, newValue, oldValue);
            ((IWritableColumnDouble)summaryColumn.getColumn()).setDouble(groupId, newVariance);
            context.markDirty(groupId);
        }
    }

    @Override
    public void onAfterCommit() {

    }

    protected double getUpdatedValue(int groupId, _KeyType_ newValue, _KeyType_ oldValue) {
        return getUpdatedVariance(groupId, newValue, oldValue);
    }

    protected double getUpdatedVariance(int groupId, _KeyType_ newValue, _KeyType_ oldValue) {
        int count = countColumnHolder.getInt(groupId);

        double oldSum = sums.size() <= groupId ? 0 : sums.getQuick(groupId);
        double newSum = oldSum - oldValue + newValue;

        if(sums.size() <= groupId) {
            sums.add(newSum);
        }else {
            sums.setQuick(groupId, newSum);
        }

        double oldSumOfSquares = sumOfSquares.size() <= groupId ? 0 : sumOfSquares.getQuick(groupId);
        double newSumOfSquares = oldSumOfSquares - Math.pow(oldValue, 2) + Math.pow(newValue, 2);

        if(sumOfSquares.size() <= groupId) {
            sumOfSquares.add(newSumOfSquares);
        }else {
            sumOfSquares.setQuick(groupId, newSumOfSquares);
        }

        return (newSumOfSquares / (double)count) - Math.pow(newSum / (double)count, 2);
    }

    @Override
    public boolean hasChanges(IRowFlags rowFlags) {
        return rowFlags.isDirty(columnHolder.getColumnId());
    }

    @Override
    public void reset() {
        ((IWritableColumnDouble)summaryColumn.getColumn()).resetAll();
        sums.clear();
        sumOfSquares.clear();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean supportsPreviousValues() {
        return columnHolder.supportsPreviousValues();
    }

    @Override
    public ColumnType getType() {
        return ColumnType.Double;
    }
}
