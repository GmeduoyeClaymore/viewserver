// :_KeyName_=Byte,_KeyType_=byte,_SumName_=Byte,_SumType_=byte;_KeyName_=Byte,_KeyType_=byte,_SumName_=Short,_SumType_=short;_KeyName_=Byte,_KeyType_=byte,_SumName_=Int,_SumType_=int;_KeyName_=Byte,_KeyType_=byte,_SumName_=Long,_SumType_=long;_KeyName_=Byte,_KeyType_=byte,_SumName_=Float,_SumType_=float;_KeyName_=Byte,_KeyType_=byte,_SumName_=Double,_SumType_=double;_KeyName_=Short,_KeyType_=short,_SumName_=Short,_SumType_=short;_KeyName_=Short,_KeyType_=short,_SumName_=Int,_SumType_=int;_KeyName_=Short,_KeyType_=short,_SumName_=Long,_SumType_=long;_KeyName_=Short,_KeyType_=short,_SumName_=Float,_SumType_=float;_KeyName_=Short,_KeyType_=short,_SumName_=Double,_SumType_=double;_KeyName_=Int,_KeyType_=int,_SumName_=Int,_SumType_=int;_KeyName_=Int,_KeyType_=int,_SumName_=Long,_SumType_=long;_KeyName_=Int,_KeyType_=int,_SumName_=Float,_SumType_=float;_KeyName_=Int,_KeyType_=int,_SumName_=Double,_SumType_=double;_KeyName_=Long,_KeyType_=long,_SumName_=Long,_SumType_=long;_KeyName_=Long,_KeyType_=long,_SumName_=Float,_SumType_=float;_KeyName_=Long,_KeyType_=long,_SumName_=Double,_SumType_=double;_KeyName_=Float,_KeyType_=float,_SumName_=Float,_SumType_=float;_KeyName_=Float,_KeyType_=float,_SumName_=Double,_SumType_=double;_KeyName_=Double,_KeyType_=double,_SumName_=Double,_SumType_=double

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

package io.viewserver.operators.group.summary.sum;

import io.viewserver.core.IArithmetic;
import io.viewserver.core._KeyType_;
import io.viewserver.operators.group.ISummary;
import io.viewserver.operators.group.summary.ISummaryContext;
import io.viewserver.schema.column.*;

/**
 * Created by bemm on 02/10/2014.
 */
public class Sum_KeyName__SumName_ implements ISummary {
    private final String name;
    private final String column;
    private ColumnHolder_KeyName_ columnHolder;
    private ColumnHolder summaryColumn;
    private ISummaryContext context;

    public Sum_KeyName__SumName_(String name, String column) {
        this.name = name;
        this.column = column;
    }

    @Override
    public void initialise(ISummaryContext context) {
        this.context = context;
        this.summaryColumn = ColumnHolderUtils.createColumnHolder(name, getType());
        context.getTableStorage().initialiseColumn(summaryColumn);
        context.setResultColumn(summaryColumn);
        columnHolder = (ColumnHolder_KeyName_) context.getInboundSchema().getColumnHolder(column);
        ((IWritableColumn)columnHolder.getColumn()).storePreviousValues();
    }

    @Override
    public void onGroupAdd(int groupId) {
    }

    @Override
    public void onGroupRemove(int groupId) {
        IWritableColumn_SumName_ column = (IWritableColumn_SumName_) summaryColumn.getColumn();
        column.set_SumName_(groupId, (_SumType_)0);
    }

    @Override
    public void onGroupEnter(int groupId, int rowId) {
        _KeyType_ value = columnHolder.get_KeyName_(rowId);
        if (value != 0) {
            IWritableColumn_SumName_ column = (IWritableColumn_SumName_) summaryColumn.getColumn();
            _SumType_ oldSum = column.get_SumName_(groupId);
            _SumType_ newSum = IArithmetic._SumName_.add(oldSum, value);
            column.set_SumName_(groupId, newSum);
            context.markDirty(groupId);
        }
    }

    @Override
    public void onGroupLeave(int groupId, int rowId) {
        _KeyType_ value = columnHolder.getPrevious_KeyName_(rowId);
        if (value != 0) {
            IWritableColumn_SumName_ column = (IWritableColumn_SumName_) summaryColumn.getColumn();
            _SumType_ oldSum = column.get_SumName_(groupId);
            _SumType_ newSum = IArithmetic._SumName_.subtract(oldSum, value);
            column.set_SumName_(groupId, newSum);
            context.markDirty(groupId);
        }
    }

    @Override
    public void onRowUpdate(int groupId, int rowId) {
        _KeyType_ newValue = columnHolder.get_KeyName_(rowId);
        _KeyType_ oldValue = columnHolder.getPrevious_KeyName_(rowId);
        if (newValue != oldValue) {
            IWritableColumn_SumName_ column = (IWritableColumn_SumName_) summaryColumn.getColumn();
            _SumType_ oldSum = column.get_SumName_(groupId);
            _SumType_ newSum = IArithmetic._SumName_.add(IArithmetic._SumName_.subtract(oldSum, oldValue), newValue);
            column.set_SumName_(groupId, newSum);
            context.markDirty(groupId);
        }
    }

    @Override
    public void onAfterCommit() {

    }

    @Override
    public boolean hasChanges(IRowFlags rowFlags) {
        return rowFlags.isDirty(columnHolder.getColumnId());
    }

    @Override
    public void reset() {
        ((IWritableColumn_SumName_)summaryColumn.getColumn()).resetAll();
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
        return ColumnType._SumName_;
    }
}
