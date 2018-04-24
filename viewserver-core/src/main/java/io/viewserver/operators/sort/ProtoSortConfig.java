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

package io.viewserver.operators.sort;

import java.util.List;

/**
 * Created by bemm on 25/11/2014.
 */
public class ProtoSortConfig implements ISortConfig {
    private final SortOperator.SortDescriptor sortDescriptor;
    private final int start;
    private final int end;

    public ProtoSortConfig(io.viewserver.messages.config.ISortConfig sortConfigDto) {
        this.sortDescriptor = new SortOperator.SortDescriptor(sortConfigDto.getSortDescriptor().getColumnName());
        final List<io.viewserver.messages.config.ISortConfig.ISortColumn> columnsToSort = sortConfigDto.getSortDescriptor().getColumnsToSort();
        final int count = columnsToSort.size();
        for (int i = 0; i < count; i++) {
            final io.viewserver.messages.config.ISortConfig.ISortColumn columnToSort = columnsToSort.get(i);
            this.sortDescriptor.getColumnsToSort().add(new SortOperator.SortColumn(columnToSort.getName(),
                    columnToSort.isDescending()));
            columnToSort.release();
        }

        this.start = sortConfigDto.getStart();
        this.end = sortConfigDto.getEnd();
    }

    @Override
    public SortOperator.SortDescriptor getSortDescriptor() {
        return sortDescriptor;
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getEnd() {
        return end;
    }
}
