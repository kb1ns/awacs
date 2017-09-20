/**
 * Copyright 2016-2017 AWACS Project.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.awacs.component.influxdb;

import io.awacs.common.Configurable;
import io.awacs.common.Configuration;
import io.awacs.common.InitializationException;
import io.awacs.common.Releasable;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

import java.util.List;

/**
 * Created by pixyonly on 03/09/2017.
 */
public class InfluxdbComponent implements Configurable, Releasable {

    private static final String CFG_URL = "url";

    private static final String CFG_USERNAME = "username";

    private static final String CFG_PASSWORD = "password";

    private static final String CFG_DATABASE = "database";

    private static final String CFG_RETENTION_POLICY = "retention_policy";

    private static final String CFG_CONSISTENCY_LEVEL = "consistency_level";

    private static final InfluxDB.ConsistencyLevel DEFAULT_CONSISTENCY_LEVEL = InfluxDB.ConsistencyLevel.ANY;

    private InfluxDB influx;

    private String database;

    private String retentionPolicy;

    private InfluxDB.ConsistencyLevel consistency;

    @Override
    public void init(Configuration configuration) throws InitializationException {
        database = configuration.getString(CFG_DATABASE);
        retentionPolicy = configuration.getString(CFG_RETENTION_POLICY, "");
        try {
            consistency = InfluxDB.ConsistencyLevel.valueOf(configuration.getString(CFG_CONSISTENCY_LEVEL));
        } catch (Exception e) {
            consistency = DEFAULT_CONSISTENCY_LEVEL;
        }
        influx = InfluxDBFactory.connect(configuration.getString(CFG_URL),
                configuration.getString(CFG_USERNAME),
                configuration.getString(CFG_PASSWORD));
    }

    public void write(List<String> records) {
        influx.write(database, retentionPolicy, consistency, records);
    }

    @Override
    public void release() {
        influx.close();
    }
}
