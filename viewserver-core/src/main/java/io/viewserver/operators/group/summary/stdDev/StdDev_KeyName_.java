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


package io.viewserver.operators.group.summary.stdDev;

import io.viewserver.core._KeyType_;
import io.viewserver.operators.group.summary.var.Var_KeyName_;

/*
 * Created by paulg on 02/10/2014.
  */

public class StdDev_KeyName_ extends Var_KeyName_ {
    public StdDev_KeyName_(String name, String column, String countColumn) {
        super(name, column, countColumn);
    }

    @Override
    protected double getUpdatedValue(int groupId, _KeyType_ newValue, _KeyType_ oldValue) {
        return Math.sqrt(super.getUpdatedValue(groupId, newValue, oldValue));
    }
}
