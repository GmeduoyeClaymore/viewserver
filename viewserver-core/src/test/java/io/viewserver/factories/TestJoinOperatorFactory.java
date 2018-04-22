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

package io.viewserver.factories;

import io.viewserver.catalog.ICatalog;
import io.viewserver.command.CommandResult;
import io.viewserver.core.IExecutionContext;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.join.IColumnNameResolver;
import io.viewserver.operators.join.IJoinConfig;
import io.viewserver.operators.join.JoinOperator;

import java.util.Map;

/**
 * Created by bemm on 01/12/2014.
 */
public class TestJoinOperatorFactory implements ITestOperatorFactory{
    private IExecutionContext executionContext;
    private ICatalog catalog;

    public static String JOIN_LEFT_COLUMN_PARAM_NAME = "leftColumn";
    public static String JOIN_RIGHT_COLUMN_PARAM_NAME = "rightColumn";

    public TestJoinOperatorFactory(IExecutionContext executionContext, ICatalog catalog) {
        this.executionContext = executionContext;
        this.catalog = catalog;
    }

    @Override
    public String getOperatorType() {
        return "join";
    }

    @Override
    public IOperator create(String operatorName, Map<String, Object> context) {
        JoinOperator filter = new JoinOperator(operatorName, executionContext, catalog);
        configure(operatorName,context);
        return filter;
    }

    @Override
    public void configure(String operatorName, Map<String, Object> config){
        IOperator join = catalog.getOperatorByPath(operatorName);
        if(join == null){
            throw new RuntimeException("Unable to find operator named "  + operatorName + " in catalog");
        }
        String  leftColumns[] = ITestOperatorFactory.getParam(JOIN_LEFT_COLUMN_PARAM_NAME, config, String.class).split(",");
        String  rightColumns[] = ITestOperatorFactory.getParam(JOIN_RIGHT_COLUMN_PARAM_NAME, config, String.class).split(",");

        ((JoinOperator) join).configure(new IJoinConfig() {
            @Override
            public String[] getLeftJoinColumns() {
                return leftColumns;
            }

            @Override
            public boolean isLeftJoinOuter() {
                return false;
            }

            @Override
            public String[] getRightJoinColumns() {
                return rightColumns;
            }

            @Override
            public boolean isRightJoinOuter() {
                return false;
            }

            @Override
            public IColumnNameResolver getColumnNameResolver() {
                return null;
            }

            @Override
            public String getLeftPrefix() {
                return null;
            }

            @Override
            public String getRightPrefix() {
                return null;
            }

            @Override
            public boolean getAlwaysResolveNames() {
                return false;
            }
        }, new CommandResult());
    }


}
