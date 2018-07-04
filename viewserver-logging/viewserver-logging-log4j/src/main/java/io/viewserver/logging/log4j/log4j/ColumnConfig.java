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

package io.viewserver.logging.log4j.log4j;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;

/**
 * Created by bemm on 30/09/15.
 */
@Plugin(name = "VSColumn", category = "Core", printObject = true)
public class ColumnConfig {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final String name;
    private final PatternLayout layout;
    private final String literalValue;
    private final boolean eventTimestamp;

    private ColumnConfig(String name, PatternLayout layout, String literalValue, boolean eventTimestamp) {
        this.name = name;
        this.layout = layout;
        this.literalValue = literalValue;
        this.eventTimestamp = eventTimestamp;
    }

    public String getName() {
        return name;
    }

    public PatternLayout getLayout() {
        return layout;
    }

    public String getLiteralValue() {
        return literalValue;
    }

    public boolean isEventTimestamp() {
        return eventTimestamp;
    }

    @Override
    public String toString() {
        return "ColumnConfig{" +
                "name='" + name + '\'' +
                ", layout=" + layout +
                ", literalValue='" + literalValue + '\'' +
                ", eventTimestamp=" + eventTimestamp +
                '}';
    }

    @PluginFactory
    public static ColumnConfig createColumnConfig(@PluginConfiguration final Configuration config,
                                                  @PluginAttribute("name") final String name,
                                                  @PluginAttribute("pattern") final String pattern,
                                                  @PluginAttribute("literal") final String literalValue,
                                                  @PluginAttribute("isEventTimestamp") final String eventTimestamp) {
        if (Strings.isEmpty(name)) {
            LOGGER.error("Invalid column config - missing 'name' attribute");
            return null;
        }

        final boolean isPattern = Strings.isNotEmpty(pattern);
        final boolean isLiteralValue = Strings.isNotEmpty(literalValue);
        final boolean isEventTimestamp = Boolean.parseBoolean(eventTimestamp);

        if ((isPattern && isLiteralValue) || (isPattern && isEventTimestamp) || (isLiteralValue && isEventTimestamp)) {
            LOGGER.error("The pattern, literal, and isEventTimestamp attributes are mutually exclusive.");
            return null;
        }

        if (isEventTimestamp) {
            return new ColumnConfig(name, null, null, true);
        }
        if (isLiteralValue) {
            return new ColumnConfig(name, null, literalValue, false);
        }
        if (isPattern) {
            final PatternLayout layout =
                    PatternLayout.newBuilder()
                            .withPattern(pattern)
                            .withConfiguration(config)
                            .withAlwaysWriteExceptions(false)
                            .build();
            return new ColumnConfig(name, layout, null, false);
        }

        LOGGER.error("To configure a column you must specify a pattern or literal or set isEventDate to true.");
        return null;
    }
}
