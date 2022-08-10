package org.apache.iceberg.data.parquet;

import org.apache.iceberg.parquet.AdaptHivePrimitiveWriter;
import org.apache.parquet.column.ColumnDescriptor;
import org.apache.parquet.io.api.Binary;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MICROSECONDS;

public class TimestampInt96Writer<T> extends AdaptHivePrimitiveWriter<T> {

  private static final long JULIAN_DAY_OF_EPOCH = 2440588L;
  private static final long MICROS_PER_DAY = 86400000000L;
  private static final long MILLIS_IN_DAY = TimeUnit.DAYS.toMillis(1);
  private static final long NANOS_PER_MILLISECOND = TimeUnit.MILLISECONDS.toNanos(1);

  public TimestampInt96Writer(ColumnDescriptor descriptor) {
    super(descriptor);
  }

  /**
   * Writes nano timestamps to parquet int96
   */
  void writeBinary(int repetitionLevel, int julianDay, long nanosOfDay) {
    ByteBuffer buf = ByteBuffer.allocate(12);
    buf.order(ByteOrder.LITTLE_ENDIAN);
    buf.putLong(nanosOfDay);
    buf.putInt(julianDay);
    buf.flip();
    column.writeBinary(repetitionLevel, Binary.fromConstantByteBuffer(buf));
  }

  /**
   * Writes Julian day and nanoseconds in a day from the number of microseconds
   *
   * @param value epoch time in microseconds
   */
  void writeLong(int repetitionLevel, long value) {
    long julianUs = value + JULIAN_DAY_OF_EPOCH * MICROS_PER_DAY;
    int day = (int) (julianUs / MICROS_PER_DAY);
    long nanos = MICROSECONDS.toNanos(julianUs % MICROS_PER_DAY);
    writeBinary(repetitionLevel, day, nanos);
  }

  /**
   * Writes Julian day and nanoseconds in a day from the local date time
   */
  void writeLocalDateTime(int repetitionLevel, LocalDateTime value) {
    long timestamp = value.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    int julianDay = (int) (timestamp / MILLIS_IN_DAY + 2440588L);
    long nanosOfDay = timestamp % MILLIS_IN_DAY * NANOS_PER_MILLISECOND + value.getNano() % NANOS_PER_MILLISECOND;
    writeBinary(repetitionLevel, julianDay, nanosOfDay);
  }

  @Override
  public void write(int repetitionLevel, T value) {
    if (value instanceof LocalDateTime) {
      writeLocalDateTime(repetitionLevel, (LocalDateTime) value);
    } else if (value instanceof Long) {
      writeLong(repetitionLevel, (Long) value);
    } else {
      throw new InvalidParameterException("unrecognized type: " + value.getClass());
    }
  }
}
