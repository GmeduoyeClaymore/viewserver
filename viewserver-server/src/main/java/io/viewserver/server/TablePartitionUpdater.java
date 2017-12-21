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

package io.viewserver.server;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.LocalTableUpdater;
import io.viewserver.datasource.PartitionConfig;
import io.viewserver.operators.table.ITable;
import io.viewserver.operators.table.TablePartitionOperator;
import io.viewserver.schema.Schema;

/**
 * Created by nick on 10/11/15.
 */
public class TablePartitionUpdater extends LocalTableUpdater {
    private final PartitionConfig partitionConfig;

    public TablePartitionUpdater(IExecutionContext executionContext, ICatalog catalog, PartitionConfig partitionConfig) {
        super(executionContext, catalog);
        this.partitionConfig = partitionConfig;
    }

    @Override
    public ITable createTable(String name, Schema schema) {
        TablePartitionOperator tablePartitionOperator = new TablePartitionOperator(name, executionContext, catalog, partitionConfig.getPartitionColumnName(),
                partitionConfig.getPartitionValue());
        catalog.getOperator(partitionConfig.getSourceTableName()).getOutput(Constants.OUT).plugIn(tablePartitionOperator.getInput());
        table = tablePartitionOperator;
        return tablePartitionOperator;
    }
}
