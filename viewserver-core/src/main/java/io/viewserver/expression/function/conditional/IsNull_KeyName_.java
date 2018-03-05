// :_KeyName_=Bool,_KeyType_=boolean;_KeyName_=Byte,_KeyType_=byte;_KeyName_=Short,_KeyType_=short;_KeyName_=Int,_KeyType_=int;_KeyName_=Long,_KeyType_=long;_KeyName_=Float,_KeyType_=float;_KeyName_=Double,_KeyType_=double;_KeyName_=String,_KeyType_=String

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

package io.viewserver.expression.function.conditional;

import io.viewserver.core._KeyType_;
import io.viewserver.expression.tree.IExpressionBool;
import io.viewserver.expression.tree.IExpression_KeyName_;
import io.viewserver.schema.column.ColumnType;

public class IsNull_KeyName_ implements IExpression_KeyName_ {
    private final IExpression_KeyName_ primaryValue;
    private final IExpression_KeyName_ fallback;
    private static _KeyType_ DEFAULT;


    public IsNull_KeyName_(IExpression_KeyName_ primaryValue, IExpression_KeyName_ fallback) {
        this.primaryValue = primaryValue;
        this.fallback = fallback;
    }

    @Override
    public _KeyType_ get_KeyName_(int row) {
        _KeyType_ keyName_ = primaryValue.get_KeyName_(row);
        return keyName_ != DEFAULT ? keyName_ : fallback.get_KeyName_(row);
    }

    @Override
    public ColumnType getType() {
        return ColumnType._KeyName_;
    }
}
