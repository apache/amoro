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

package org.apache.amoro.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestSimpleSerializer {

  @Test
  public void testDeserializeClassOnlyVisibleFromThreadContextClassLoader() throws Exception {
    Path tempDir = Files.createTempDirectory("amoro-simple-serializer");
    Path sourceDir = tempDir.resolve("src").resolve("dynamic");
    Path classesDir = tempDir.resolve("classes");
    Files.createDirectories(sourceDir);
    Files.createDirectories(classesDir);

    Path sourceFile = sourceDir.resolve("TaskPayload.java");
    Files.write(
        sourceFile,
        ("package dynamic;\n"
                + "import java.io.Serializable;\n"
                + "public class TaskPayload implements Serializable {\n"
                + "  private static final long serialVersionUID = 1L;\n"
                + "  private final String value;\n"
                + "  public TaskPayload(String value) { this.value = value; }\n"
                + "  public String value() { return value; }\n"
                + "}\n")
            .getBytes(StandardCharsets.UTF_8));

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    assertNotNull("The test must run with a JDK, not a JRE.", compiler);
    assertEquals(
        0,
        compiler.run(
            null, null, null, "-d", classesDir.toString(), sourceFile.toAbsolutePath().toString()));

    URL[] urls = new URL[] {classesDir.toUri().toURL()};
    try (URLClassLoader payloadLoader = new URLClassLoader(urls, null)) {
      Class<?> payloadClass = Class.forName("dynamic.TaskPayload", true, payloadLoader);
      Constructor<?> constructor = payloadClass.getConstructor(String.class);
      Object payload = constructor.newInstance("paimon-primary-key");

      SimpleSerializer<Object> serializer = new SimpleSerializer<>();
      byte[] bytes = serializer.serializeResource(payload);

      Thread thread = Thread.currentThread();
      ClassLoader originalContextClassLoader = thread.getContextClassLoader();
      try {
        thread.setContextClassLoader(payloadLoader);

        Object deserialized = serializer.deserializeResource(bytes);

        assertEquals(payloadClass, deserialized.getClass());
        assertEquals("paimon-primary-key", payloadClass.getMethod("value").invoke(deserialized));
      } finally {
        thread.setContextClassLoader(originalContextClassLoader);
      }
    }
  }
}
