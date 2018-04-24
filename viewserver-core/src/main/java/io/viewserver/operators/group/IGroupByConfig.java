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

package io.viewserver.operators.group;

import java.util.Arrays;
import java.util.List;

/**
 * Created by bemm on 21/10/2014.
 */
public interface IGroupByConfig {
    List<String> getGroupBy();
    List<Summary> getSummaries();
    String getCountColumnName();
    List<String> getSubtotals();

    public static class Summary {
        private String name;
        private String function;
        private String target;
        private boolean isRegex;
        private Object[] arguments;

        public Summary(String name, String function) {
            this.name = name;
            this.function = function;
        }

        public Summary(String name, String function, Object[] arguments) {
            this.name = name;
            this.function = function;
            this.arguments = arguments;
        }

        public Summary(String name, String function, String target) {
            this.name = name;
            this.function = function;
            this.target = target;
        }

        public Summary(String name, String function, String target, Object[] arguments) {
            this.name = name;
            this.function = function;
            this.target = target;
            this.arguments = arguments;
        }

        public Summary(String name, String function, String target, boolean isRegex) {
            this.name = name;
            this.function = function;
            this.target = target;
            this.isRegex = isRegex;
        }

        public Summary(String name, String function, String target, boolean isRegex, Object[] arguments) {
            this.name = name;
            this.function = function;
            this.target = target;
            this.isRegex = isRegex;
            this.arguments = arguments;
        }

        public String getName() {
            return name;
        }

        public String getFunction() {
            return function;
        }

        public Object[] getArguments() {
            return arguments;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Summary summary = (Summary) o;

            if (isRegex != summary.isRegex) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            if (!Arrays.equals(arguments, summary.arguments)) return false;
            if (function != null ? !function.equals(summary.function) : summary.function != null) return false;
            if (name != null ? !name.equals(summary.name) : summary.name != null) return false;
            if (target != null ? !target.equals(summary.target) : summary.target != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (function != null ? function.hashCode() : 0);
            result = 31 * result + (target != null ? target.hashCode() : 0);
            result = 31 * result + (isRegex ? 1 : 0);
            result = 31 * result + (arguments != null ? Arrays.hashCode(arguments) : 0);
            return result;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public boolean isRegex() {
            return isRegex;
        }

        public void setRegex(boolean isRegex) {
            this.isRegex = isRegex;
        }
    }
}
