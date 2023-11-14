package com.netease.arctic.io.reader;

import com.netease.arctic.utils.SerializationUtil;
import org.apache.iceberg.StructLike;
import org.apache.paimon.shade.guava30.com.google.common.hash.Funnel;
import org.apache.paimon.shade.guava30.com.google.common.hash.PrimitiveSink;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public enum StructLikeFunnel implements Funnel<StructLike> {
  INSTANCE;

  StructLikeFunnel() {}

  @Override
  public void funnel(@NotNull StructLike structLike, PrimitiveSink primitiveSink) {
    StructLike copy = SerializationUtil.StructLikeCopy.copy(structLike);
    try {
      primitiveSink.putBytes(SerializationUtil.kryoSerialize(copy));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
