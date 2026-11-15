#!/usr/bin/env python3
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

"""Verify Javadoc and fat-JAR component declarations against actual archives.

This checks component declarations and package-local license references;
it does not approve all third-party obligations or a release candidate.
"""

import argparse
import hashlib
import json
from pathlib import Path
import re
import sys
import zipfile


# Component expectations for the independent fat JAR distributions.
# Keep these checks independent of local audit reports and release timestamps.
FAT_JAR_COMPONENTS = {
    'amoro-metrics-prometheus': (
        'copied:ByteArrayWrapper',
        'maven:com.esotericsoftware:kryo:4.0.3',
        'maven:com.esotericsoftware:minlog:1.3.0',
        'maven:com.esotericsoftware:reflectasm:1.11.3',
        'maven:com.google.protobuf:protobuf-java:2.5.0',
        'maven:com.google.re2j:re2j:1.1',
        'maven:com.jcraft:jsch:0.1.55',
        'maven:com.sun.jersey:jersey-client:1.19.4',
        'maven:com.sun.jersey:jersey-core:1.19.4',
        'maven:com.sun.jersey:jersey-servlet:1.19.4',
        'maven:dnsjava:dnsjava:3.6.1',
        'maven:jakarta.activation:jakarta.activation-api:1.2.1',
        'maven:jakarta.xml.bind:jakarta.xml.bind-api:2.3.2',
        'maven:javax.annotation:javax.annotation-api:1.3.2',
        'maven:javax.servlet:javax.servlet-api:3.1.0',
        'maven:javax.ws.rs:jsr311-api:1.1.1',
        'maven:javolution:javolution:5.5.1',
        'maven:org.antlr:antlr-runtime:3.5.2',
        'maven:org.codehaus.woodstox:stax2-api:4.2.1',
        'maven:org.jline:jline-builtins:3.9.0',
        'maven:org.jline:jline-reader:3.9.0',
        'maven:org.jline:jline-remote-ssh:3.9.0',
        'maven:org.jline:jline-remote-telnet:3.9.0',
        'maven:org.jline:jline-style:3.9.0',
        'maven:org.jline:jline-terminal-jansi:3.9.0',
        'maven:org.jline:jline-terminal-jna:3.9.0',
        'maven:org.jline:jline-terminal:3.9.0',
        'maven:org.jline:jline:3.9.0',
        'maven:org.slf4j:slf4j-api:2.0.17',
        'maven:org.threeten:threeten-extra:1.7.1',
        'maven:sqlline:sqlline:1.3.0',
        'snappy-java',
    ),
    'amoro-optimizer-flink': (
        'args4j',
        'copied:ByteArrayWrapper',
        'maven:com.google.protobuf:protobuf-java:2.5.0',
        'maven:com.google.re2j:re2j:1.1',
        'maven:com.jcraft:jsch:0.1.55',
        'maven:com.sun.jersey:jersey-client:1.19.4',
        'maven:com.sun.jersey:jersey-core:1.19.4',
        'maven:com.sun.jersey:jersey-servlet:1.19.4',
        'maven:dnsjava:dnsjava:3.6.1',
        'maven:jakarta.activation:jakarta.activation-api:1.2.1',
        'maven:jakarta.xml.bind:jakarta.xml.bind-api:2.3.2',
        'maven:javax.annotation:javax.annotation-api:1.3.2',
        'maven:javax.servlet:javax.servlet-api:3.1.0',
        'maven:javax.ws.rs:jsr311-api:1.1.1',
        'maven:javolution:javolution:5.5.1',
        'maven:org.antlr:antlr-runtime:3.5.2',
        'maven:org.codehaus.woodstox:stax2-api:4.2.1',
        'maven:org.jline:jline-builtins:3.9.0',
        'maven:org.jline:jline-reader:3.9.0',
        'maven:org.jline:jline-remote-ssh:3.9.0',
        'maven:org.jline:jline-remote-telnet:3.9.0',
        'maven:org.jline:jline-style:3.9.0',
        'maven:org.jline:jline-terminal-jansi:3.9.0',
        'maven:org.jline:jline-terminal-jna:3.9.0',
        'maven:org.jline:jline-terminal:3.9.0',
        'maven:org.jline:jline:3.9.0',
        'maven:org.slf4j:slf4j-api:2.0.17',
        'maven:org.threeten:threeten-extra:1.7.1',
        'maven:sqlline:sqlline:1.3.0',
        'snappy-java',
    ),
}


