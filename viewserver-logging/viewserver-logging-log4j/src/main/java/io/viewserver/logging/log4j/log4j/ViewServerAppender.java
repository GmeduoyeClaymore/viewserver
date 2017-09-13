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

import org.apache.logging.log4j.LoggingException;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by nick on 29/09/15.
 */
@Plugin(name = "ViewServer", category = "Core", elementType = "appender")
public class ViewServerAppender extends AbstractAppender {
    private final ViewServerManager manager;
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock;

    private ViewServerAppender(String name, Filter filter, ViewServerManager manager) {
        super(name, filter, null);
        this.readLock = this.rwLock.readLock();
        this.manager = manager;
    }

    @PluginFactory
    public static ViewServerAppender createAppender(@PluginAttribute("name") String name,
                                                    @PluginAttribute("url") String url,
                                                    @PluginAttribute("tableName") String tableName,
                                                    @PluginAttribute("rolloverSize") String rolloverSize,
                                                    @PluginElement("Filters") Filter filter,
                                                    @PluginElement("VSColumn") final ColumnConfig[] columnConfigs) {
        if (name == null) {
            LOGGER.error("No name provided for ViewServerAppender");
            return null;
        }

        if (url == null || "".equals(url)) {
            LOGGER.error("No URL provided for ViewServerAppender");
            return null;
        }

        if (tableName == null || "".equals(tableName)) {
            LOGGER.error("No table name provided for ViewServerAppender");
            return null;
        }

        int rolloverSizeInt = AbstractAppender.parseInt(rolloverSize, -1);

        ViewServerManager manager = ViewServerManager.getManager(url, tableName, columnConfigs, rolloverSizeInt);
        if (manager == null) {
            return null;
        }

        return new ViewServerAppender(name, filter, manager);
    }

    @Override
    public void append(LogEvent logEvent) {
        this.readLock.lock();

        try {
            this.manager.write(logEvent);
        } catch (LoggingException ex) {
            LOGGER.error("Unable to write to viewserver [{}] for appender [{}].", new Object[]{this.manager.getName(), this.getName(), ex});
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unable to write to viewserver [{}] for appender [{}].", new Object[]{this.manager.getName(), this.getName(), ex});
            throw new AppenderLoggingException("Unable to write to database in appender: " + ex.getMessage(), ex);
        } finally {
            this.readLock.unlock();
        }
    }
}
