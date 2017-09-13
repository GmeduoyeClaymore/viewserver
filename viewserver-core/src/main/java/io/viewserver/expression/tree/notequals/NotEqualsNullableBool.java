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

package io.viewserver.expression.tree.notequals;

import io.viewserver.expression.tree.IExpressionBool;
import io.viewserver.expression.tree.IExpressionNullableBool;
import io.viewserver.schema.column.ColumnType;

public class NotEqualsNullableBool implements IExpressionBool {
    private final IExpressionNullableBool lhs;
    private final IExpressionNullableBool rhs;

    public NotEqualsNullableBool(IExpressionNullableBool lhs, IExpressionNullableBool rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public boolean getBool(int row) {
        return lhs.getNullableBool(row) != rhs.getNullableBool(row);
    }

    @Override
    public ColumnType getType() {
        return ColumnType.Bool;
    }
}
