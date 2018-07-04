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

import io.viewserver.expression.tree.IExpression;
import io.viewserver.expression.tree.IExpressionBool;
import io.viewserver.expression.tree.IExpressionString;
import io.viewserver.schema.column.ColumnType;

/**
 * Created by bemm on 24/03/2015.
 */
public class StringIfJoin implements IUserDefinedFunction, IExpressionString {
    private IExpression[] parameters;

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length < 4 || parameters.length % 2 == 1) {
            throwSyntaxException();
        }
        if (!(parameters[0] instanceof IExpressionString)) {
            throwSyntaxException();
        }
        if (!(parameters[1] instanceof IExpressionString)) {
            throwSyntaxException();
        }
        for (int i = 2; i < parameters.length; i += 2) {
            if (!(parameters[i] instanceof IExpressionBool)) {
                throwSyntaxException();
            }
            if (!(parameters[i + 1] instanceof IExpressionString)) {
                throwSyntaxException();
            }
        }

        this.parameters = parameters;
    }

    private void throwSyntaxException() {
        throw new IllegalArgumentException("Syntax: stringifjoin([prefix], [jointext], [condition], [text], { [condition], [text], ... })");
    }

    @Override
    public String getString(int row) {
        StringBuilder builder = new StringBuilder();
        String prefix = ((IExpressionString)parameters[0]).getString(row);
        String joinText = ((IExpressionString)parameters[1]).getString(row);
        boolean first = true;
        for (int i = 2; i < parameters.length; i += 2) {
            if (((IExpressionBool) parameters[i]).getBool(row)) {
                if (first) {
                    if (prefix != null) {
                        builder.append(prefix);
                    }
                } else {
                    if (joinText != null) {
                        builder.append(joinText);
                    }
                }

                builder.append(((IExpressionString)parameters[i + 1]).getString(row));

                first = false;
            }
        }
        return builder.toString();
    }

    @Override
    public ColumnType getType() {
        return ColumnType.String;
    }
}
