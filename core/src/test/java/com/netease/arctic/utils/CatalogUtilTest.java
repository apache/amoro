package com.netease.arctic.utils;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class CatalogUtilTest {
    /**
     * when log-store flag is on , fill up with default related props and other user-defined prop should be keep
     */
    @Test
    public void testMergeCatalogPropertiesToTable() {
        Map<String, String> expected = new HashMap<>();
        expected.put("log-store.enable","true");
        expected.put("log-store.address","168.0.0.1:9092");
        expected.put("log-store.type","kafka");
        expected.put("other.prop","10");
        expected.put("log-store.consistency.guarantee.enable","true");

        Map<String, String> userDefined = new HashMap<>();
        userDefined.put("log-store.enable","true");
        userDefined.put("other.prop","10");

        Map<String, String> catalogProperties = new HashMap<>();
        catalogProperties.put("table.log-store.enable","false");
        catalogProperties.put("table.log-store.address","168.0.0.1:9092");
        catalogProperties.put("table.log-store.type","kafka");
        catalogProperties.put("table.log-store.consistency.guarantee.enable","true");
        catalogProperties.put("ams.address","127.0.0.1");

        Map<String, String> result = CatalogUtil.mergeCatalogPropertiesToTable(userDefined, catalogProperties);
        Assert.assertEquals(expected, result);
    }

    /**
     * when log-store flag is off, remove all related props
     */
    @Test
    public void testMergeCatalogPropertiesToTable1() {
        Map<String, String> expected = new HashMap<>();
        expected.put("log-store.enable","false");

        Map<String, String>  userDefined = new HashMap<>();
        userDefined.put("log-store.enable","false");

        Map<String, String> catalogProperties = new HashMap<>();
        catalogProperties.put("table.log-store.enable","false");
        catalogProperties.put("table.log-store.address","168.0.0.1:9092");
        catalogProperties.put("table.log-store.type","kafka");
        catalogProperties.put("table.log-store.consistency.guarantee.enable","true");
        catalogProperties.put("ams.address","127.0.0.1");

        Map<String, String> result = CatalogUtil.mergeCatalogPropertiesToTable(userDefined, catalogProperties);
        Assert.assertEquals(expected, result);
    }

    /**
     * user-defined prop should not be overwritten by default props
     */
    @Test
    public void testMergeCatalogPropertiesToTable2() {
        Map<String, String> expected = new HashMap<>();
        expected.put("log-store.enable","true");
        expected.put("log-store.address","168.0.0.1:9092");
        expected.put("log-store.type","pulsar");
        expected.put("log-store.consistency.guarantee.enable","true");

        Map<String, String> userDefined = new HashMap<>();
        userDefined.put("log-store.enable","true");
        userDefined.put("log-store.type","pulsar");

        Map<String, String> catalogProperties = new HashMap<>();
        catalogProperties.put("table.log-store.enable","false");
        catalogProperties.put("table.log-store.address","168.0.0.1:9092");
        catalogProperties.put("table.log-store.type","kafka");
        catalogProperties.put("table.log-store.consistency.guarantee.enable","true");
        catalogProperties.put("ams.address","127.0.0.1");

        Map<String, String> result = CatalogUtil.mergeCatalogPropertiesToTable(userDefined, catalogProperties);
        Assert.assertEquals(expected, result);
    }

    /**
     * Other user-defined prop should not lose
     */
    @Test
    public void testMergeCatalogPropertiesToTable3() {
        Map<String, String> expected = new HashMap<>();
        expected.put("log-store.enable","true");
        expected.put("log-store.address","168.0.0.1:9092");
        expected.put("log-store.type","kafka");
        expected.put("log-store.consistency.guarantee.enable","true");
        expected.put("table.other-props","foo");

        Map<String, String>  userDefined = new HashMap<>();
        userDefined.put("log-store.enable","true");
        userDefined.put("log-store.type","kafka");
        userDefined.put("table.other-props","foo");

        Map<String, String> catalogProperties = new HashMap<>();
        catalogProperties.put("table.log-store.enable","false");
        catalogProperties.put("table.log-store.address","168.0.0.1:9092");
        catalogProperties.put("table.log-store.type","kafka");
        catalogProperties.put("table.log-store.consistency.guarantee.enable","true");
        catalogProperties.put("ams.address","127.0.0.1");

        Map<String, String> result = CatalogUtil.mergeCatalogPropertiesToTable(userDefined, catalogProperties);
        Assert.assertEquals(expected, result);
    }

    /**
     * user-defined and default catalog 'optimize.enable' are both switched on,
     * keep all related props
     */
    @Test
    public void testMergeCatalogPropertiesToTable4() {
        Map<String, String> expected = new HashMap<>();
        expected.put("log-store.enable","true");
        expected.put("log-store.address","168.0.0.1:9092");
        expected.put("log-store.type","kafka");
        expected.put("log-store.consistency.guarantee.enable","true");
        expected.put("optimize.enable","true");
        expected.put("optimize.quota","0.2"); // should not overwritten by default
        expected.put("optimize.group","mygroup"); // inherit from default prop
        expected.put("table.other-props","foo");


        Map<String, String> userDefined = new HashMap<>();
        userDefined.put("log-store.enable","true");
        userDefined.put("optimize.enable","true");
        userDefined.put("optimize.quota","0.2");
        userDefined.put("table.other-props","foo");

        Map<String, String> catalogProperties = new HashMap<>();
        catalogProperties.put("table.log-store.enable","false");
        catalogProperties.put("table.log-store.address","168.0.0.1:9092");
        catalogProperties.put("table.log-store.type","kafka");
        catalogProperties.put("table.log-store.consistency.guarantee.enable","true");
        catalogProperties.put("table.optimize.enable","false");
        catalogProperties.put("table.optimize.quota","0.1");
        catalogProperties.put("table.optimize.group","mygroup");
        catalogProperties.put("ams.address","127.0.0.1");

        Map<String, String> result = CatalogUtil.mergeCatalogPropertiesToTable(userDefined, catalogProperties);
        Assert.assertEquals(expected, result);
    }

    /**
     * user-defined and default catalog prop 'optimize.enable' are both switched off,
     * remove optimizer related props from default catalog
     * while keep user-defined related prop and 'optimize.enable' itself.
     */
    @Test
    public void testMergeCatalogPropertiesToTable5() {
        Map<String, String> expected = new HashMap<>();
        expected.put("log-store.enable","true");
        expected.put("log-store.address","168.0.0.1:9092");
        expected.put("log-store.type","kafka");
        expected.put("log-store.consistency.guarantee.enable","true");
        expected.put("optimize.enable","false");
        // user-defined related prop should be kept
        expected.put("optimize.quota","0.2");
        expected.put("table.other-props","foo");

        Map<String, String> userDefined = new HashMap<>();
        userDefined.put("log-store.enable","true");
        userDefined.put("optimize.enable","false");
        userDefined.put("optimize.quota","0.2");
        userDefined.put("table.other-props","foo");

        Map<String, String> catalogProperties = new HashMap<>();
        catalogProperties.put("table.log-store.enable","false");
        catalogProperties.put("table.log-store.address","168.0.0.1:9092");
        catalogProperties.put("table.log-store.type","kafka");
        catalogProperties.put("table.log-store.consistency.guarantee.enable","true");
        catalogProperties.put("table.optimize.enable","false");
        catalogProperties.put("table.optimize.quota","0.1");
        catalogProperties.put("table.optimize.group","mygroup");
        catalogProperties.put("ams.address","127.0.0.1");

        Map<String, String> result = CatalogUtil.mergeCatalogPropertiesToTable(userDefined, catalogProperties);
        Assert.assertEquals(expected, result);
    }

    /**
     * when optimized flag is off in catalog props and no user-defined value,
     * remove optimizer related props but 'optimize.enable' itself.
     */
    @Test
    public void testMergeCatalogPropertiesToTable6() {
        Map<String, String> expected = new HashMap<>();
        expected.put("log-store.enable","true");
        expected.put("log-store.address","168.0.0.1:9092");
        expected.put("log-store.type","kafka");
        expected.put("log-store.consistency.guarantee.enable","true");
        expected.put("optimize.enable","false");
        expected.put("table.other-props","foo");

        Map<String, String> userDefined = new HashMap<>();
        userDefined.put("log-store.enable","true");
        userDefined.put("table.other-props","foo");

        Map<String, String> catalogProperties = new HashMap<>();
        catalogProperties.put("table.log-store.enable","false");
        catalogProperties.put("table.log-store.address","168.0.0.1:9092");
        catalogProperties.put("table.log-store.type","kafka");
        catalogProperties.put("table.log-store.consistency.guarantee.enable","true");
        catalogProperties.put("table.optimize.enable","false");
        catalogProperties.put("table.optimize.quota","0.1");
        catalogProperties.put("table.optimize.group","mygroup");
        catalogProperties.put("ams.address","127.0.0.1");

        Map<String, String> result = CatalogUtil.mergeCatalogPropertiesToTable(userDefined, catalogProperties);
        Assert.assertEquals(expected, result);
    }

    /**
     * user-defined 'optimize.enable' is switched on,
     * overwrite behavior of default catalog props
     */
    @Test
    public void testMergeCatalogPropertiesToTable7() {
        Map<String, String> expected = new HashMap<>();
        expected.put("optimize.enable","true");
        expected.put("optimize.quota","0.1");
        expected.put("optimize.group","mygroup");
        expected.put("table.other-props","foo");

        Map<String, String> userDefined = new HashMap<>();
        userDefined.put("optimize.enable","true");
        userDefined.put("table.other-props","foo");

        Map<String, String> catalogProperties = new HashMap<>();
        catalogProperties.put("table.log-store.enable","false");
        catalogProperties.put("table.log-store.address","168.0.0.1:9092");
        catalogProperties.put("table.log-store.type","kafka");
        catalogProperties.put("table.log-store.consistency.guarantee.enable","true");
        catalogProperties.put("table.optimize.enable","false");
        catalogProperties.put("table.optimize.quota","0.1");
        catalogProperties.put("table.optimize.group","mygroup");
        catalogProperties.put("ams.address","127.0.0.1");

        Map<String, String> result = CatalogUtil.mergeCatalogPropertiesToTable(userDefined, catalogProperties);
        Assert.assertEquals(expected, result);
    }
}