package com.netease.arctic.ams.server;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @Author alex.wang
 * @Date 2023/2/26 11:16
 * @PackageName: PACKAGE_NAME
 * @Version 1.0
 */
public abstract class LocalJsonNode extends JsonNode {

    public String getString(String fieldName){
        if(fieldName == null || super.get(fieldName) == null){
            return null;
        } else {
            return super.get(fieldName).toString();
        }
    }
}
