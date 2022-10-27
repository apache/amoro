package com.netease.arctic.ams.server.service.impl;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.server.mapper.CatalogMetadataMapper;
import com.netease.arctic.ams.server.mapper.PlatformFileInfoMapper;
import com.netease.arctic.ams.server.service.IJDBCService;
import org.apache.ibatis.session.SqlSession;

import java.util.Base64;
import java.util.List;

/**
 * @Auth: hzwangtao6
 * @Time: 2022/10/26 10:30
 * @Description:
 */
public class PlatformFileInfoService extends IJDBCService {
  /**
   * add some file
   * @param name
   * @param content
   * @return
   */
  public Integer addFile(String name, String content) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      PlatformFileInfoMapper platformFileInfoMapper =
              getMapper(sqlSession, PlatformFileInfoMapper.class);
      Integer fildId = 0;
      platformFileInfoMapper.addFile(name, content, fildId);
      return fildId;
    }
  }

  /**
   * get file content
   * @param fileId
   * @return
   */
  public String getFileContentB64ById(Integer fileId) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      PlatformFileInfoMapper platformFileInfoMapper =
              getMapper(sqlSession, PlatformFileInfoMapper.class);
      return platformFileInfoMapper.getFileById(fileId);
    }
  }

  /**
   * get file content
   * @param fileId
   * @return
   */
  public String getFileContentById(Integer fileId) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      PlatformFileInfoMapper platformFileInfoMapper =
              getMapper(sqlSession, PlatformFileInfoMapper.class);
      return new String(Base64.getDecoder().decode(platformFileInfoMapper.getFileById(fileId)));
    }
  }

}
