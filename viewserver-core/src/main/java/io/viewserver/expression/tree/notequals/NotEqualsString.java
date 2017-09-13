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
import io.viewserver.expression.tree.IExpressionString;
import io.viewserver.expression.tree.literal.LiteralString;
import io.viewserver.schema.column.ColumnType;
import com.google.common.base.Objects;

public class NotEqualsString implements IExpressionBool {
        private final IExpressionString lhs;
        private final IExpressionString rhs;
    private boolean isLiteral;
    private boolean isNotEqual;

        public NotEqualsString(IExpressionString lhs, IExpressionString rhs) {
            this.lhs = lhs;
            this.rhs = rhs;

            if (lhs instanceof LiteralString && rhs instanceof LiteralString) {
                isLiteral = true;
                isNotEqual = !Objects.equal(lhs.getString(0), rhs.getString(0));
            }
        }

        @Override
        public boolean getBool(int row) {
            return isLiteral ? isNotEqual : !Objects.equal(lhs.getString(0), rhs.getString(0));
        }

        @Override
        public ColumnType getType() {
            return ColumnType.Bool;
        }
    }
