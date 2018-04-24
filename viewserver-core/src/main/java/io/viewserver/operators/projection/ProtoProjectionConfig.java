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
import java.util.Collection;
import java.util.List;

/**
 * Created by bemm on 25/11/2014.
 */
public class ProtoProjectionConfig implements IProjectionConfig {
    private final ProjectionMode mode;
    private List<ProjectionColumn> projectionColumns = new ArrayList<>();

    public ProtoProjectionConfig(io.viewserver.messages.config.IProjectionConfig projectionConfigDto) {
        this.mode = getProjectionMode(projectionConfigDto.getMode());
        final List<io.viewserver.messages.config.IProjectionConfig.IProjectionColumn> projectionColumns = projectionConfigDto.getProjectionColumns();
        int count = projectionColumns.size();
        for (int i = 0; i < count; i++) {
            final io.viewserver.messages.config.IProjectionConfig.IProjectionColumn projectionColumn = projectionColumns.get(i);
            this.projectionColumns.add(new ProjectionColumn(
                    projectionColumn.getInboundName(),
                    projectionColumn.isRegex(),
                    projectionColumn.getOutboundNames()));
            projectionColumn.release();
        }
    }

    @Override
    public ProjectionMode getMode() {
        return mode;
    }

    private ProjectionMode getProjectionMode(io.viewserver.messages.config.IProjectionConfig.ProjectionMode mode) {
        switch (mode) {
            case Projection: {
                return ProjectionMode.Projection;
            }
            case Inclusionary: {
                return ProjectionMode.Inclusionary;
            }
            case Exclusionary: {
                return ProjectionMode.Exclusionary;
            }
            default: {
                throw new IllegalArgumentException("Unknown projection mode '" + mode + "'");
            }
        }
    }

    @Override
    public Collection<ProjectionColumn> getProjectionColumns() {
        return projectionColumns;
    }
}
