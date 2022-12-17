package com.netease.arctic.utils.map;

import com.netease.arctic.iceberg.optimize.StructLikeWrapper;
import com.netease.arctic.iceberg.optimize.StructLikeWrapperFactory;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.types.Types;

public abstract class StructLikeBaseMap<T> implements SimpleMap<StructLike, T> {

  protected final ThreadLocal<StructLikeWrapper> wrappers;
  protected final StructLikeWrapperFactory structLikeWrapperFactory;

  protected StructLikeBaseMap(Types.StructType type) {
    this.structLikeWrapperFactory = new StructLikeWrapperFactory(type);
    this.wrappers = ThreadLocal.withInitial(() -> structLikeWrapperFactory.create());
  }

  @Override
  public T get(StructLike key) {
    StructLikeWrapper wrapper = wrappers.get();
    T value = getInternalMap().get(wrapper.set((key)));
    wrapper.set(null); // don't hold a reference to the key.
    return value;
  }

  @Override
  public void put(StructLike key, T value) {
    getInternalMap().put(structLikeWrapperFactory.create().set(key), value);
  }

  @Override
  public void delete(StructLike key) {
    StructLikeWrapper wrapper = wrappers.get();
    getInternalMap().delete(wrapper.set(key));
    wrapper.set(null); // don't hold a reference to the key.
  }

  @Override
  public void close() {
    getInternalMap().close();
  }

  protected abstract SimpleMap<StructLikeWrapper, T> getInternalMap();
}
