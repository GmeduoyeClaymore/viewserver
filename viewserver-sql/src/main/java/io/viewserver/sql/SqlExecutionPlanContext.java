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

import io.viewserver.execution.context.OptionsExecutionPlanContext;
import io.viewserver.execution.nodes.*;
import com.facebook.presto.sql.tree.Query;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nick on 19/11/15.
 */
public class SqlExecutionPlanContext extends OptionsExecutionPlanContext {
    private final Query query;
    private final boolean permanent;
    private ProjectionNode projection;
    private FilterNode filter;
    private CalcColNode preSummaryCalculations;
    private CalcColNode postSummaryCalculations;
    private SortNode orderBy;
    private GroupByNode groupBy;
    private FilterNode having;
    private boolean usingPrefixes;
    private Map<String, String> prefixes = new HashMap<>();

    public SqlExecutionPlanContext(Query query, boolean permanent) {
        this.query = query;
        this.permanent = permanent;
    }

    public Query getQuery() {
        return query;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public ProjectionNode getProjection() {
        return projection;
    }

    public void setProjection(ProjectionNode projection) {
        this.projection = projection;
    }

    public FilterNode getFilter() {
        return filter;
    }

    public void setFilter(FilterNode filter) {
        this.filter = filter;
    }

    public FilterNode getHaving() {
        return having;
    }

    public void setHaving(FilterNode having) {
        this.having = having;
    }

    public SortNode getOrderBy() {
        return orderBy;
    }

    public SortNode getOrCreateOrderBy() {
        if (orderBy == null) {
            orderBy = new SortNode("orderBy").withColumnName("orderBy");
            getGraphNodes().add(orderBy);
        }
        return orderBy;
    }

    public CalcColNode getPreSummaryCalculations() {
        return preSummaryCalculations;
    }

    public CalcColNode getOrCreatePreSummaryCalculations() {
        if (preSummaryCalculations == null) {
            preSummaryCalculations = new CalcColNode("preSummaryCalcs");
            getGraphNodes().add(preSummaryCalculations);
        }
        return preSummaryCalculations;
    }

    public GroupByNode getGroupBy() {
        return groupBy;
    }

    public GroupByNode getOrCreateGroupBy() {
        if (groupBy == null) {
            groupBy = new GroupByNode("groupBy");
            getGraphNodes().add(groupBy);
        }
        return groupBy;
    }

    public CalcColNode getPostSummaryCalculations() {
        return postSummaryCalculations;
    }

    public CalcColNode getOrCreatePostSummaryCalculations() {
        if (postSummaryCalculations == null) {
            postSummaryCalculations = new CalcColNode("postSummaryCalcs");
            getGraphNodes().add(postSummaryCalculations);
        }
        return postSummaryCalculations;
    }

    public boolean isUsingPrefixes() {
        return usingPrefixes;
    }

    public void setUsingPrefixes(boolean usingPrefixes) {
        this.usingPrefixes = usingPrefixes;
    }

    public void addPrefix(String prefix, String tableName) {
        prefixes.put(prefix, tableName);
    }

    public String getTableNameForPrefix(String prefix) {
        return prefixes.get(prefix);
    }

    public String getPrefixForTableName(String tableName) {
        for (Map.Entry<String, String> entry : prefixes.entrySet()) {
            if (entry.getValue().equals(tableName)) {
                return entry.getKey();
            }
        }
        return tableName;
    }
}
