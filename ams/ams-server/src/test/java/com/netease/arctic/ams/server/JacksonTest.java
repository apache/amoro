package com.netease.arctic.ams.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netease.arctic.ams.server.config.ArcticMetaStoreConf;
import com.netease.arctic.ams.server.config.ConfigFileProperties;
import com.netease.arctic.ams.server.utils.JacksonUtils;
import com.netease.arctic.ams.server.utils.YamlUtils;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

/**
 * @Author alex.wang
 * @Date 2023/2/26 10:22
 * @PackageName: com.netease.arctic.ams.server
 * @Version 1.0
 */
public class JacksonTest {



    @Test
    public void jacksonTest(){
        String configPath = System.getProperty("user.dir") + "/src/test/resources/test.yaml";
        JsonNode yamlConfig = load(configPath);
        JsonNode systemConfig = yamlConfig.get(ConfigFileProperties.SYSTEM_CONFIG);

        JsonNode catalogs = yamlConfig.findValues(ConfigFileProperties.CONTAINER_LIST).get(0);
        for(JsonNode jsonNode : catalogs){
            System.out.println(jsonNode.get("name").asText());
        }
        System.out.println(systemConfig.has("aaa"));
        System.out.println(JacksonUtils.getString(systemConfig, "aaaaa"));
        System.out.println(JacksonUtils.getString(systemConfig, "b"));
        System.out.println(JacksonUtils.getInteger(systemConfig, "a"));
        try {
            System.out.println(systemConfig.get("asdasd").asText());
        }catch (Exception ex){
            ex.printStackTrace();
        }
        System.out.println(JacksonUtils.getOrDefaultString(systemConfig, "c", "test"));
        try{
            System.out.println(JacksonUtils.getBoolean(systemConfig,"a"));
        } catch (Exception ex){
            ex.printStackTrace();
        }

        System.out.println(JacksonUtils.getBoolean(systemConfig,"f"));
        System.out.println(JacksonUtils.parseObject(JacksonUtils.getString(yamlConfig, "test"), Map.class));
        String listStr = JacksonUtils.getString(systemConfig, "d");
        System.out.println(listStr);
        List<JsonNode> e = yamlConfig.findValues("d");
        List<String> o = JacksonUtils.parseObjectsString(listStr);
        for (int i = 0; i<o.size();i++){
            System.out.println(o.get(i));
        }

        for (Iterator<String> it = systemConfig.fieldNames(); it.hasNext(); ) {
            String key = it.next();
            if (key != null) {
                String value = JacksonUtils.getString(systemConfig, key);
                System.out.println(value);
            }
        }

        JackTest test = new JackTest();
        test.setA("good");
        test.setB("better");
        test.setC(12);
        System.out.println(JacksonUtils.toJSONString(test));

        String testJson = "{\"a\":\"good\",\"b\":\"better\",\"c\":12}";
        JackTest jack = JacksonUtils.parseObject(testJson, JackTest.class);
        

        Map<String, Object> testDir = new HashMap<>();
        testDir.put("a","aaa");
        testDir.put("b", true);
        testDir.put("c", 1);
        System.out.println(JacksonUtils.toJSONString(testDir));
    }

   static class JackTest{

        private String a;

        private String b;

        private Integer c;

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public String getB() {
            return b;
        }

        public void setB(String b) {
            this.b = b;
        }

        public Integer getC() {
            return c;
        }

        public void setC(Integer c) {
            this.c = c;
        }
    }

    private static JsonNode load(String path) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Yaml yaml = new Yaml();
        ObjectMapper objMapper = new ObjectMapper();
        return objMapper.valueToTree(yaml.loadAs(inputStream, Map.class));
    }
}
