package com.netease.arctic.server.utils;

@FunctionalInterface
public interface SupplierWithException<T, E extends Exception> {
  T get() throws E;
}
