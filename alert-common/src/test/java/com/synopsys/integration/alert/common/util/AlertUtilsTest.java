package com.synopsys.integration.alert.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.util.Stringable;

public class AlertUtilsTest {

    @Test
    public void convertListWithNameKey() {
        String key1 = "key1";
        String key2 = "key2";
        TestObject testObject1 = new TestObject(key1, "something");
        TestObject testObject2 = new TestObject(key2, "something");
        final Map<String, TestObject> mapWithNameKey = AlertUtils.convertToMapWithCopiedValue(List.of(testObject1, testObject2), TestObject::getName);

        assertTrue(mapWithNameKey.containsKey(key1));
        assertTrue(mapWithNameKey.containsKey(key2));
        assertEquals(testObject1, mapWithNameKey.get(key1));

        assertFalse(mapWithNameKey.containsKey(testObject1));
        assertFalse(mapWithNameKey.containsKey(testObject1.getDesc()));
    }

    @Test
    public void convertListWithObjectKey() {
        String key1 = "key1";
        String key2 = "key2";
        TestObject testObject1 = new TestObject(key1, "something");
        TestObject testObject2 = new TestObject(key2, "something");
        final Map<TestObject, String> mapWithObjectKey = AlertUtils.convertToMapWithCopiedKey(List.of(testObject1, testObject2), TestObject::getName);

        assertTrue(mapWithObjectKey.containsKey(testObject1));
        assertTrue(mapWithObjectKey.containsKey(testObject2));
        assertEquals(key1, mapWithObjectKey.get(testObject1));

        assertFalse(mapWithObjectKey.containsKey(key1));
    }

    @Test
    public void checkCurrentTime() throws InterruptedException {
        final Date firstTimeStamp = AlertUtils.createCurrentDateTimestamp();
        TimeUnit.SECONDS.sleep(1);
        final Date currentDateTimestamp = AlertUtils.createCurrentDateTimestamp();

        assertTrue(currentDateTimestamp.after(firstTimeStamp));
        assertTrue(firstTimeStamp.before(currentDateTimestamp));
    }

    class TestObject extends Stringable {
        String name;
        String desc;

        public TestObject(final String name, final String desc) {
            this.name = name;
            this.desc = desc;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }
    }
}
