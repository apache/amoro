package com.netease.arctic.ams.server.mapper;

import com.netease.arctic.ams.server.model.ApiTokens;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ApiTokensMapper {
  String TABLE_NAME = "api_tokens";

  @Select("select secret from " +
          TABLE_NAME + " where apikey = #{apikey}")
  String getSecretBykey(String apikey);

  @Insert("insert into " + TABLE_NAME + " (apikey,secret,applyTime) values(#{apiTokens.apikey}," +
          "#{apiTokens.secret},#{apiTokens.applyTime})")
  void insert(ApiTokens apiTokens);

  @Insert("delete from " + TABLE_NAME + " where id = #{id}")
  void delToken(Integer id);

}
