// :_KeyName_=Byte,_KeyType_=byte;_KeyName_=Short,_KeyType_=short;_KeyName_=Int,_KeyType_=int;_KeyName_=Long,_KeyType_=long;_KeyName_=Float,_KeyType_=float;_KeyName_=Double,_KeyType_=double

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

package io.viewserver.expression.tree.divide;

import io.viewserver.core._KeyType_;
import io.viewserver.expression.tree.IExpression_KeyName_;
import io.viewserver.schema.column.ColumnType;

public class Divide_KeyName_ implements IExpression_KeyName_ {
        private final IExpression_KeyName_ lhs;
        private final IExpression_KeyName_ rhs;

        public Divide_KeyName_(IExpression_KeyName_ lhs, IExpression_KeyName_ rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public _KeyType_ get_KeyName_(int row) {
            return (_KeyType_)(lhs.get_KeyName_(row) / rhs.get_KeyName_(row));
        }

        @Override
        public ColumnType getType() {
            return ColumnType._KeyName_;
        }
    }
