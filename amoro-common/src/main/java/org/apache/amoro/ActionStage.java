package org.apache.amoro;

public class ActionStage {

  private String desc;
  private int weight;

  public ActionStage(String desc, int weight) {
    this.desc = desc;
    this.weight = weight;
  }

  public String getDesc() {
    return desc;
  }

  public int getWeight() {
    return weight;
  }
}
