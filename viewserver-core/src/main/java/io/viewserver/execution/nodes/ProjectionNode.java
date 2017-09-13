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

package io.viewserver.execution.nodes;

import io.viewserver.execution.ParameterHelper;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.config.IOperatorConfig;
import io.viewserver.operators.projection.IProjectionConfig;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by paulg on 12/11/2014.
 */
public class ProjectionNode extends GraphNodeBase<ProjectionNode> {
    private IProjectionConfig.ProjectionMode mode;
    private List<IProjectionConfig.ProjectionColumn> projectionColumns = new ArrayList<>();

    public ProjectionNode() {
        super();
    }

    public ProjectionNode(String name) {
        super(name, "Projection");
    }

    public ProjectionNode withMode(IProjectionConfig.ProjectionMode mode) {
        this.mode = mode;
        return this;
    }

    public ProjectionNode withProjectionColumns(IProjectionConfig.ProjectionColumn... projectionColumns) {
        this.projectionColumns.addAll(Arrays.asList(projectionColumns));
        return this;
    }

    public ProjectionNode withProjectionColumns(Collection<IProjectionConfig.ProjectionColumn> projectionColumns) {
        this.projectionColumns.addAll(projectionColumns);
        return this;
    }

    @Override
    public Object getConfig(ParameterHelper parameterHelper) {
        return new IProjectionConfig() {
            @Override
            public ProjectionMode getMode() {
                return mode;
            }

            @Override
            public Collection<ProjectionColumn> getProjectionColumns() {
                return projectionColumns;
            }
        };
    }

    @Override
    protected IOperatorConfig getConfigDto(ParameterHelper parameterHelper) {
        final io.viewserver.messages.config.IProjectionConfig projectionConfig = MessagePool.getInstance().get(io.viewserver.messages.config.IProjectionConfig.class)
                .setMode(getDtoMode());
        final List<io.viewserver.messages.config.IProjectionConfig.IProjectionColumn> projectionColumnMessages = projectionConfig.getProjectionColumns();
        int count = projectionColumns.size();
        for (int i = 0; i < count; i++) {
            final IProjectionConfig.ProjectionColumn projectionColumn = projectionColumns.get(i);
            final io.viewserver.messages.config.IProjectionConfig.IProjectionColumn projectionColumnMessage = MessagePool.getInstance().get(io.viewserver.messages.config.IProjectionConfig.IProjectionColumn.class)
                    .setInboundName(projectionColumn.getInboundName())
                    .setIsRegex(projectionColumn.isRegex());
            projectionColumnMessage.getOutboundNames().addAll(projectionColumn.getOutboundNames());
            projectionColumnMessages.add(projectionColumnMessage);
            projectionColumnMessage.release();
        }
        return projectionConfig;
    }

    private io.viewserver.messages.config.IProjectionConfig.ProjectionMode getDtoMode() {
        switch (mode) {
            case Projection: {
                return io.viewserver.messages.config.IProjectionConfig.ProjectionMode.Projection;
            }
            case Inclusionary: {
                return io.viewserver.messages.config.IProjectionConfig.ProjectionMode.Inclusionary;
            }
            case Exclusionary: {
                return io.viewserver.messages.config.IProjectionConfig.ProjectionMode.Exclusionary;
            }
            default: {
                throw new IllegalArgumentException("Unknown projection mode '" + mode + "'");
            }
        }
    }

    @Override
    protected String getConfigForOperatorName(ParameterHelper parameterHelper) {
        ArrayList<String> configStrings = new ArrayList<>();

        for (IProjectionConfig.ProjectionColumn projectionCol : projectionColumns) {
            configStrings.add(String.format("%s:%s:%s",
                    parameterHelper.substituteParameterValues(projectionCol.getInboundName()),
                    projectionCol.isRegex(),
                    parameterHelper.substituteParameterValues(StringUtils.join(projectionCol.getOutboundNames(), ","))));
        }

        return String.format("projection:%s;%s", mode, StringUtils.join(configStrings, ','));
    }

    public IProjectionConfig.ProjectionMode getMode() {
        return mode;
    }

    public void setMode(IProjectionConfig.ProjectionMode mode) {
        this.mode = mode;
    }

    public List<IProjectionConfig.ProjectionColumn> getProjectionColumns() {
        return projectionColumns;
    }

    public void setProjectionColumns(List<IProjectionConfig.ProjectionColumn> projectionColumns) {
        this.projectionColumns = projectionColumns;
    }
}
