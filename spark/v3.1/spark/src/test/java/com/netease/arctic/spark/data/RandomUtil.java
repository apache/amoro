/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.netease.arctic.spark.data;

import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

public class RandomUtil {

  private static final long FIFTY_YEARS_IN_MICROS =
      (50L * (365 * 3 + 366) * 24 * 60 * 60 * 1_000_000) / 4;
  private static final int ABOUT_380_YEARS_IN_DAYS = 380 * 365;
  private static final long ONE_DAY_IN_MICROS = 24 * 60 * 60 * 1_000_000L;
  private static final String CHARS =
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-.!?";
  private static long data = 0;

  public static Object generatePrimitive(Type.PrimitiveType primitive,
                                         Random random) {
    switch (primitive.typeId()) {
      case BOOLEAN:
        return random.nextBoolean();

      case INTEGER:
        return random.nextInt();

      case LONG:
        return data += 1;

      case FLOAT:
        return random.nextFloat();

      case DOUBLE:
        return random.nextDouble();

      case DATE:
        return random.nextInt() % ABOUT_380_YEARS_IN_DAYS;

      case TIME:
        return (random.nextLong() & Integer.MAX_VALUE) % ONE_DAY_IN_MICROS;

      case TIMESTAMP:
        return random.nextLong() % FIFTY_YEARS_IN_MICROS;

      case STRING:
        return randomString(random);

      case UUID:
        byte[] uuidBytes = new byte[16];
        random.nextBytes(uuidBytes);
        return uuidBytes;

      case FIXED:
        byte[] fixed = new byte[((Types.FixedType) primitive).length()];
        random.nextBytes(fixed);
        return fixed;

      case BINARY:
        byte[] binary = new byte[random.nextInt(50)];
        random.nextBytes(binary);
        return binary;

      case DECIMAL:
        Types.DecimalType type = (Types.DecimalType) primitive;
        BigInteger unscaled = randomUnscaled(type.precision(), random);
        return new BigDecimal(unscaled, type.scale());

      default:
        throw new IllegalArgumentException(
            "Cannot generate random value for unknown type: " + primitive);
    }
  }

  private static String randomString(Random random) {
    int length = random.nextInt(50);
    byte[] buffer = new byte[length];

    for (int i = 0; i < length; i += 1) {
      buffer[i] = (byte) CHARS.charAt(random.nextInt(CHARS.length()));
    }

    return new String(buffer);
  }

  private static final String DIGITS = "0123456789";

  private static BigInteger randomUnscaled(int precision, Random random) {
    int length = random.nextInt(precision);
    if (length == 0) {
      return BigInteger.ZERO;
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i += 1) {
      sb.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
    }

    return new BigInteger(sb.toString());
  }
}
