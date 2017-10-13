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

package io.viewserver.expression.tree.like;

import io.viewserver.expression.tree.IExpressionBool;
import io.viewserver.expression.tree.IExpressionString;
import io.viewserver.schema.column.ColumnType;

/**
 * Created by nick on 31/03/2015.
 */
public class LikeExpression implements IExpressionBool {
    private final IExpressionString lhs;
    private final IExpressionString rhs;

    public LikeExpression(IExpressionString lhs, IExpressionString rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public boolean getBool(int row) {
        String lhsString = lhs.getString(row);
        String rhsString = rhs.getString(row);
        if (rhsString == null || "".equals(rhsString)) {
            return false;
        }
        if ("*".equals(rhsString)) {
            return true;
        }
        if (lhsString == null || "".equals(lhsString)) {
            return false;
        }
        if (rhsString.startsWith("*")) {
            if (rhsString.endsWith("*")) {
                if(rhsString.length() <= 2){
                    return true;
                }else{
                    return lhsString.toLowerCase().contains(rhsString.toLowerCase().substring(1, rhsString.length() - 2));
                }
            }
            return lhsString.toLowerCase().endsWith(rhsString.toLowerCase().substring(1));
        }
        if (rhsString.endsWith("*")) {
            return lhsString.toLowerCase().startsWith(rhsString.toLowerCase().substring(0, rhsString.length() - 2));
        }
        return lhsString.toLowerCase().equals(rhsString.toLowerCase());
    }

    @Override
    public ColumnType getType() {
        return ColumnType.Bool;
    }
}
