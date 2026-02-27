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

package org.apache.amoro.utils;

import static org.apache.amoro.utils.MemorySize.MemoryUnit.MEGA_BYTES;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/** Tests for the {@link MemorySize} class. */
public class MemorySizeTest {

  static Stream<Object[]> memorySizeProvider() {
    return Stream.of(
        new Object[] {MemorySize.ZERO, 0, 0, 0, 0, 0},
        new Object[] {new MemorySize(955), 955, 0, 0, 0, 0},
        new Object[] {new MemorySize(18500), 18500, 18, 0, 0, 0},
        new Object[] {new MemorySize(15 * 1024 * 1024), 15_728_640, 15_360, 15, 0, 0},
        new Object[] {
          new MemorySize(2L * 1024 * 1024 * 1024 * 1024 + 10),
          2199023255562L,
          2147483648L,
          2097152,
          2048,
          2
        });
  }

  @ParameterizedTest
  @MethodSource("memorySizeProvider")
  void testUnitConversion(
      MemorySize memorySize,
      long expectedBytes,
      long expectedKibiBytes,
      long expectedMebiBytes,
      long expectedGibiBytes,
      long expectedTebiBytes) {
    assertEquals(expectedBytes, memorySize.getBytes());
    assertEquals(expectedKibiBytes, memorySize.getKibiBytes());
    assertEquals(expectedMebiBytes, memorySize.getMebiBytes());
    assertEquals(expectedGibiBytes, memorySize.getGibiBytes());
    assertEquals(expectedTebiBytes, memorySize.getTebiBytes());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalid() {
    new MemorySize(-1);
  }

  @ParameterizedTest
  @CsvSource({
    "'1234', 1234",
    "'1234b', 1234",
    "'1234 b', 1234",
    "'1234bytes', 1234",
    "'1234 bytes', 1234"
  })
  public void testParseBytes(String input, long expected) {
    assertEquals(expected, MemorySize.parseBytes(input));
  }

  @ParameterizedTest
  @CsvSource({
    "667766k, 667766",
    "667766 k, 667766",
    "667766kb, 667766",
    "667766 kb, 667766",
    "667766kibibytes, 667766",
    "667766 kibibytes, 667766"
  })
  public void testParseKibiBytes(String input, int expected) {
    assertEquals(expected, MemorySize.parse(input).getKibiBytes());
  }

  @ParameterizedTest
  @CsvSource({
    "7657623m, 7657623",
    "7657623 m, 7657623",
    "7657623mb, 7657623",
    "7657623 mb, 7657623",
    "7657623mebibytes, 7657623",
    "7657623 mebibytes, 7657623"
  })
  public void testParseMebiBytes(String input, int expected) {
    assertEquals(expected, MemorySize.parse(input).getMebiBytes());
  }

  @ParameterizedTest
  @CsvSource({
    "987654g, 987654",
    "987654 g, 987654",
    "987654gb, 987654",
    "987654 gb, 987654",
    "987654gibibytes, 987654",
    "987654 gibibytes, 987654"
  })
  public void testParseGibiBytes(String input, int expected) {
    assertEquals(expected, MemorySize.parse(input).getGibiBytes());
  }

  @ParameterizedTest
  @CsvSource({
    "1234567t, 1234567",
    "1234567 t, 1234567",
    "1234567tb, 1234567",
    "1234567 tb, 1234567",
    "1234567tebibytes, 1234567",
    "1234567 tebibytes, 1234567"
  })
  public void testParseTebiBytes(String input, int expected) {
    assertEquals(expected, MemorySize.parse(input).getTebiBytes());
  }

  @Test
  public void testUpperCase() {
    assertEquals(1L, MemorySize.parse("1 B").getBytes());
    assertEquals(1L, MemorySize.parse("1 K").getKibiBytes());
    assertEquals(1L, MemorySize.parse("1 M").getMebiBytes());
    assertEquals(1L, MemorySize.parse("1 G").getGibiBytes());
    assertEquals(1L, MemorySize.parse("1 T").getTebiBytes());
  }

  @Test
  public void testTrimBeforeParse() {
    assertEquals(155L, MemorySize.parseBytes("      155      "));
    assertEquals(155L, MemorySize.parseBytes("      155      bytes   "));
  }

  @Test
  public void testParseInvalid() {
    // null
    try {
      MemorySize.parseBytes(null);
      fail("exception expected");
    } catch (IllegalArgumentException ignored) {
    }

    // empty
    try {
      MemorySize.parseBytes("");
      fail("exception expected");
    } catch (IllegalArgumentException ignored) {
    }

    // blank
    try {
      MemorySize.parseBytes("     ");
      fail("exception expected");
    } catch (IllegalArgumentException ignored) {
    }

    // no number
    try {
      MemorySize.parseBytes("foobar or fubar or foo bazz");
      fail("exception expected");
    } catch (IllegalArgumentException ignored) {
    }

    // wrong unit
    try {
      MemorySize.parseBytes("16 gjah");
      fail("exception expected");
    } catch (IllegalArgumentException ignored) {
    }

    // multiple numbers
    try {
      MemorySize.parseBytes("16 16 17 18 bytes");
      fail("exception expected");
    } catch (IllegalArgumentException ignored) {
    }

    // negative number
    try {
      MemorySize.parseBytes("-100 bytes");
      fail("exception expected");
    } catch (IllegalArgumentException ignored) {
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseNumberOverflow() {
    MemorySize.parseBytes("100000000000000000000000000000000 bytes");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseNumberTimeUnitOverflow() {
    MemorySize.parseBytes("100000000000000 tb");
  }

  @Test
  public void testParseWithDefaultUnit() {
    assertEquals(7, MemorySize.parse("7", MEGA_BYTES).getMebiBytes());
    assertNotEquals(7, MemorySize.parse("7340032", MEGA_BYTES));
    assertEquals(7, MemorySize.parse("7m", MEGA_BYTES).getMebiBytes());
    assertEquals(7168, MemorySize.parse("7", MEGA_BYTES).getKibiBytes());
    assertEquals(7168, MemorySize.parse("7m", MEGA_BYTES).getKibiBytes());
    assertEquals(7, MemorySize.parse("7 m", MEGA_BYTES).getMebiBytes());
    assertEquals(7, MemorySize.parse("7mb", MEGA_BYTES).getMebiBytes());
    assertEquals(7, MemorySize.parse("7 mb", MEGA_BYTES).getMebiBytes());
    assertEquals(7, MemorySize.parse("7mebibytes", MEGA_BYTES).getMebiBytes());
    assertEquals(7, MemorySize.parse("7 mebibytes", MEGA_BYTES).getMebiBytes());
  }

  @Test
  public void testDivideByLong() {
    final MemorySize memory = new MemorySize(100L);
    assertThat(memory.divide(23), is(new MemorySize(4L)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDivideByNegativeLong() {
    final MemorySize memory = new MemorySize(100L);
    memory.divide(-23L);
  }

  static Stream<Arguments> testToHumanReadableStringProvider() {
    return Stream.of(
        Arguments.of(0L, "0 bytes"),
        Arguments.of(1L, "1 bytes"),
        Arguments.of(1024L, "1024 bytes"),
        Arguments.of(1025L, "1.001kb (1025 bytes)"),
        Arguments.of(1536L, "1.500kb (1536 bytes)"),
        Arguments.of(1_000_000L, "976.563kb (1000000 bytes)"),
        Arguments.of(1_000_000_000L, "953.674mb (1000000000 bytes)"),
        Arguments.of(1_000_000_000_000L, "931.323gb (1000000000000 bytes)"),
        Arguments.of(1_000_000_000_000_000L, "909.495tb (1000000000000000 bytes)"));
  }

  @ParameterizedTest
  @MethodSource("testToHumanReadableStringProvider")
  public void testToHumanReadableString(long bytes, String expected) {
    assertThat(new MemorySize(bytes).toHumanReadableString(), is(expected));
  }
}
