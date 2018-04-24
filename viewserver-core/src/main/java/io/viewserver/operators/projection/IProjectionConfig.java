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

package io.viewserver.operators.projection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by bemm on 31/10/2014.
 */
public interface IProjectionConfig {
    ProjectionMode getMode();
    Collection<ProjectionColumn> getProjectionColumns();

    static IProjectionConfig fromDto(io.viewserver.messages.config.IProjectionConfig configDto) {
        return new IProjectionConfig() {
            @Override
            public ProjectionMode getMode() {
                return null;
            }

            @Override
            public Collection<ProjectionColumn> getProjectionColumns() {
                return null;
            }
        };
    }

    enum ProjectionMode {
        Projection,
        Inclusionary,
        Exclusionary
    }

    class ProjectionColumn {
        private String inboundName;
        private boolean isRegex;
        private List<String> outboundNames = new ArrayList<>();

        public ProjectionColumn() {
        }

        public ProjectionColumn(String inboundName) {
            this.inboundName = inboundName;
        }

        public ProjectionColumn(String inboundName, String... outboundNames) {
            this.inboundName = inboundName;
            int count = outboundNames.length;
            for (int i = 0; i < count; i++) {
                if (outboundNames[i] != null) {
                    this.outboundNames.add(outboundNames[i]);
                }
            }
        }

        public ProjectionColumn(String inboundName, boolean isRegex) {
            this.inboundName = inboundName;
            this.isRegex = isRegex;
        }

        public ProjectionColumn(String inboundNameRegex, boolean isRegex, String... outboundNames) {
            this.inboundName = inboundNameRegex;
            this.isRegex = isRegex;
            this.outboundNames.addAll(Arrays.asList(outboundNames));
        }

        public ProjectionColumn(String inboundNameRegex, boolean isRegex, List<String> outboundNames) {
            this.inboundName = inboundNameRegex;
            this.isRegex = isRegex;
            this.outboundNames.addAll(outboundNames);
        }

        public String getInboundName() {
            return inboundName;
        }

        public boolean isRegex() {
            return isRegex;
        }

        public List<String> getOutboundNames() {
            return outboundNames;
        }

        public void setInboundName(String inboundName) {
            this.inboundName = inboundName;
        }

        public void setRegex(boolean isRegex) {
            this.isRegex = isRegex;
        }

        public void setOutboundNames(List<String> outboundName) {
            this.outboundNames = outboundName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ProjectionColumn that = (ProjectionColumn) o;

            if (isRegex != that.isRegex) return false;
            if (!inboundName.equals(that.inboundName)) return false;
            return outboundNames.equals(that.outboundNames);

        }

        @Override
        public int hashCode() {
            int result = inboundName.hashCode();
            result = 31 * result + (isRegex ? 1 : 0);
            result = 31 * result + outboundNames.hashCode();
            return result;
        }

        public void addOutboundName(String outboundName) {
            outboundNames.add(outboundName);
        }
    }
}
