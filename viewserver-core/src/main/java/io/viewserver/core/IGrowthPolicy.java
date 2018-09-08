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

package io.viewserver.core;

/**
 * Created by bemm on 26/09/2014.
 */
public interface IGrowthPolicy {
    int getNewSize(int currentSize, int minimumNewSize);

    public static final IGrowthPolicy DoubleGrowth = new DoubleGrowthPolicy();
    public static final IGrowthPolicy MinimumGrowth = new MinimumGrowthPolicy();
    public static final IGrowthPolicy TenPercentGrowth = new TenPercentGrowthPolicy();

    static final class DoubleGrowthPolicy implements IGrowthPolicy {
        @Override
        public int getNewSize(int currentSize, int minimumNewSize) {
            int newSize = currentSize;
            while (newSize < minimumNewSize) {
                newSize <<= 1;
            }
            return newSize;
        }
    }


    static final class TenPercentGrowthPolicy implements IGrowthPolicy {
        @Override
        public int getNewSize(int currentSize, int minimumNewSize) {
            return minimumNewSize + (currentSize / 10);
        }
    }

    static final class MinimumGrowthPolicy implements IGrowthPolicy {
        @Override
        public int getNewSize(int currentSize, int minimumNewSize) {
            return minimumNewSize;
        }
    }
}
