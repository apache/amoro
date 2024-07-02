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

package org.apache.amoro.optimizer.util;

import java.net.URL;
import java.net.URLClassLoader;

public class TaskClassLoader extends URLClassLoader {

  private static final TaskClassLoader INSTANCE = new TaskClassLoader();

  public static TaskClassLoader getInstance() {
    return INSTANCE;
  }

  public TaskClassLoader() {
    super(
        new URL[] {TaskClassLoader.class.getProtectionDomain().getCodeSource().getLocation()},
        Thread.currentThread().getContextClassLoader());
  }

  @Override
  public final Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    synchronized (getClassLoadingLock(name)) {
      // First, check if the class has already been loaded
      Class<?> c = findLoadedClass(name);

      if (c == null) {
        try {
          // check the URLs
          c = findClass(name);
        } catch (ClassNotFoundException e) {
          // let URLClassLoader do it, which will eventually call the parent
          c = super.loadClass(name, resolve);
        }
      } else if (resolve) {
        resolveClass(c);
      }

      return c;
    }
  }
}
