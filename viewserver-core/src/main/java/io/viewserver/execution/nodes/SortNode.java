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

package io.viewserver.execution.nodes;

import io.viewserver.execution.ParameterHelper;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.config.IOperatorConfig;
import io.viewserver.operators.sort.ISortConfig;
import io.viewserver.operators.sort.SortOperator;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by nickc on 03/11/2014.
 */
public class SortNode extends GraphNodeBase<SortNode> {
    private final List<SortOperator.SortColumn> columnsToSort = new ArrayList<>();
    private int offset = -1;
    private int limit = -1;
    private String columnName;

    public SortNode(){super();}

    public SortNode(String name) {
        super(name, "Sort");
    }

    public SortNode withColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public SortNode withColumnsToSort(SortOperator.SortColumn... columnsToSort) {
        this.columnsToSort.addAll(Arrays.asList(columnsToSort));
        return this;
    }

    public SortNode withColumnsToSort(Collection<SortOperator.SortColumn> columnsToSort) {
        this.columnsToSort.addAll(columnsToSort);
        return this;
    }

    public SortNode withOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public SortNode withLimit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public Object getConfig(ParameterHelper parameterHelper) {
        return new ISortConfig() {
            @Override
            public SortOperator.SortDescriptor getSortDescriptor() {
                return new SortOperator.SortDescriptor(columnName, columnsToSort);
            }

            @Override
            public int getStart() {
                return offset;
            }

            @Override
            public int getEnd() {
                return limit;
            }
        };
    }

    @Override
    protected IOperatorConfig getConfigDto(ParameterHelper parameterHelper) {
        io.viewserver.messages.config.ISortConfig sortConfigDto = MessagePool.getInstance().get(io.viewserver.messages.config.ISortConfig.class)
                .setStart(offset)
                .setEnd(limit);
        io.viewserver.messages.config.ISortConfig.ISortDescriptor sortDescriptor = MessagePool.getInstance().get(io.viewserver.messages.config.ISortConfig.ISortDescriptor.class)
                .setColumnName(columnName);
        final List<io.viewserver.messages.config.ISortConfig.ISortColumn> columnsToSort = sortDescriptor.getColumnsToSort();
        for (SortOperator.SortColumn columnToSort : this.columnsToSort) {
            final io.viewserver.messages.config.ISortConfig.ISortColumn sortColumnDto = MessagePool.getInstance().get(io.viewserver.messages.config.ISortConfig.ISortColumn.class)
                    .setName(columnToSort.getName())
                    .setDescending(columnToSort.isDescending());
            columnsToSort.add(sortColumnDto);
            sortColumnDto.release();
        }
        sortConfigDto.setSortDescriptor(sortDescriptor);
        sortDescriptor.release();
        return sortConfigDto;
    }

    @Override
    protected String getConfigForOperatorName(ParameterHelper parameterHelper) {
        return String.format("sort:[%s]:%s:%d:%d",
                StringUtils.join(columnsToSort, ','),
                columnName,
                offset,
                limit
                );
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public List<SortOperator.SortColumn> getColumnsToSort() {
        return columnsToSort;
    }

    @Override
    public String toString() {
        return "SortNode{" +
                "columnsToSort=" + columnsToSort +
                ", offset=" + offset +
                ", limit=" + limit +
                ", columnName='" + columnName + '\'' +
                '}';
    }
}
