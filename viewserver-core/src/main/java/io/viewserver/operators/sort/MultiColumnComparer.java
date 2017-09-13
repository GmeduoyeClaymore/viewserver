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

package io.viewserver.operators.sort;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nickc on 21/01/2015.
 */
public class MultiColumnComparer implements IComparer {
    private final List<IComparer> columnComparers = new ArrayList<>();

    public void addColumnComparer(IComparer columnComparer) {
        columnComparers.add(columnComparer);
    }

    @Override
    public int compare(int row) {
        throw new UnsupportedOperationException("Not implemented as shouldn't be needed");
    }

    @Override
    public void setPivotValue(int row) {
        throw new UnsupportedOperationException("Not implemented as shouldn't be needed");
    }

    @Override
    public int compare(int row1, boolean usePreviousValue1, int row2, boolean usePreviousValue2) {
        for (IComparer columnComparer : columnComparers) {
            int result = columnComparer.compare(row1, usePreviousValue1, row2, usePreviousValue2);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }
}
