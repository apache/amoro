package org.apache.amoro.server.permission;

import org.apache.amoro.config.Configurations;
import org.casbin.jcasbin.main.Enforcer;

public class PermissionManager {
    Enforcer enforcer;

    public PermissionManager() {
        //enforcer = new Enforcer(modelPath, policyFile);
    }

    public  boolean accessible(String userName,String url,String method) {

        return false;
    }
}
