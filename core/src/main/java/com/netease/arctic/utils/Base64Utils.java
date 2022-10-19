package com.netease.arctic.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class Base64Utils {
  public static String encodeBase64(String filePath) throws IOException {
    if (filePath == null || "".equals(filePath.trim())) {
      return null;
    } else {
      return Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(filePath)));
    }
  }

  public static String encodeXmlBase64(String filePath) throws IOException {
    if (filePath == null || "".equals(filePath.trim())) {
      return Base64.getEncoder().encodeToString("<configuration></configuration>".getBytes(StandardCharsets.UTF_8));
    } else {
      return Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(filePath)));
    }
  }
}
