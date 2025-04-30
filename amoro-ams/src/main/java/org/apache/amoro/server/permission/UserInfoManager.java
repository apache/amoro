package org.apache.amoro.server.permission;

import com.google.common.collect.Maps;
import org.apache.amoro.server.Environments;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserInfoManager {

    private final Map<String,String> users = Maps.newHashMap();


    public UserInfoManager() {
        String configPath = Environments.getConfigPath() + "/users.csv" ;
        this.loadUserInfoFileToMap(configPath);
    }
    public boolean isValidate(String username, String password) {
        if (users.containsKey(username)) {
            return users.get(username).equals(password);
        }
        return false;
    }
    private  void  loadUserInfoFileToMap(String filePath) {
        try {
            FileUtils.readLines(new File(filePath), "UTF-8").forEach(line -> {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    users.put(username, password);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("load userInfo file error",e);
        }

    }

}
