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

import io.viewserver.messages.command.IUpdateDimensionMapCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by nick on 02/07/15.
 */
public class DimensionMapUpdater {
    private static final Logger log = LoggerFactory.getLogger(DimensionMapUpdater.class);
    private SlaveDimensionMapper dimensionMapper;
    private IDataSourceRegistry dataSourceRegistry;

    public DimensionMapUpdater(SlaveDimensionMapper dimensionMapper, IDataSourceRegistry dataSourceRegistry) {
        this.dimensionMapper = dimensionMapper;
        this.dataSourceRegistry = dataSourceRegistry;
    }

    public void update(IUpdateDimensionMapCommand updateCommand) {
        List<IUpdateDimensionMapCommand.IDataSource> dataSources = updateCommand.getDataSources();
        int dataSourceCount = dataSources.size();
        for (int i = 0; i < dataSourceCount; i++) {
            IUpdateDimensionMapCommand.IDataSource dataSourceDto = dataSources.get(i);
            IDataSource dataSource = dataSourceRegistry.get(dataSourceDto.getName());
            if (dataSource == null) {
                log.warn("Unknown data source '{}' in map update message", dataSourceDto.getName());
                continue;
            }
            List<IUpdateDimensionMapCommand.IDimension> dimensions = dataSourceDto.getDimensions();
            int dimensionCount = dimensions.size();
            for (int j = 0; j < dimensionCount; j++) {
                IUpdateDimensionMapCommand.IDimension dimensionDto = dimensions.get(j);
                Dimension dimension = dataSource.getDimension(dimensionDto.getName());
                if (dimension == null) {
                    log.warn("Unknown dimension '{}' for data source '{}' in map update message", dimensionDto.getName(), dataSource.getName());
                    continue;
                }
                List<IUpdateDimensionMapCommand.IMapping> mappings = dimensionDto.getMappings();
                int mappingCount = mappings.size();
                for (int k = 0; k < mappingCount; k++) {
                    IUpdateDimensionMapCommand.IMapping mappingDto = mappings.get(k);
                    int id = mappingDto.getId();
                    switch (dimension.getType()) {
                        case Byte: {
                            dimensionMapper.mapByte(dataSource, dimension, (byte) mappingDto.getIntegerValue(), id);
                            break;
                        }
                        case Short: {
                            dimensionMapper.mapShort(dataSource, dimension, (short)mappingDto.getIntegerValue(), id);
                            break;
                        }
                        case Int: {
                            dimensionMapper.mapInt(dataSource, dimension, mappingDto.getIntegerValue(), id);
                            break;
                        }
                        case Long: {
                            dimensionMapper.mapLong(dataSource, dimension, mappingDto.getLongValue(), id);
                            break;
                        }
                        case String: {
                            dimensionMapper.mapString(dataSource, dimension, mappingDto.getStringValue(), id);
                            break;
                        }
                        default: {
                            log.warn("Unsupported dimension type '{}' for dimension '{}' in map update message", dimension.getType(), dimension.getName());
                        }
                    }
                    mappingDto.release();
                }
                dimensionDto.release();
            }
            dataSourceDto.release();
        }

    }
}
