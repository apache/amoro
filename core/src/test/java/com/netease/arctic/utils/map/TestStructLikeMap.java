package com.netease.arctic.utils.map;

import com.google.common.collect.Maps;
import com.netease.arctic.data.ChangedLsn;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.types.Types;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class TestStructLikeMap {
  private static final Random RANDOM = new Random(100000);

  private static Types.StructType TYPE = Types.StructType.of(
        Arrays.asList(new Types.NestedField[] {
                Types.NestedField.of(1, false,"c1", Types.DoubleType.get()),
                Types.NestedField.of(2, false,"c2", Types.IntegerType.get()),
                Types.NestedField.of(3, false,"c3", Types.BooleanType.get()),
                Types.NestedField.of(4, false,"c4", Types.StringType.get()),
                Types.NestedField.of(5, false,"c5", Types.BinaryType.get()) }
        )
  );
  private static class StructLikeImpl implements StructLike {

    private Object[] values = new Object[]{
            RANDOM.nextDouble(),
            RANDOM.nextInt(),
            RANDOM.nextBoolean(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString().getBytes("utf8")
    };

    StructLikeImpl() throws UnsupportedEncodingException {
    }
    @Override
    public int size() {
      return 5;
    }

    @Override
    public <T> T get(int pos, Class<T> javaClass) {
      return javaClass.cast(values[pos]);
    }

    @Override
    public <T> void set(int pos, T value) {
      throw new UnsupportedOperationException();
    }
  }

  @Test
  public void testMemoryMap() throws UnsupportedEncodingException {
    testMap(StructLikeMemoryMap.create(TYPE));
  }

  @Test
  public void testSpillableMap() throws UnsupportedEncodingException {
    testMap(StructLikeSpillableMap.create(TYPE,
            50L, "TEST"));
  }

  private void testMap(StructLikeBaseMap actualMap) throws UnsupportedEncodingException {
    Map<StructLike, ChangedLsn> expectedMap = Maps.newHashMap();
    for (long i = 0; i < 100; i++) {
      StructLikeImpl key = new StructLikeImpl();
      expectedMap.put(key, ChangedLsn.of(i, i));
      actualMap.put(key, ChangedLsn.of(i, i));
    }
    for (StructLike key : expectedMap.keySet()) {
      Assert.assertEquals(expectedMap.get(key), actualMap.get(key));
    }
    actualMap.close();
  }
}
