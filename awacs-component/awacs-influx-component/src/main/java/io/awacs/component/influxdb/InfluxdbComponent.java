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

    private InfluxDB influx;

    private String database;

    @Override
    public void init(Configuration configuration) throws InitializationException {
        database = configuration.getString(CFG_DATABASE);
        influx = InfluxDBFactory.connect(configuration.getString(CFG_URL),
                configuration.getString(CFG_USERNAME),
                configuration.getString(CFG_PASSWORD));
    }

    public void write(List<String> records) {
        influx.write(database, "", InfluxDB.ConsistencyLevel.valueOf(""), records);
    }

    @Override
    public void release() {
        influx.close();
    }
}
