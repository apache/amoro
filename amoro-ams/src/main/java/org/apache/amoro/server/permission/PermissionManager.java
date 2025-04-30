package org.apache.amoro.server.permission;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.Environments;
import org.casbin.jcasbin.main.Enforcer;

public class PermissionManager {
    private final Enforcer enforcer;

    public PermissionManager() {
        String modelPath = Environments.getConfigPath() + "/rbac_model.conf" ;
        String policyFile = Environments.getConfigPath() + "/policy.csv" ;
        enforcer = new Enforcer(modelPath, policyFile);
    }

    public  boolean accessible(String user,String url,String method) {
        if (!enforcer.enforce(user, url, method)) {
            return false;
        }
        return true;
    }
}
