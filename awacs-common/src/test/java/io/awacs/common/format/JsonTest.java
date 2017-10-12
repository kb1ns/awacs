package io.awacs.common.format;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by pixyonly on 12/10/2017.
 */
public class JsonTest {

    @Test
    public void test() {
        List<Object> l = new LinkedList<>();
        l.add("hello");
        l.add("world");
        l.add(3l);
        l.add(false);
        Map<String, Object> map = new HashMap<>();
        map.put("1", 2);
        map.put("2", 0.2f);
        map.put("3", true);
        map.put("4", 'h');
        String s = Json.empty()
                .startObject()
                .objectValue("key1", "value1")
                .startObject("map")
                .objectValue("sub1", "sub1")
                .endObject()
                .array("l", l)
                .object("m", map)
                .endObject()
                .toString();
        Assert.assertEquals("", s, "{\"key1\":\"value1\",\"map\":{\"sub1\":\"sub1\"},\"l\":[\"hello\",\"world\",3,false],\"m\":{\"1\":2,\"2\":0.2,\"3\":true,\"4\":\"h\"}}");
    }
}
