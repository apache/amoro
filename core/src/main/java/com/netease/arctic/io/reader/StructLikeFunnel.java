package com.netease.arctic.io.reader;

import org.apache.iceberg.StructLike;
import org.apache.paimon.shade.guava30.com.google.common.hash.Funnel;
import org.apache.paimon.shade.guava30.com.google.common.hash.PrimitiveSink;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public enum StructLikeFunnel implements Funnel<StructLike> {
  INSTANCE;

  @Override
  public void funnel(StructLike structLike, PrimitiveSink primitiveSink) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      for (int i = 0; i < structLike.size(); i++) {
        Object obj = structLike.get(i, Object.class);
        oos.writeObject(obj);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    primitiveSink.putBytes(baos.toByteArray());
  }
  /** Returns a funnel that extracts the bytes from a {@code StructLike}. */
  public static Funnel<StructLike> structLikeFunnel() {
    return StructLikeFunnel.INSTANCE;
  }
}
