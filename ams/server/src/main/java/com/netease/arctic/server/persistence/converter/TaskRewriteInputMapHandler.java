package com.netease.arctic.server.persistence.converter;

import com.google.common.collect.Maps;
import com.netease.arctic.optimizing.RewriteFilesInput;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.ibatis.type.JdbcType;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

public class TaskRewriteInputMapHandler extends Object2ByteArrayConvert<Map<Integer, RewriteFilesInput>> {

  @Override
  public void setParameter(
      PreparedStatement ps, int i, Map<Integer, RewriteFilesInput> parameter, JdbcType jdbcType) throws SQLException {
    if (parameter == null) {
      ps.setNull(i, Types.BLOB);
      return;
    }
    byte[] values = serialize(parameter);
    ps.setBytes(i, values);
  }

  private byte[] serialize(Map<Integer, RewriteFilesInput> target) {
    Map<Integer, RewriteFilesInput> copied = Maps.newHashMapWithExpectedSize(target.size());
    target.forEach((id, input) -> {
      RewriteFilesInput copiedInput = new RewriteFilesInput(
          input.rewrittenDataFiles(), input.rePosDeletedDataFiles(),
          input.readOnlyDeleteFiles(), input.rewrittenDeleteFiles(), null);
      copied.put(id, copiedInput);
    });
    return SerializationUtil.simpleSerialize(copied).array();
  }
}
