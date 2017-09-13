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

package io.viewserver.server.setup;

import io.viewserver.core.IJsonSerialiser;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.core.Utils;
import io.viewserver.datasource.DataSource;
import io.viewserver.report.ReportDefinition;
import io.viewserver.server.IViewServerMasterConfiguration;
import com.google.common.io.Resources;
import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nick on 10/09/15.
 */
public abstract class BootstrapperBase implements IBootstrapper {
    private static final Logger log = LoggerFactory.getLogger(BootstrapperBase.class);
    private IJsonSerialiser serialiser = new JacksonSerialiser();
    public static void bootstrap(IViewServerMasterConfiguration configuration) {
        Class<?> bootstrapperClass;
        String bootstrapperClassName = configuration.getBootstrapperClass();
        try {
            bootstrapperClass = Class.forName(bootstrapperClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not find bootstrapper class " + bootstrapperClassName, e);
        }

        Constructor<?> constructor;
        try {
            constructor = bootstrapperClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not find default constructor for bootstrapper class " + bootstrapperClass.getName(), e);
        }

        IBootstrapper bootstrapper;
        try {
            bootstrapper = (IBootstrapper) constructor.newInstance();
        } catch (Throwable e) {
            throw new RuntimeException("Could not create bootstrapper " + bootstrapperClass.getName(), e);
        }

        bootstrapper.run(configuration);
    }

    protected IJsonSerialiser getSerialiser() {
        return serialiser;
    }

    @Override
    public void run(IViewServerMasterConfiguration configuration) {
        log.info("Bootstrapping local database");

        String path = Utils.replaceSystemTokens(configuration.getMasterDatabasePath());
        log.info("Using database at {}", path);
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(String.format("jdbc:h2:%s;MV_STORE=FALSE;DATABASE_TO_UPPER=FALSE", path));
        try (Connection connection = dataSource.getConnection()) {
            setupDatabase(connection);
        } catch (Throwable e) {
            throw new RuntimeException("Could not run database setup script", e);
        }
    }

    protected void setupDatabase(Connection connection) {
        setupDatabaseObjects(connection);
        setupDataSources(connection);
        setupReports(connection);
    }

    private void setupDatabaseObjects(Connection connection) {
        log.info("Creating database objects");
        ArrayList<String> scripts = new ArrayList<String>();
        try {
            scripts.add(Resources.toString(Resources.getResource(BootstrapperBase.class, "SetupDatabase.sql"), Charset.forName("utf-8")));
        } catch (IOException e) {
            throw new RuntimeException("Could not read database setup script", e);
        }
        scripts.addAll(getDatabaseSetupScripts());
        for (String script : scripts) {
            try (PreparedStatement statement = connection.prepareStatement(script)) {
                statement.execute();
            } catch (Throwable e) {
                throw new RuntimeException("Could not run database setup script", e);
            }
        }
    }

    protected Collection<String> getDatabaseSetupScripts() {
        return new ArrayList<>();
    }

    private void setupDataSources(Connection connection) {
        log.info("Creating data sources");
        Collection<DataSource> dataSources = getDataSources();
        try (PreparedStatement deleteStatement = connection.prepareStatement("delete from datasources where name = ?")) {
            try (PreparedStatement insertStatement = connection.prepareStatement("insert into datasources (name, json) values (?, ?)")) {
                for (DataSource dataSource : dataSources) {
                    log.debug("-    {}", dataSource.getName());
                    try {
                        deleteStatement.setNString(1, dataSource.getName());
                        deleteStatement.execute();

                        String json = serialiser.serialise(dataSource, true);
                        NClob jsonClob = connection.createNClob();
                        jsonClob.setString(1, json);
                        System.out.println(json);
                        insertStatement.setNString(1, dataSource.getName());
                        insertStatement.setNClob(2, jsonClob);
                        insertStatement.execute();
                    } catch (SQLException e) {
                        log.error(String.format("Could not create data source '%s'", dataSource.getName()), e);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Could not create data sources", e);
        }
    }

    protected Collection<DataSource> getDataSources() {
        return new ArrayList<>();
    }

    private void setupReports(Connection connection) {
        log.info("Creating report definitions");

        Map<String, ReportDefinition> reportDefinitions = getReportDefinitions();
        try (PreparedStatement deleteStatement = connection.prepareStatement("delete from reports where id = ?")) {
            try (PreparedStatement insertStatement = connection.prepareStatement("insert into reports (id, name, dataSource, json) values (?, ?, ?, ?)")) {
                for (ReportDefinition reportDefinition : reportDefinitions.values()) {
                    log.debug("-    {}", reportDefinition.getId());
                    try {
                        deleteStatement.setNString(1, reportDefinition.getId());
                        deleteStatement.execute();

                        String json = serialiser.serialise(reportDefinition, true);
                        NClob jsonClob = connection.createNClob();
                        jsonClob.setString(1, json);
                        log.debug(json);
                        insertStatement.setNString(1, reportDefinition.getId());
                        insertStatement.setNString(2, reportDefinition.getName());
                        insertStatement.setNString(3, reportDefinition.getDataSource());
                        insertStatement.setNClob(4, jsonClob);
                        insertStatement.execute();
                    } catch (SQLException e) {
                        log.error(String.format("Could not create data source '%s'", reportDefinition.getId()), e);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Could not create report definitions", e);
        }
    }

    protected Map<String, ReportDefinition> getReportDefinitions() {
        return new HashMap<>();
    }
}
