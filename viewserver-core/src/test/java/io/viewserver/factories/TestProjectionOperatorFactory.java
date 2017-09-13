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
import io.viewserver.core.ExecutionContext;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.operators.projection.ProjectionOperator;

import java.util.*;

/**
 * Created by bemm on 01/12/2014.
 */
public class TestProjectionOperatorFactory implements ITestOperatorFactory{
    private ExecutionContext executionContext;
    private ICatalog catalog;

    public static String PROJECTION_PARAM_NAME = "projections";
    public static String INCLUDED_COLUMNS = "included";
    public static String EXCLUDED_COLUMNS = "excluded";

    public TestProjectionOperatorFactory(ExecutionContext executionContext, ICatalog catalog) {
        this.executionContext = executionContext;
        this.catalog = catalog;
    }

    @Override
    public String getOperatorType() {
        return "projection";
    }

    @Override
    public IOperator create(String operatorName, Map<String, Object> context) {
        ProjectionOperator filter = new ProjectionOperator(operatorName, executionContext, catalog);
        configure(operatorName,context);
        return filter;
    }

    @Override
    public void configure(String operatorName, Map<String, Object> config){

        IOperator operator = catalog.getOperator(operatorName);
        if(operator == null){
            throw new RuntimeException("Unable to find operator named "  + operatorName + " in catalog");
        }

        String[]  projections = null;
        String projectionsStr = ITestOperatorFactory.getParam(PROJECTION_PARAM_NAME, config, String.class,true);
        if(projectionsStr != null){
            projections = projectionsStr.split(",");
        }
        List<String>  includedColumns = null;
        String includedColumnsStr = ITestOperatorFactory.getParam(INCLUDED_COLUMNS, config, String.class,true);
        if(includedColumnsStr != null) {
            includedColumns = Arrays.asList(includedColumnsStr.split(","));
        }
        List<String>  excludedColumns = null;
        String excludedColumnsStr = ITestOperatorFactory.getParam(EXCLUDED_COLUMNS, config, String.class,true);
        if(excludedColumnsStr != null) {
            excludedColumns = Arrays.asList(excludedColumnsStr.split(","));
        }

        final List<String> finalIncludedColumns = includedColumns;
        final List<String> finalExcludedColumns = excludedColumns;
        final String[] finalProjections = projections;
        ((ProjectionOperator)operator).configure(new IProjectionConfig() {
            @Override
            public ProjectionMode getMode() {
                if (finalIncludedColumns != null) {
                    return ProjectionMode.Inclusionary;
                } else if (finalExcludedColumns != null) {
                    return ProjectionMode.Exclusionary;
                } else {
                    return ProjectionMode.Projection;
                }
            }

            @Override
            public Collection<ProjectionColumn> getProjectionColumns() {
                ArrayList<ProjectionColumn> projectionColumns = new ArrayList<>();
                if (finalProjections != null) {
                    for (String entry : finalProjections) {
                        String[] entryPairs = entry.split("=");
                        if (entryPairs.length != 2) {
                            throw new RuntimeException(String.format("Invalid projection \"%s\". Should be in the format FROM_COLUMN=TO_COLUMN", entry));
                        }
                        projectionColumns.add(new ProjectionColumn(entryPairs[0], entryPairs[1]));
                    }
                }
                if (finalIncludedColumns != null) {
                    for (String includedColumn : finalIncludedColumns) {
                        projectionColumns.add(new ProjectionColumn(includedColumn));
                    }
                }
                if (finalExcludedColumns != null) {
                    for (String excludedColumn : finalExcludedColumns) {
                        projectionColumns.add(new ProjectionColumn(excludedColumn));
                    }
                }
                return projectionColumns;
            }
        }, new CommandResult());
    }
}
