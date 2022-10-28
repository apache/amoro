package com.netease.arctic.ams.server.model;

import java.util.List;
import java.util.Map;

/**
 * @Auth: hzwangtao6
 * @Time: 2022/10/28 17:37
 * @Description:
 */
public class OptimizeContainerInfo {
  String name;
  String type;
  Map<String,String> properties;

  List<OptimizeGroup> optimizeGroup;

  public OptimizeContainerInfo() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public List<OptimizeGroup> getOptimizeGroup() {
    return optimizeGroup;
  }

  public void setOptimizeGroup(List<OptimizeGroup> optimizeGroup) {
    this.optimizeGroup = optimizeGroup;
  }

  public static class OptimizeGroup {
    String name;
    Integer tmMemory;
    Integer jmMemory;

    public OptimizeGroup(String name, Integer tmMemory, Integer jmMemory) {
      this.name = name;
      this.tmMemory = tmMemory;
      this.jmMemory = jmMemory;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Integer getTmMemory() {
      return tmMemory;
    }

    public void setTmMemory(Integer tmMemory) {
      this.tmMemory = tmMemory;
    }

    public Integer getJmMemory() {
      return jmMemory;
    }

    public void setJmMemory(Integer jmMemory) {
      this.jmMemory = jmMemory;
    }
  }
}
