package com.netease.arctic.ams.server.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @Author alex.wang
 * @Date 2023/2/17 16:51
 * @PackageName: com.netease.arctic.ams.server.utils
 * @Version 1.0
 */
public class JacksonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final ObjectMapper INDENT_OBJECT_MAPPER = new ObjectMapper();

    private static final String SERIALIZE_ERROR = "serialize object error";

    private static final String DESERIALIZE_ERROR = "deserialize to object error";

    static {
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, true);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        INDENT_OBJECT_MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    private JacksonUtils(){
        //do nothing
    }

    public static String toIndentJSON(Object object){
        try{
            return INDENT_OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(SERIALIZE_ERROR, e);
        }
    }

    public static String toJSONString(Object object){
        try{
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e){
            throw new IllegalStateException(SERIALIZE_ERROR, e);
        }
    }

    public static <T> T parseObject(String jsonString, Class<T> clazz){
        try {
            return OBJECT_MAPPER.readValue(jsonString, clazz);
        } catch (IOException e){
            throw new IllegalStateException(DESERIALIZE_ERROR, e);
        }
    }

    public static <T> T parseObject(String jsonString, TypeReference<T> typeReference){
        try {
            return OBJECT_MAPPER.readValue(jsonString, typeReference);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(DESERIALIZE_ERROR, e);
        }
    }

    public static <T> T parseObjects(String jsonString, Class<T> clazz){
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(ArrayList.class, clazz);
        try {
            return OBJECT_MAPPER.readValue(jsonString, javaType);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(DESERIALIZE_ERROR, e);
        }
    }

}
