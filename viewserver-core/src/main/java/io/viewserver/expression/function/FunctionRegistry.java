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

import io.viewserver.expression.function.abs.Abs;
import io.viewserver.expression.function.conditional.Conditional;
import io.viewserver.expression.function.conditional.IsNull;
import io.viewserver.expression.function.sqrt.Sqrt;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bemm on 14/10/2014.
 */
public class FunctionRegistry {
    private Map<String, Class<? extends IUserDefinedFunction>> functions = new HashMap<>();

    public FunctionRegistry() {
        functions.put("abs", Abs.class);
        functions.put("businessDay", DateToBusinessDay.class);
        functions.put("weekday", DateToWeekday.class);
        functions.put("dayToBusinessDay", DayToBusinessDay.class);

        functions.put("round", Round.class);
        functions.put("day", DateToDay.class);
        functions.put("if", Conditional.class);
        functions.put("isNull", IsNull.class);
        functions.put("text", Text.class);
        functions.put("sqrt", Sqrt.class);
        functions.put("stringifjoin", StringIfJoin.class);

        functions.put("hash", Hash.class);
        functions.put("serialize", Serialize.class);
        functions.put("distance", Distance.class);
        functions.put("distanceJson", DistanceJson.class);
    }

    public void register(String name, Class<? extends IUserDefinedFunction> clazz) {
        functions.put(name, clazz);
    }

    public IUserDefinedFunction create(String name) {
        Class<? extends IUserDefinedFunction> clazz = functions.get(name);
        if (clazz == null) {
            return null;
        }

        try {
            return clazz.newInstance();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
