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

package io.viewserver.factories;

import io.viewserver.operators.IOperator;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bemm on 01/12/2014.
 */
public interface ITestOperatorFactory {

    String getOperatorType();

    IOperator create(String operatorName, Map<String, Object> context);

    public static HashMap<String,TestTypeConverter> typeConverters = new HashMap<>();

    public static <ParamClass> void register(TestTypeConverter converter,Class<ParamClass> paramType){
        register(converter, paramType.getName());
    }
    public static <ParamClass> void register(TestTypeConverter converter,String paramType){
        typeConverters.put(paramType,converter);
    }

    public static <ParamClass> ParamClass getParam (String paramName,Map<String,Object> context,Class<ParamClass> paramType){
        return getParam(paramName,context,paramType,false);
    }

    public static <ParamClass> ParamClass getParam (String paramName,Map<String,Object> context,Class<ParamClass> paramType,boolean isOptional){
        Object param = context.get(paramName);
        if (param == null){
            if(isOptional){
                return null;
            }
            throw new RuntimeException(String.format("expecting parameter named \"%s\" in context",paramName));
        }
        if(param.getClass().isAssignableFrom(paramType)){
            return (ParamClass)param;
        }
        return getParam(paramName,context,paramType.getName(),isOptional);
    }

    public static <ParamClass> ParamClass getParam (String paramName,Map<String,Object> context,String paramType,boolean isOptional){
        Object param = context.get(paramName);
        if (param == null){
            if(isOptional){
                return null;
            }
            throw new RuntimeException(String.format("expecting parameter named \"%s\" in context",paramName));
        }

        TestTypeConverter converter = typeConverters.get(paramType);
        if(converter == null){
            throw new RuntimeException(String.format("Unable to convert parameter \"%s\" value \"%s\" to type \"%s\"",paramName,param,paramType));
        }
        return (ParamClass) converter.convert(param + "");

    }

    void configure(String operatorName, Map<String, Object> config);

    public interface TestTypeConverter{
        public Object convert(String param);
    }
}


