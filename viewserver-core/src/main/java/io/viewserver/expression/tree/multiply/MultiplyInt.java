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

package io.viewserver.expression.tree.multiply;

import io.viewserver.expression.tree.IExpressionInt;
import io.viewserver.expression.tree.IExpressionLong;
import io.viewserver.schema.column.ColumnType;

/**
 * Created by nickc on 11/12/2014.
 */
public class MultiplyInt implements IExpressionLong {
    private final IExpressionInt lhs;
    private final IExpressionInt rhs;

    public MultiplyInt(IExpressionInt lhs, IExpressionInt rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public long getLong(int row) {
        return ((long)lhs.getInt(row)) * ((long)rhs.getInt(row));
    }

    @Override
    public ColumnType getType() {
        return ColumnType.Long;
    }
}
