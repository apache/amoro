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

package com.netease.arctic.optimizer.util;

import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * not thread-safe
 * Fix capacity, Reverse order traversal, Circular Array.
 */
public class CircularArray<E> implements Iterable<E>, Serializable {
  private final Object[] elementData;
  private int currentIndex;
  private boolean full = false;

  public CircularArray(int capacity) {
    Preconditions.checkArgument(capacity > 0);
    this.elementData = new Object[capacity];
    this.currentIndex = 0;
  }

  /**
   * not thread-safe
   *
   * @param element -
   */
  public void add(E element) {
    elementData[currentIndex] = element;
    currentIndex++;
    if (currentIndex == elementData.length) {
      currentIndex = 0;
      if (!full) {
        full = true;
      }
    }
  }

  public void clear() {
    Arrays.fill(elementData, null);
    currentIndex = 0;
    full = false;
  }
  
  public int size() {
    if (full) {
      return elementData.length;
    } else {
      return currentIndex;
    }
  }

  @NotNull
  @Override
  public Iterator<E> iterator() {
    return new ListIterator<>(currentIndex);
  }

  @Override
  public void forEach(Consumer<? super E> action) {
    Iterable.super.forEach(action);
  }

  @Override
  public Spliterator<E> spliterator() {
    return Iterable.super.spliterator();
  }

  private class ListIterator<E> implements Iterator<E> {
    private int nowIndex;
    private int cnt = 0;

    public ListIterator(int startIndex) {
      this.nowIndex = startIndex;
    }

    @Override
    public boolean hasNext() {
      if (full) {
        return cnt < elementData.length;
      } else {
        return nowIndex != 0;
      }
    }

    @Override
    public E next() {
      if (!hasNext()) {
        throw new IndexOutOfBoundsException("no more element");
      }
      nowIndex = getNextIndex();
      cnt++;
      return (E) elementData[nowIndex];
    }

    private int getNextIndex() {
      int next = nowIndex - 1;
      if (next == -1) {
        next = elementData.length - 1;
      }
      return next;
    }
  }
}
