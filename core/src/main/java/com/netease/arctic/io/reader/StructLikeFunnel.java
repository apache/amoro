package com.netease.arctic.io.reader;

import com.netease.arctic.utils.SerializationUtil;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.StructLikeWrapper;
import org.apache.paimon.shade.guava30.com.google.common.hash.Funnel;
import org.apache.paimon.shade.guava30.com.google.common.hash.PrimitiveSink;
import org.jetbrains.annotations.NotNull;

public class StructLikeFunnel implements Funnel<StructLike> {
  private final StructLikeWrapper structLikeWrapper;
  private final SerializationUtil.StructLikeWrapperSerializer structLikeWrapperSerializer;

  public StructLikeFunnel(Types.StructType structType) {
    structLikeWrapper = StructLikeWrapper.forType(structType);
    structLikeWrapperSerializer =
        new SerializationUtil.StructLikeWrapperSerializer(structLikeWrapper);
  }

  /** Returns a funnel that extracts the bytes from a {@code StructLike}. */
  public static Funnel<StructLike> structLikeFunnel(Types.StructType structType) {
    return new StructLikeFunnel(structType);
  }

  @Override
  public void funnel(@NotNull StructLike structLike, PrimitiveSink primitiveSink) {
    primitiveSink.putBytes(
        structLikeWrapperSerializer.serialize(structLikeWrapper.copyFor(structLike)));
  }
}
