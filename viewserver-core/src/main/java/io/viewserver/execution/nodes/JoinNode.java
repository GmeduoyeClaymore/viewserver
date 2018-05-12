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

import io.viewserver.execution.JoinColumnNamer;
import io.viewserver.execution.ParameterHelper;
import io.viewserver.messages.common.ValueLists;
import io.viewserver.messages.config.IOperatorConfig;
import io.viewserver.operators.join.IColumnNameResolver;
import io.viewserver.operators.join.IJoinConfig;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Created by nick on 16/03/2015.
 */
public class JoinNode extends GraphNodeBase<JoinNode> {
    private String[] leftJoinColumns = new String[0];
    private String[] rightJoinColumns = new String[0];
    private String isLeftJoinOuter;
    private String isRightJoinOuter;
    private IColumnNameResolver columnNameResolver;
    private boolean alwaysResolveNames;
    private String leftPrefix;
    private String rightPrefix;

    public JoinNode() {
        super();
    }

    public JoinNode(String name) {
        super(name, "Join");
    }

    public JoinNode withLeftJoinColumns(String... leftJoinColumns) {
        this.leftJoinColumns = leftJoinColumns;
        return this;
    }

    public JoinNode withLeftJoinColumns(ValueLists.IStringList leftJoinColumns) {
        this.leftJoinColumns = leftJoinColumns.toArray(new String[0]);
        return this;
    }

    public JoinNode withRightJoinColumns(String... rightJoinColumns) {
        this.rightJoinColumns = rightJoinColumns;
        return this;
    }

    public JoinNode withRightJoinColumns(ValueLists.IStringList rightJoinColumns) {
        this.rightJoinColumns = rightJoinColumns.toArray(new String[0]);
        return this;
    }

    public JoinNode withColumnNameResolver(IColumnNameResolver columnNameResolver) {
        this.columnNameResolver = columnNameResolver;
        return this;
    }

    public JoinNode withColumnPrefixes(String leftPrefix, String rightPrefix){
        this.leftPrefix = leftPrefix;
        this.rightPrefix = rightPrefix;
        return this;
    }

    public JoinNode withColumnPrefixes(String leftPrefix){
        this.leftPrefix = leftPrefix;
        return this;
    }

    public JoinNode withAlwaysResolveNames() {
        this.alwaysResolveNames = true;
        return this;
    }

    @Override
    public Object getConfig(ParameterHelper parameterHelper) {
        return new IJoinConfig() {
            @Override
            public String[] getLeftJoinColumns() {
                return parameterize(leftJoinColumns,parameterHelper);
            }

            @Override
            public boolean isLeftJoinOuter() {
                return Boolean.parseBoolean(parameterHelper.substituteParameterValues(isLeftJoinOuter));
            }

            @Override
            public String[] getRightJoinColumns() {
                return parameterize(rightJoinColumns,parameterHelper);
            }

            @Override
            public boolean isRightJoinOuter() {
                return  Boolean.parseBoolean(parameterHelper.substituteParameterValues(isRightJoinOuter));
            }

            @Override
            public IColumnNameResolver getColumnNameResolver() {
                return columnNameResolver;
            }

            @Override
            public String getLeftPrefix() { return leftPrefix; }

            @Override
            public String getRightPrefix() { return rightPrefix; }

            @Override
            public boolean getAlwaysResolveNames() {
                return alwaysResolveNames;
            }
        };
    }

    private String[] parameterize(String[] columns, ParameterHelper parameterHelper) {
        String[] result = new String[columns.length];
        for(int i=0;i<columns.length;i++){
            result[i] = parameterHelper.substituteParameterValues(columns[i]);
        }
        return result;
    }

    @Override
    protected String getConfigForOperatorName(ParameterHelper parameterHelper) {
        // TODO: add column name resolver in here
        return String.format("join:%s:%b:%s:%b",
                StringUtils.join(leftJoinColumns, ","),
                isLeftJoinOuter,
                StringUtils.join(rightJoinColumns, ","),
                isRightJoinOuter
                );
    }

    @Override
    protected IOperatorConfig getConfigDto(ParameterHelper parameterHelper) {
        throw new UnsupportedOperationException("Haven't implemented join config dto");
    }

    public JoinNode withLeftJoinOuter() {
        return withLeftJoinOuter("true");
    }

    public JoinNode withLeftJoinOuter(String shouldLeftOuter) {
        this.isLeftJoinOuter = shouldLeftOuter;
        return this;
    }

    public JoinNode withRightJoinOuter(String shouldRightOuter) {
        this.isRightJoinOuter = shouldRightOuter;
        return this;
    }
    public JoinNode withRightJoinOuter() {
        return withRightJoinOuter("true");
    }

    public IColumnNameResolver getColumnNameResolver() {
        return columnNameResolver;
    }

    public void setColumnNameResolver(IColumnNameResolver columnNameResolver) {
        this.columnNameResolver = columnNameResolver;
    }

    public String getLeftPrefix() {
        return leftPrefix;
    }

    public void setLeftPrefix(String leftPrefix) {
        this.leftPrefix = leftPrefix;
    }

    public String getRightPrefix() {
        return rightPrefix;
    }

    public void setRightPrefix(String rightPrefix) {
        this.rightPrefix = rightPrefix;
    }

    public boolean isAlwaysResolveNames() {
        return alwaysResolveNames;
    }

    public void setAlwaysResolveNames(boolean alwaysResolveNames) {
        this.alwaysResolveNames = alwaysResolveNames;
    }

    public String[] getLeftJoinColumns() {
        return leftJoinColumns;
    }

    public void setLeftJoinColumns(String[] leftJoinColumns) {
        this.leftJoinColumns = leftJoinColumns;
    }

    public String[] getRightJoinColumns() {
        return rightJoinColumns;
    }

    public void setRightJoinColumns(String[] rightJoinColumns) {
        this.rightJoinColumns = rightJoinColumns;
    }

    public String getLeftJoinOuter() {
        return isLeftJoinOuter;
    }

    public void setLeftJoinOuter(String isLeftJoinOuter) {
        this.isLeftJoinOuter = isLeftJoinOuter;
    }

    public String getRightJoinOuter() {
        return isRightJoinOuter;
    }

    public void setRightJoinOuter(String isRightJoinOuter) {
        this.isRightJoinOuter = isRightJoinOuter;
    }

    public JoinNode withLeftJoinColumns(List<String> leftJoinColumns) {
        this.leftJoinColumns = leftJoinColumns.toArray(new String[leftJoinColumns.size()]);
        return this;
    }

    public JoinNode withRightJoinColumns(List<String> rightJoinColumns) {
        this.rightJoinColumns = rightJoinColumns.toArray(new String[rightJoinColumns.size()]);
        return this;
    }
}
