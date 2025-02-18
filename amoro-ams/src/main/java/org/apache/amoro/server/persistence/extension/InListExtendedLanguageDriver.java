/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.amoro.server.persistence.extension;

import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mybatis language extensions, this diver will replace sql script like
 *
 * <pre>
 *  select * from tbl where id in (#{ids::number[]})
 * </pre>
 *
 * to
 *
 * <pre>
 * <script>
 *   select * from tbl where id in
 *    <foreach collection="items" item="item" separator="," open="(" close=")" >
 *      #{items}
 *    </foreach>
 * </script>
 * </pre>
 *
 * <p>or replace sql script like
 *
 * <pre>
 *  select * from tbl where id in (#{ids::string[]})
 * </pre>
 *
 * to
 *
 * <pre>
 * <script>
 *   select * from tbl where id in
 *    <foreach collection="items" item="item" separator="," open="(" close=")" >
 *      #{items}
 *    </foreach>
 * </script>
 * </pre>
 */
public class InListExtendedLanguageDriver extends XMLLanguageDriver implements LanguageDriver {

  private static final Logger LOG = LoggerFactory.getLogger(InListExtendedLanguageDriver.class);

  private final Pattern inNumberPattern = Pattern.compile("\\(#\\{(\\w+)::number\\[]}\\)");

  private final Pattern inStringPattern = Pattern.compile("\\(#\\{(\\w+)::string\\[]}\\)");

  @Override
  public SqlSource createSqlSource(
      Configuration configuration, String script, Class<?> parameterType) {
    Matcher matcher = inNumberPattern.matcher(script);
    if (matcher.find()) {
      String newScript = inNumber(matcher);
      LOG.info("REPLACE IN_NUMBER_LIST original script:{}, new script:{}", script, newScript);
      return super.createSqlSource(configuration, newScript, parameterType);
    }

    matcher = inStringPattern.matcher(script);
    if (matcher.find()) {
      String newScript = inString(matcher);
      LOG.info("REPLACE IN_NUMBER_LIST original script:{}, new script:{}", script, newScript);
      return super.createSqlSource(configuration, newScript, parameterType);
    }

    return super.createSqlSource(configuration, script, parameterType);
  }

  private String inNumber(Matcher matcher) {
    final String replacement =
        "(<foreach collection=\"$1\" item=\"__item\" separator=\",\" >#{__item}</foreach>)";
    String script = matcher.replaceAll(replacement);
    return "<script>" + script + "</script>";
  }

  private String inString(Matcher matcher) {
    final String replacement =
        "(<foreach collection=\"$1\" item=\"__item\" separator=\",\" >\"#{__item}\"</foreach>)";
    String script = matcher.replaceAll(replacement);
    return "<script>" + script + "</script>";
  }
}
