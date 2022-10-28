package com.netease.arctic.ams.server.mapper;

import com.netease.arctic.ams.server.model.PlatformFileInfo;
import com.netease.arctic.table.TableIdentifier;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @Auth: hzwangtao6
 * @Time: 2022/10/25 23:28
 * @Description:
 */
public interface PlatformFileInfoMapper {
  String TABLE_NAME = "platform_file_info";

  /**
   * add a file with content encoded by base64
   */
  @Insert("insert into " + TABLE_NAME + " (id,file_name,file_content_b64) " +
          "values(#{fileInfo.fileId},#{fileInfo.fileName},#{fileInfo.fileContent})")
  @Options(useGeneratedKeys = true, keyProperty = "fileInfo.fileId", keyColumn = "id")
  void addFile(@Param("fileInfo")PlatformFileInfo platformFileInfo);


  // get file content encoded by base64 by fileId
  @Select("select file_content_b64 from " + TABLE_NAME + " where id=#{fileId}")
  String getFileById(@Param("fileId") Integer fileId);
}
