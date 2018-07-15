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

package io.viewserver.expression.function;

import io.viewserver.datasource.DimensionMapper;
import io.viewserver.expression.tree.IExpression;
import io.viewserver.expression.tree.IExpressionInt;
import io.viewserver.expression.tree.IExpressionString;
import io.viewserver.schema.column.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Paul on 19/11/2015.
 */

public class UnEnum implements IUserDefinedFunction, IExpressionString {
    private static final Logger log = LoggerFactory.getLogger(UnEnum.class);
    private IExpressionInt dimensionColumn;
    private IExpressionString dimensionName;
    private IExpressionString dataSourceName;

    private DimensionMapper dimensionMapper;

    public UnEnum(DimensionMapper dimensionMapper) {
        this.dimensionMapper = dimensionMapper;
    }

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length != 3) {
            throw new IllegalArgumentException("Syntax: unenum(<dimensionColumn (int)>,<dimensionName (String)>,<dataSourceName (String)>)");
        }

        dimensionColumn = (IExpressionInt) parameters[0];
        dimensionName = (IExpressionString) parameters[1];
        dataSourceName = (IExpressionString) parameters[2];
    }

    @Override
    public ColumnType getType() {
        return ColumnType.String;
    }


    @Override
    public String getString(int row) {
        int dimVal = this.dimensionColumn.getInt(row);
        String dimensionName = this.dimensionName.getString(row);
        String dataSourceNameString = this.dataSourceName.getString(row);
        String result = this.dimensionMapper.lookupString(dataSourceNameString, dimensionName, dimVal);
        return result;
    }
}


