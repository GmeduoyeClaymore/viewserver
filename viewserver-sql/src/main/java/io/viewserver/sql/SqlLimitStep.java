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

package io.viewserver.sql;

import io.viewserver.execution.nodes.SortNode;
import io.viewserver.execution.steps.IExecutionPlanStep;
import com.facebook.presto.sql.tree.Query;
import com.facebook.presto.sql.tree.QuerySpecification;
import javolution.text.TypeFormat;

import java.util.Optional;

/**
 * Created by bemm on 19/11/15.
 */
public class SqlLimitStep implements IExecutionPlanStep<SqlExecutionPlanContext> {
    @Override
    public void execute(SqlExecutionPlanContext executionPlanContext) {
        final Query query = executionPlanContext.getQuery();
        final QuerySpecification body = (QuerySpecification) query.getQueryBody();
        Optional<String> limit = body.getLimit();
        if (limit.isPresent()) {
            int limitValue = TypeFormat.parseInt(limit.get());
            SortNode orderBy = executionPlanContext.getOrCreateOrderBy();
            if (orderBy.getOffset() == -1) {
                orderBy.withOffset(0);
            }
            orderBy.withLimit(limitValue);
        }
    }
}
