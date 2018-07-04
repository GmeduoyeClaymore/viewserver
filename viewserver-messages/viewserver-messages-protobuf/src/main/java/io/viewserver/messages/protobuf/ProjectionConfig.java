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

import io.viewserver.messages.PoolableMessage;
import io.viewserver.messages.config.IProjectionConfig;
import io.viewserver.messages.protobuf.dto.ProjectionConfigMessage;

import java.util.List;

/**
 * Created by bemm on 08/12/15.
 */
public class ProjectionConfig extends PoolableMessage<ProjectionConfig> implements IProjectionConfig<ProjectionConfig> {
    private ProjectionConfigMessage.ProjectionConfigDtoOrBuilder projectionConfigDto;
    private RecyclingList<IProjectionColumn, ProjectionConfigMessage.ProjectionConfigDto.ProjectionColumn> projectionColumns;

    ProjectionConfig() {
        super(IProjectionConfig.class);
    }

    @Override
    public void setDto(Object dto) {
        projectionConfigDto = (ProjectionConfigMessage.ProjectionConfigDtoOrBuilder) dto;
    }

    @Override
    public Object getDto() {
        return getBuilder();
    }

    @Override
    public ProjectionMode getMode() {
        final ProjectionConfigMessage.ProjectionConfigDto.Mode mode = projectionConfigDto.getMode();
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
                throw new UnsupportedOperationException(String.format("Unknown projection mode '%s'", mode));
            }
        }
    }

    @Override
    public IProjectionConfig<ProjectionConfig> setMode(ProjectionMode mode) {
        switch (mode) {
            case Projection: {
                getProjectionConfigDtoBuilder().setMode(ProjectionConfigMessage.ProjectionConfigDto.Mode.Projection);
                break;
            }
            case Inclusionary: {
                getProjectionConfigDtoBuilder().setMode(ProjectionConfigMessage.ProjectionConfigDto.Mode.Inclusionary);
                break;
            }
            case Exclusionary: {
                getProjectionConfigDtoBuilder().setMode(ProjectionConfigMessage.ProjectionConfigDto.Mode.Exclusionary);
                break;
            }
            default: {
                throw new IllegalArgumentException(String.format("Unknown projection mode '%s'", mode));
            }
        }
        return this;
    }

    @Override
    public List<IProjectionColumn> getProjectionColumns() {
        if (projectionColumns == null) {
            projectionColumns = new RecyclingList<IProjectionColumn, ProjectionConfigMessage.ProjectionConfigDto.ProjectionColumn>(
                    IProjectionColumn.class
            ) {
                @Override
                protected void doAdd(Object dto) {
                    final ProjectionConfigMessage.ProjectionConfigDto.Builder builder = getProjectionConfigDtoBuilder();
                    dtoList = builder.getProjectionsList();
                    if (dto instanceof ProjectionConfigMessage.ProjectionConfigDto.ProjectionColumn) {
                        builder.addProjections((ProjectionConfigMessage.ProjectionConfigDto.ProjectionColumn) dto);
                    } else {
                        builder.addProjections((ProjectionConfigMessage.ProjectionConfigDto.ProjectionColumn.Builder) dto);
                    }
                }

                @Override
                protected void doClear() {
                    getProjectionConfigDtoBuilder().clearProjections();
                }
            };
        }
        projectionColumns.setDtoList(projectionConfigDto != null ? projectionConfigDto.getProjectionsList() : null);
        return projectionColumns;
    }

    @Override
    protected void doRelease() {
        if (projectionColumns != null) {
            projectionColumns.release();
        }
        projectionConfigDto = null;
    }

    ProjectionConfigMessage.ProjectionConfigDto.Builder getBuilder() {
        return getProjectionConfigDtoBuilder();
    }

    private ProjectionConfigMessage.ProjectionConfigDto.Builder getProjectionConfigDtoBuilder() {
        if (projectionConfigDto == null) {
            projectionConfigDto = ProjectionConfigMessage.ProjectionConfigDto.newBuilder();
        } else if (projectionConfigDto instanceof ProjectionConfigMessage.ProjectionConfigDto) {
            projectionConfigDto = ((ProjectionConfigMessage.ProjectionConfigDto) projectionConfigDto).toBuilder();
        }
        return (ProjectionConfigMessage.ProjectionConfigDto.Builder) projectionConfigDto;
    }

    public static class ProjectionColumn extends PoolableMessage<ProjectionColumn> implements IProjectionColumn<ProjectionColumn> {
        private ProjectionConfigMessage.ProjectionConfigDto.ProjectionColumnOrBuilder projectionColumnDto;
        private ListWrapper<String> outboundNamesList;

        ProjectionColumn() {
            super(IProjectionColumn.class);
        }

        @Override
        public void setDto(Object dto) {
            projectionColumnDto = (ProjectionConfigMessage.ProjectionConfigDto.ProjectionColumnOrBuilder) dto;
        }

        @Override
        public Object getDto() {
            return getBuilder();
        }

        @Override
        public String getInboundName() {
            return projectionColumnDto.getInboundName();
        }

        @Override
        public IProjectionColumn<ProjectionColumn> setInboundName(String inboundName) {
            getProjectionColumnDtoBuilder().setInboundName(inboundName);
            return this;
        }

        @Override
        public boolean isRegex() {
            return projectionColumnDto.getIsRegex();
        }

        @Override
        public IProjectionColumn<ProjectionColumn> setIsRegex(boolean isRegex) {
            getProjectionColumnDtoBuilder().setIsRegex(isRegex);
            return this;
        }

        @Override
        public List<String> getOutboundNames() {
            if (outboundNamesList == null) {
                outboundNamesList = new ListWrapper<>(x -> {
                            final ProjectionConfigMessage.ProjectionConfigDto.ProjectionColumn.Builder builder = getProjectionColumnDtoBuilder();
                            outboundNamesList.setInnerList(builder.getOutboundNameList());
                            builder.addOutboundName(x);
                        });
            }
            outboundNamesList.setInnerList(projectionColumnDto.getOutboundNameList());
            return outboundNamesList;
        }

        @Override
        protected void doRelease() {
            if (outboundNamesList != null) {
                outboundNamesList.setInnerList(null);
            }
            projectionColumnDto = null;
        }

        ProjectionConfigMessage.ProjectionConfigDto.ProjectionColumn.Builder getBuilder() {
            return getProjectionColumnDtoBuilder();
        }

        private ProjectionConfigMessage.ProjectionConfigDto.ProjectionColumn.Builder getProjectionColumnDtoBuilder() {
            if (projectionColumnDto == null) {
                projectionColumnDto = ProjectionConfigMessage.ProjectionConfigDto.ProjectionColumn.newBuilder();
            } else if (projectionColumnDto instanceof ProjectionConfigMessage.ProjectionConfigDto.ProjectionColumn) {
                projectionColumnDto = ((ProjectionConfigMessage.ProjectionConfigDto.ProjectionColumn) projectionColumnDto).toBuilder();
            }
            return (ProjectionConfigMessage.ProjectionConfigDto.ProjectionColumn.Builder) projectionColumnDto;
        }
    }
}