def check_embedded_resources(jar, main, names, issues):
    web_components = [
        ("webapps/static/jquery/jquery-3.6.0.min.js", "jQuery 3.6.0"),
        ("webapps/static/jquery/jquery-ui-1.13.2.custom.min.js", "jQuery UI 1.13.2"),
        ("webapps/static/jquery/themes-1.9.1/base/jquery-ui.css", "jQuery UI theme 1.9.1"),
        ("webapps/static/dt-1.11.5/js/jquery.dataTables.min.js", "DataTables 1.11.5"),
        ("webapps/static/jt/jquery.jstree.js", "jsTree 1.0-rc3"),
    ]
    present = [component for member, component in web_components if member in names]
    for component in present:
        if component not in main:
            issues.append("Missing bundled Web UI attribution: " + component)
    if present:
        target = "META-INF/licenses/LICENSE.hadoop-web"
        if target not in names or target not in main:
            issues.append("Missing Hadoop Web UI license or pointer")
        else:
            terms = jar.read(target).decode("utf-8")
            if terms.count("Permission is hereby granted") < len(present):
                issues.append("Incomplete MIT terms for bundled Web UI components")
            for owner in ["OpenJS Foundation", "jQuery Foundation", "SpryMedia", "Ivan Bozhanov"]:
                if owner not in terms:
                    issues.append("Missing Web UI copyright: " + owner)
    native = [n for n in names if "tcnative" in n and
              n.endswith((".so", ".dll", ".jnilib", ".dylib"))]
    if any(b"BoringSSL" in jar.read(n) for n in native):
        target = "META-INF/license/LICENSE.boringssl.txt"
        if "BoringSSL" not in main or target not in main or target not in names:
            issues.append("Missing BoringSSL native attribution, terms or pointer")
        else:
            terms = jar.read(target).decode("utf-8")
            if not all(term in terms for term in ["OpenSSL", "SSLeay", "ISC", "MIT"]):
                issues.append("Incomplete BoringSSL component-specific terms")


def check(path):
    issues = []
    with zipfile.ZipFile(path) as jar:
        names = jar.namelist()
        if len(names) != len(set(names)):
            issues.append("Duplicate archive members")
        main = jar.read("META-INF/LICENSE").decode("utf-8")
        if path.name.endswith("-javadoc.jar"):
            if "legal/LICENSE" in names:
                if main.count("Additional components in the generated Javadoc") != 1:
                    issues.append("Missing or repeated Javadoc license addendum")
                for member in ["legal/LICENSE", "legal/ADDITIONAL_LICENSE_INFO",
                               "legal/ASSEMBLY_EXCEPTION", "legal/jquery.md",
                               "legal/jqueryUI.md", "legal/jszip.md", "legal/pako.md"]:
                    if member not in names or member not in main:
                        issues.append("Missing generated-asset license or pointer: " + member)
        else:
            check_embedded_resources(jar, main, names, issues)
            components = next((items for module, items in FAT_JAR_COMPONENTS.items()
                               if path.name.startswith(module + "-")), ())
            for component in components:
                token = component.split(":")[-2] if component.startswith("maven:") else component
                if token == "copied:ByteArrayWrapper":
                    token = "ByteArrayWrapper"
                if token.lower() not in main.lower():
                    issues.append("Missing component attribution: " + component)
            for member in re.findall(r"META-INF/licenses/([^\s]+)", main):
                target = "META-INF/licenses/" + member.rstrip(".")
                if target not in names or jar.getinfo(target).file_size == 0:
                    issues.append("Missing license target: " + target)
            if "META-INF/README-LICENSES" not in names:
                issues.append("Missing third-party licensing README")
            if not any(n.startswith("META-INF/dependency-notices/") for n in names):
                issues.append("Original dependency notices not preserved separately")
    return {"jar": str(path), "sha512": hashlib.sha512(path.read_bytes()).hexdigest(),
            "status": "FAIL" if issues else "PASS", "issues": issues}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("paths", nargs="+", type=Path)
    args = parser.parse_args()
    paths = set()
    for path in args.paths:
        paths.update(path.rglob("*.jar") if path.is_dir() else [path])
    selected = sorted(p for p in paths if (p.name.endswith("-javadoc.jar") or (p.name.endswith("-jar-with-dependencies.jar") and p.name.startswith(("amoro-metrics-prometheus-", "amoro-optimizer-flink-")))))
    if not selected:
        parser.error("No javadoc or jar-with-dependencies archives found")
    results = [check(path) for path in selected]
    print(json.dumps(results, indent=2))
    return int(any(r["status"] != "PASS" for r in results))


if __name__ == "__main__":
    sys.exit(main())
