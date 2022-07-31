package com.netease.arctic.ams.server.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParamSignatureCalculator {
  public static final Logger LOG = LoggerFactory.getLogger(ParamSignatureCalculator.class);

  public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

  public static String getMD5(byte[] bytes) {
    char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    char[] str = new char[16 * 2];
    try {
      java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
      md.update(bytes);
      byte[] tmp = md.digest();
      int k = 0;
      for (int i = 0; i < 16; i++) {
        byte byte0 = tmp[i];
        str[k++] = hexDigits[byte0 >>> 4 & 0xf];
        str[k++] = hexDigits[byte0 & 0xf];
      }
    } catch (Exception e) {
      LOG.error("failed", e);
    }
    return new String(str);
  }

  /**
   * 计算字符串的md5值
   *
   * @param value
   * @return
   */
  public static String getMD5(String value) {
    String result = "";
    try {
      result = getMD5(value.getBytes("UTF-8"));
    } catch (Exception e) {
      LOG.error("What the fuck!!", e);
    }
    return result;
  }

  /**
   * 根据请求参数对，获取升序的 keyvalue拼接字符串。
   * 例：
   * params: name=&value=111&age=11&sex=1&high=180&nick=
   * 去除空，升序排列： age11high180sex1value111
   *
   * @param map
   * @return
   */
  public static String generateParamString(Map<String, String> map) {
    Set<String> set = map.keySet();
    String[] keyArray = (String[]) set.toArray(new String[set.size()]);
    StringBuffer sb = new StringBuffer("");
    Arrays.sort(keyArray);
    for (int i = 0; i < keyArray.length; i++) {
      String value = map.get(keyArray[i]);
      if (!StringUtils.isBlank(value)) {
        try {
          value = URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
          LOG.error("Failed to caculate signature", e);
          return null;
        }
        sb.append(keyArray[i]).append(value);
      }
    }
    return sb.toString();
  }

  public static String generateParamStringWithValueArray(Map<String, List<String>> map) {
    Set<String> set = map.keySet();
    String[] keyArray = (String[]) set.toArray(new String[set.size()]);
    StringBuffer sb = new StringBuffer("");
    Arrays.sort(keyArray);
    String firstValue = "";
    for (int i = 0; i < keyArray.length; i++) {
      List<String> values = map.get(keyArray[i]);
      Collections.sort(values);
      // 如果有值，只取第1个值进行校验
      if (values.size() >= 1) {
        if (!StringUtils.isBlank(values.get(0))) {
          try {
            firstValue = URLDecoder.decode(values.get(0), "utf-8");
          } catch (UnsupportedEncodingException e) {
            LOG.error("Failed to caculate signature", e);
            return null;
          }
          sb.append(keyArray[i]).append(firstValue);
        }
      }
    }
    return sb.toString();
  }

}
