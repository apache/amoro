package com.netease.arctic.utils.map;

public interface SimpleMap<T, K> {

  void put(T key, K value);

  void delete(T key);

  K get(T key);

  void close();
}
