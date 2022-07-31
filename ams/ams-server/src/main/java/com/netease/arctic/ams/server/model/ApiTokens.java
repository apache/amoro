package com.netease.arctic.ams.server.model;

public class ApiTokens {
  Integer id;
  String apikey;
  String secret;
  String applyTime;

  public ApiTokens() {
  }

  public ApiTokens(String apiKey, String secret) {
    this.apikey = apiKey;
    this.secret = secret;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getApikey() {
    return apikey;
  }

  public void setApikey(String apikey) {
    this.apikey = apikey;
  }


  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public String getApplyTime() {
    return applyTime;
  }

  public void setApplyTime(String applyTime) {
    this.applyTime = applyTime;
  }
}
