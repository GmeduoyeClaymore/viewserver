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

package io.viewserver.datasource;

import io.viewserver.core.NullableBool;
import io.viewserver.core._KeyType_;

/**
 * Created by nick on 02/07/15.
 */
public interface IDimensionMapper {

    LookupKey registerDimension(String dimensionNamespace, Dimension dimension);

    LookupKey registerDimension(String dimensionNamespace, String dimensionName, ContentType dimensionContentType);

    String lookupString(String dimensionNamespace, String dimensionName, int id);

    byte lookupByte(String dimensionNamespace, String dimensionName, int id);

    boolean lookupBool(String dimensionNamespace, String dimensionName, int id);

    NullableBool lookupNullableBool(String dimensionNamespace, String dimensionName, int id);

    short lookupShort(String dimensionNamespace, String dimensionName, int id);

    int lookupInt(String dimensionNamespace, String dimensionName, int id);

    long lookupLong(String dimensionNamespace, String dimensionName, int id);

    _KeyType_ lookup_KeyName_(String dimensionNamespace, String dimensionName, int id);

    void clear();
}


