package com.netease.arctic.server.persistence.converter;


import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(List.class)
public class List2StringConverter implements TypeHandler<List> {

  private String separator = ",";

  @Override
  public void setParameter(PreparedStatement ps, int i, List parameter, JdbcType jdbcType) throws SQLException {
    if (parameter == null) {
      ps.setString(i, null);
      return;
    }

    StringBuilder builder = new StringBuilder();
    Iterator iterator = parameter.iterator();
    while (iterator.hasNext()) {
      builder.append(iterator.next());
      if (iterator.hasNext()) {
        builder.append(separator);
      }
    }
    ps.setString(i, builder.toString());

  }

  @Override
  public List getResult(ResultSet rs, String columnName) throws SQLException {
    String res = rs.getString(columnName);
    if (res == null) {
      return null;
    }

    List set = new ArrayList<>();
    if (res.length() != 0) {
      String[] fields = res.split(separator);
      for (String field : fields) {
        set.add(field);
      }
    }

    return set;
  }

  @Override
  public List getResult(ResultSet rs, int columnIndex) throws SQLException {
    String res = rs.getString(columnIndex);
    if (res == null) {
      return null;
    }

    List set = new ArrayList<>();
    if (res.length() != 0) {
      String[] fields = res.split(separator);
      for (String field : fields) {
        set.add(field);
      }
    }

    return set;
  }

  @Override
  public List getResult(CallableStatement cs, int columnIndex) throws SQLException {
    String res = cs.getString(columnIndex);
    if (res == null) {
      return null;
    }

    List set = new ArrayList<>();
    if (res.length() != 0) {
      String[] fields = res.split(separator);
      for (String field : fields) {
        set.add(field);
      }
    }

    return set;
  }
}

