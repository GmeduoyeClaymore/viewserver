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

package io.viewserver.messages.protobuf;

import io.viewserver.messages.MessagePool;
import com.google.protobuf.AbstractMessageLite;
import com.google.protobuf.ByteString;
import io.viewserver.messages.config.*;
import io.viewserver.messages.protobuf.dto.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bemm on 08/12/15.
 */
public class OperatorConfigCoder {
    public static final OperatorConfigCoder INSTANCE = new OperatorConfigCoder();
    private Map<Class<? extends IOperatorConfig>, Class<? extends AbstractMessageLite>> dtoRegistry = new HashMap<>();

    private OperatorConfigCoder() {
        dtoRegistry.put(IFilterConfig.class, FilterConfigMessage.FilterConfigDto.class);
        dtoRegistry.put(IProjectionConfig.class, ProjectionConfigMessage.ProjectionConfigDto.class);
        dtoRegistry.put(IGroupByConfig.class, GroupByConfigMessage.GroupByConfigDto.class);
        dtoRegistry.put(ICalcColConfig.class, CalcColConfigMessage.CalcColConfigDto.class);
        dtoRegistry.put(ISortConfig.class, SortConfigMessage.SortConfigDto.class);
        dtoRegistry.put(IIndexConfig.class, IndexConfigMessage.IndexConfigDto.class);
        dtoRegistry.put(ITransposeConfig.class, TransposeConfigMessage.TransposeConfigDto.class);
        dtoRegistry.put(IRollingTableConfig.class, RollingTableConfigMessage.RollingTableConfigDto.class);
    }

    public ByteString encode(IOperatorConfig config) {
        return ((AbstractMessageLite.Builder)config.getDto()).buildPartial().toByteString();
    }

    public <T extends IOperatorConfig> T decode(ByteString bytes, Class<T> clazz) {
        final Class<? extends AbstractMessageLite> dtoClass = dtoRegistry.get(clazz);
        try {
            Method parseMethod = dtoClass.getMethod("parseFrom", ByteString.class);

            Object dto = parseMethod.invoke(null, bytes);
            final T message = MessagePool.getInstance().get(clazz);
            message.setDto(dto);
            return message;
        } catch (NoSuchMethodException e) {
            String message = String.format("Failed to find static method parseFrom() on %s",
                    ((Class)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0]).getName());
            throw new RuntimeException(message, e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            String message = String.format("Failed to invoke parseFrom() method on %s",
                    ((Class)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0]).getName());
            throw new RuntimeException(message, e);
        }
    }
}
