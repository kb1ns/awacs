package io.awacs.component.influxdb;

import io.awacs.common.Configurable;
import io.awacs.common.Configuration;
import io.awacs.common.InitializationException;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

/**
 * Created by pixyonly on 03/09/2017.
 */
public class InfluxdbComponent implements Configurable {

    @Override
    public void init(Configuration configuration) throws InitializationException {
        InfluxDB influx = InfluxDBFactory.connect("", "", "");

        BatchPoints bp = BatchPoints.database("").point(Point.measurement("").build()).build();
        influx.write(bp);

        influx.close();
    }

    public void write(Point point) {

    }
}
