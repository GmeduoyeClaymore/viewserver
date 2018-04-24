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

package io.viewserver.operators.union;

import io.viewserver.configurator.IInputConfig;

/**
 * Created by bemm on 31/10/2014.
 */
public interface IUnionConfig extends IInputConfig {
    Input[] getInputs();

    public static class Input {
        private String name;
        private int sourceId;

        public Input(String name, int sourceId) {
            this.name = name;
            this.sourceId = sourceId;
        }

        public String getName() {
            return name;
        }

        public int getSourceId() {
            return sourceId;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setSourceId(int sourceId) {
            this.sourceId = sourceId;
        }

        @Override
        public String toString(){
            return String.format("%s:%d", name, sourceId);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Input input = (Input) o;

            if (sourceId != input.sourceId) return false;
            if (!name.equals(input.name)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + sourceId;
            return result;
        }
    }
}
