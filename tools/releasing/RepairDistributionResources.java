/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/** Repairs only generated distribution archives, never dependency-cache artifacts. */
public class RepairDistributionResources {
  private static final Set<String> JSP_FILES = Set.of(
      "jsp_2_0.xsd", "web-jsptaglibrary_1_1.dtd", "web-jsptaglibrary_1_2.dtd",
      "web-jsptaglibrary_2_0.xsd", "web-jsptaglibrary_2_1.xsd");
  private static final Set<String> STATIC_TCNATIVE = Set.of(
      "META-INF/native/libnetty_tcnative_linux_x86_64.so",
      "META-INF/native/libnetty_tcnative_linux_aarch_64.so",
      "META-INF/native/libnetty_tcnative_osx_x86_64.jnilib",
      "META-INF/native/libnetty_tcnative_osx_aarch_64.jnilib",
      "META-INF/native/netty_tcnative_windows_x86_64.dll");
  private static final String NATIVE_NOTICE = "\nAmoro TLS packaging change\n"
      + "Static netty-tcnative-boringssl libraries are removed from this derived archive.\n"
      + "ZooKeeper uses its default JDK TLS provider. Existing explicit OPENSSL provider\n"
      + "configurations must select JDK. Netty Java classes and unrelated epoll natives remain.\n"
      + "Original producer legal records are retained as provenance.\n";
  private static final String JSP_NOTICE = "\nAmoro JSP resource replacements\n"
      + "Five standard JSP schema/DTD resources, where present, are replaced with unmodified\n"
      + "GlassFish files from javaee/glassfish@371c9e1beb285a30bd4203ec86fde5729dbd06f9.\n"
      + "Amoro selects CDDL-1.1; see META-INF/licenses/LICENSE.jsp-resources and\n"
      + "META-INF/licenses/LICENSE.CDDL-1.1. Original resource paths and owner headers remain.\n";

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      throw new IllegalArgumentException("Expected repository root and module build directory");
    }
    Path root = Path.of(args[0]);
    Path target = Path.of(args[1]);
    List<Path> jars = new ArrayList<>();
    for (Path directory : List.of(target, target.resolve("amoro-ams-dependency/lib"))) {
      if (Files.isDirectory(directory)) {
        try (Stream<Path> files = Files.list(directory)) {
          jars.addAll(files.filter(p -> p.toString().endsWith(".jar"))
              .filter(p -> !p.getFileName().toString().matches("original-.*|.*-(sources|javadoc|tests)\\.jar"))
              .sorted().collect(Collectors.toList()));
        }
      }
    }
    for (Path jar : jars) {
      repair(root, jar);
    }
  }

  private static void repair(Path root, Path path) throws IOException {
    Map<String, byte[]> changes = new LinkedHashMap<>();
    Path temporary = null;
    try (ZipFile jar = new ZipFile(path.toFile())) {
      List<? extends ZipEntry> entries = Collections.list(jar.entries());
      Set<String> names = entries.stream().map(ZipEntry::getName).collect(Collectors.toSet());
      if (names.size() != entries.size()) {
        throw new IOException("Duplicate archive members: " + path);
      }
      boolean jsp = false;
      for (ZipEntry entry : entries) {
        for (String file : JSP_FILES) {
          if (entry.getName().equals("javax/servlet/jsp/resources/" + file)
              || entry.getName().equals("org/apache/hadoop/shaded/javax/servlet/jsp/resources/" + file)) {
            byte[] replacement = Files.readAllBytes(root.resolve("tools/releasing/jsp-resources/" + file));
            if (!java.util.Arrays.equals(read(jar, entry), replacement)) {
              changes.put(entry.getName(), replacement);
              jsp = true;
            }
          }
        }
      }
      if (jsp) {
        String main = names.contains("META-INF/LICENSE") ? "META-INF/LICENSE"
            : names.contains("META-INF/LICENSE.txt") ? "META-INF/LICENSE.txt" : "META-INF/LICENSE";
        String original = names.contains(main) ? new String(read(jar, jar.getEntry(main)), StandardCharsets.UTF_8) : "";
        changes.put(main, (original + JSP_NOTICE).getBytes(StandardCharsets.UTF_8));
        for (String license : List.of("LICENSE.jsp-resources", "LICENSE.CDDL-1.1")) {
          changes.put("META-INF/licenses/" + license, Files.readAllBytes(root.resolve("licenses-binary/" + license)));
        }
        changes.put("glassfish/bootstrap/legal/LICENSE.txt", Files.readAllBytes(root.resolve("licenses-binary/LICENSE.CDDL-1.1")));
        changes.put("META-INF/README-amoro-resource-changes", JSP_NOTICE.getBytes(StandardCharsets.UTF_8));
      }
      Set<String> removed = names.stream().filter(STATIC_TCNATIVE::contains).collect(Collectors.toSet());
      if (!removed.isEmpty()) {
        String note = "META-INF/README-amoro-resource-changes";
        byte[] existing = changes.get(note);
        if (existing == null) {
          existing = names.contains(note) ? read(jar, jar.getEntry(note)) : new byte[0];
        }
        changes.put(note, (new String(existing, StandardCharsets.UTF_8) + NATIVE_NOTICE).getBytes(StandardCharsets.UTF_8));
      }
      if (changes.isEmpty() && removed.isEmpty()) {
        return;
      }
      if (names.stream().anyMatch(n -> n.matches("(?i)META-INF/[^/]+\\.(SF|RSA|DSA|EC)"))) {
        throw new IOException("Refusing to invalidate signed dependency: " + path);
      }
      temporary = Files.createTempFile(path.getParent(), ".amoro-resources-", ".tmp");
      try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(temporary))) {
        for (ZipEntry entry : entries) {
          if (removed.contains(entry.getName())) {
            continue;
          }
          ZipEntry copy = new ZipEntry(entry.getName());
          copy.setTime(entry.getTime());
          output.putNextEntry(copy);
          if (changes.containsKey(entry.getName())) {
            output.write(changes.remove(entry.getName()));
          } else {
            try (InputStream input = jar.getInputStream(entry)) {
              input.transferTo(output);
            }
          }
          output.closeEntry();
        }
        for (Map.Entry<String, byte[]> entry : changes.entrySet()) {
          ZipEntry added = new ZipEntry(entry.getKey());
          added.setTime(315532800000L);
          output.putNextEntry(added);
          output.write(entry.getValue());
          output.closeEntry();
        }
      }
    } catch (IOException | RuntimeException e) {
      if (temporary != null) {
        Files.deleteIfExists(temporary);
      }
      throw e;
    }
    Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    System.out.println("Reconciled distribution resources: " + path);
  }

  private static byte[] read(ZipFile jar, ZipEntry entry) throws IOException {
    try (InputStream input = jar.getInputStream(entry)) {
      return input.readAllBytes();
    }
  }
}
