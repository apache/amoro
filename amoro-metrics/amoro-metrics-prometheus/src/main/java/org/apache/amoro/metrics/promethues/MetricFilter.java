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

package org.apache.amoro.metrics.promethues;

import java.util.Map;
import java.util.regex.Pattern;

/** Regex-based metric filter for Prometheus exporter. */
public class MetricFilter {

  public static final String INCLUDES_KEY = "metric-filter.includes";
  public static final String EXCLUDES_KEY = "metric-filter.excludes";

  public static final MetricFilter ACCEPT_ALL = new MetricFilter(null, null);

  private final Pattern includePattern;
  private final Pattern excludePattern;

  public MetricFilter(Pattern includePattern, Pattern excludePattern) {
    this.includePattern = includePattern;
    this.excludePattern = excludePattern;
  }

  /** Parse metric filter from reporter properties. */
  public static MetricFilter fromProperties(Map<String, String> properties) {
    String includes = properties.get(INCLUDES_KEY);
    String excludes = properties.get(EXCLUDES_KEY);

    if (includes == null && excludes == null) {
      return ACCEPT_ALL;
    }

    Pattern includePattern = includes != null ? Pattern.compile(includes) : null;
    Pattern excludePattern = excludes != null ? Pattern.compile(excludes) : null;
    return new MetricFilter(includePattern, excludePattern);
  }

  /** Check if a metric name passes the filter. */
  public boolean matches(String metricName) {
    if (includePattern != null && !includePattern.matcher(metricName).matches()) {
      return false;
    }
    if (excludePattern != null && excludePattern.matcher(metricName).matches()) {
      return false;
    }
    return true;
  }
}
