package io.awacs.repository.influx;

import io.awacs.core.Configurable;
import io.awacs.core.Configuration;
import io.awacs.core.InitializationException;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

/**
 * Created by pixyonly on 31/08/2017.
 */
public class InfluxRepository implements Configurable {

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
