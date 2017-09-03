package io.awacs.repository.influx;

import io.awacs.common.Configuration;
import io.awacs.common.InitializationException;
import io.awacs.common.Packet;
import io.awacs.common.Repository;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.net.InetSocketAddress;

/**
 *
 * Created by pixyonly on 31/08/2017.
 */
public class InfluxRepository implements Repository {

    @Override
    public void init(Configuration configuration) throws InitializationException {
        InfluxDB influx = InfluxDBFactory.connect("", "", "");

        BatchPoints bp = BatchPoints.database("").point(Point.measurement("").build()).build();
        influx.write(bp);

        influx.close();
    }

    public void write(Point point) {

    }

    @Override
    public Packet confirm(Packet recieve, InetSocketAddress remote) {
        return null;
    }
}
