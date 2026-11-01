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

"""Verify known release-license repairs in actual JARs and binary TARs.

This regression check does not replace the complete amoro-release audit.
"""

import argparse
import hashlib
import io
import json
from pathlib import Path
import re
import sys
import tarfile
import zipfile

from check_javadoc_fat_licenses import check, check_embedded_resources


def require_terms(text, issues):
    for marker in ["The LevelDB Authors", "Julian", "Jean-loup Gailly",
                   "Mark Adler", "Yann Collet", "Facebook", "Google Inc.",
                   "Redistributions in binary form", "provided 'as-is'"]:
        if marker not in text:
            issues.append("Missing RocksDB native license text: " + marker)


def check_distribution_resources(jar, issues):
    names = jar.namelist()
    for name in names:
        if name.startswith("META-INF/native/") and "netty_tcnative_" in name:
            issues.append("Static tcnative payload remains: " + name)
        if "/servlet/jsp/resources/" in name:
            source = Path(__file__).parent / "jsp-resources" / Path(name).name
            if source.is_file() and source.suffix in (".xsd", ".dtd"):
                if jar.read(name) != source.read_bytes():
                    issues.append("JSP resource does not match owner-published replacement: " + name)
                for target in ["META-INF/licenses/LICENSE.jsp-resources", "META-INF/licenses/LICENSE.CDDL-1.1", "glassfish/bootstrap/legal/LICENSE.txt"]:
                    if target not in names:
                        issues.append("Missing replacement resource license: " + target)


def check_binary_attributions(jar, filename, main, readme, archive, root, issues, notice=""):
    names = jar.namelist()
    if any(n.startswith("org/apache/hadoop/shaded/org/xbill/DNS/")
           and n.endswith(".class") for n in names):
        target = "licenses/LICENSE.dnsjava"
        if "dnsjava:dnsjava:3.6.1" not in main or target not in main:
            issues.append("Missing shaded dnsjava component/terms pointer")
        member = root + "/" + target
        if member not in archive.getnames():
            issues.append("Missing dnsjava license text")
        elif "Copyright (c) 2019-2023, dnsjava authors" not in archive.extractfile(member).read().decode():
            issues.append("dnsjava copyright differs from upstream 3.6.1")
    if filename == "xmlenc-0.52.jar":
        target = "licenses/LICENSE.xmlenc-0.52"
        if "xmlenc:xmlenc:0.52" not in main or target not in main:
            issues.append("Missing xmlenc component/terms pointer")
        member = root + "/" + target
        if member not in archive.getnames():
            issues.append("Missing xmlenc original license text")
        else:
            # Exact original COPYRIGHT in the publisher's xmlenc-0.52.tgz.
            # Preserve all three conditions, the original owner and disclaimer.
            expected = "d33031741096b45fa5d1cc1d97374114d18801c834b4eddc1bee942355de6563dfe5d734e91231a27ccd327cf0cf0501071d1ff60875c642709a7bf8b6fa5ea8"
            if hashlib.sha512(archive.extractfile(member).read()).hexdigest() != expected:
                issues.append("xmlenc license differs from the complete upstream 0.52 COPYRIGHT")
    if filename.startswith("snappy-java-") and any(
            n.startswith("org/xerial/snappy/native/") and n.endswith((".so", ".dll", ".dylib", ".jnilib"))
            for n in names):
        target = "licenses/LICENSE.snappy-java"
        if not all(t in main for t in ["Snappy native compression library", "Bitshuffle", target]):
            issues.append("Missing Snappy native component/terms pointer")
        member = root + "/" + target
        if member not in archive.getnames():
            issues.append("Missing Snappy native license text")
        elif hashlib.sha512(archive.extractfile(member).read()).hexdigest() != "30b2483cd575206deda66919c4757ba978cbc5e459ed236160ef1c17f2318c2129b699a567d0d8a23f7fd10ca5c360ea1c7c1d5b1cda32716a0680ab18f24fd4":
            issues.append("Incomplete Snappy native grants/runtime exception")
        if "GCC Runtime Library Exception" not in " ".join(readme.split()) or "Snappy Java" not in readme:
            issues.append("Missing Snappy native README label")
        # Both producer versions declare static libstdc++; keep their notice.
        if "statically linked libstdc++" not in notice:
            issues.append("Missing Snappy upstream runtime notification")
    if filename == "jta-1.1.jar":
        if "lib/jta-1.1.jar (Hadoop 2 and Hadoop 3)" not in main:
            issues.append("JTA 1.1 incorrectly restricted to Hadoop 2")
    if filename == "amoro-shade-zookeeper-3-3.9.1-0.7.0-incubating.jar":
        if "org.apache.zookeeper:zookeeper:3.9.1" not in notice or "org.apache.zookeeper:zookeeper:3.1.1" in notice:
            issues.append("ZooKeeper NOTICE index differs from bundled 3.9.1")
    if filename == "kyuubi-hive-jdbc-shaded-1.10.2.jar":
        if any(n.startswith("org/slf4j/") and n.endswith(".class") for n in names):
            issues.append("Kyuubi SLF4J 1.x implementation was not stripped")
        target = "META-INF/README-amoro-resource-changes"
        if target not in names:
            issues.append("Modified Kyuubi JAR has no packaging-change notice")
        else:
            note = jar.read(target).decode()
            if not all(t in note for t in ["Kyuubi", "1.10.2", "org/slf4j/", "SLF4J 2"]):
                issues.append("Incomplete Kyuubi packaging-change notice")
        if "Kyuubi" not in readme or "SLF4J" not in readme:
            issues.append("Binary README does not disclose Kyuubi repackaging")


def check_jar(path):
    issues = []
    with zipfile.ZipFile(path) as jar:
        names = jar.namelist()
        check_distribution_resources(jar, issues)
        if any(n.endswith("/org/apache/zookeeper/version/Info.class") for n in names):
            notice = jar.read("META-INF/NOTICE").decode("utf-8")
            if "org.apache.zookeeper:zookeeper:3.1.1" in notice:
                issues.append("ZooKeeper NOTICE index differs from bundled 3.9.1")
        main = jar.read("META-INF/LICENSE").decode("utf-8")
        if any(n.startswith("org/xerial/snappy/native/") and n.endswith((".so", ".dll", ".dylib", ".jnilib")) for n in names):
            target = "META-INF/licenses/LICENSE.snappy-java"
            if "org.xerial.snappy:snappy-java:1.1.10.4" not in main or target not in main:
                issues.append("Snappy fat JAR declaration differs from actual 1.1.10.4 producer")
            if target not in names or hashlib.sha512(jar.read(target)).hexdigest() != "1be8b2b909a245df5cf0b4a6afe0e5c4b9824ddc272251ca662463bc86b21be308c775096dede3f5e84a2a06faf16733b6573abf30e352104438f294cbb31283":
                issues.append("Incomplete Snappy fat JAR grants/runtime exceptions")
        if len(names) != len(set(names)):
            issues.append("Duplicate archive members")
        if "-javadoc.jar" in path.name or "-jar-with-dependencies.jar" in path.name:
            issues.extend(check(path)["issues"])
        else:
            check_embedded_resources(jar, main, names, issues)
        schemas = [n for n in names if "/jakarta/servlet/resources/" in n and n.endswith((".xsd", ".dtd"))]
        psl = [n for n in names if n.endswith(("mozilla/public-suffix-list.txt", "org/publicsuffix/list/effective_tld_names.dat"))]
        for present, component, target in [
            (schemas, "Jakarta Servlet API 5.0.0", "META-INF/licenses/LICENSE.EPL-2.0"),
            (psl, "Public Suffix List", "META-INF/licenses/LICENSE.MPL-2.0"),
        ]:
            if present:
                if component not in main or target not in main or target not in names:
                    issues.append("Missing runtime-resource declaration or terms: " + component)
                if "META-INF/README-LICENSES" not in names:
                    issues.append("Missing runtime-resource README: " + component)
                elif component not in jar.read("META-INF/README-LICENSES").decode("utf-8"):
                    issues.append("Missing prominent runtime-resource label: " + component)
        if schemas and "org/apache/amoro/shade/thrift/jakarta/servlet/resources/" not in main:
            issues.append("Wrong package-local Servlet schema path")
        if any("org/apache/lucene/" in n and n.endswith(".class") for n in names):
            issues.append("Lucene implementation remains bundled despite test-only scope")
        if any("org/apache/amoro/utils/map/ObjectSizeEstimator" + suffix in names for suffix in [".class", ".java"]):
            notice = jar.read("META-INF/NOTICE").decode("utf-8")
            if "RamUsageEstimator" not in notice or "Copyright 2001-2021" not in notice:
                issues.append("Missing adapted memory-estimator attribution")
        if any("librocksdbjni" in n for n in names):
            target = "META-INF/licenses/LICENSE.rocksdb-native"
            for marker in ["RocksDB", "LevelDB", "bzip2 1.0.8", "zlib 1.2.13",
                           "LZ4 1.9.3", "Zstandard 1.4.9", "Snappy 1.1.8", target]:
                if marker not in main:
                    issues.append("Missing native component declaration: " + marker)
            if target not in names:
                issues.append("Missing native license document: " + target)
            else:
                require_terms(jar.read(target).decode("utf-8"), issues)
        if path.name.startswith("amoro-web-") and any(n.startswith("static/") for n in names):
            if "Bundled dashboard components" not in main:
                issues.append("Missing dashboard component index")
            for target in re.findall(r"See (licenses/[^\s]+)", main):
                target = target.rstrip(".")
                if target not in names or not jar.read(target):
                    issues.append("Missing dashboard license target: " + target)
            for target in ["licenses/LICENSE-apidom-embedded", "licenses/LICENSE-ietf-json-pointer"]:
                if target not in main or target not in names:
                    issues.append("Missing embedded frontend terms/pointer: " + target)
            notice = jar.read("META-INF/NOTICE").decode("utf-8")
            for owner in ["SmartBear", "Vladimír Gorej", "IETF Trust", "Joyent"]:
                if owner not in notice:
                    issues.append("Missing frontend NOTICE attribution: " + owner)
            for component, target, owner in [
                ("rolldown 1.2.5", "licenses/LICENSE-rolldown", "VoidZero"),
                ("@ant-design/icons-svg 4.4.2", "licenses/LICENSE-@ant-design-icons-svg", "Ant UED"),
            ]:
                if component not in main or target not in main or target not in names:
                    issues.append("Missing dashboard declaration: " + component)
                elif owner not in jar.read(target).decode("utf-8"):
                    issues.append("Missing dashboard copyright: " + owner)
    return issues


def check_tar(path):
    issues = []
    with tarfile.open(path) as archive:
        names = archive.getnames()
        root = next(n.rsplit("/", 1)[0] for n in names if n.count("/") == 1 and n.endswith("/LICENSE"))
        main = archive.extractfile(root + "/LICENSE").read().decode("utf-8")
        readme = archive.extractfile(root + "/README-LICENSES").read().decode("utf-8")
        notice = archive.extractfile(root + "/NOTICE").read().decode("utf-8")
        for name in names:
            if "/lib/amoro-web-" in name and name.endswith(".jar"):
                with zipfile.ZipFile(io.BytesIO(archive.extractfile(name).read())) as web:
                    if "Bundled dashboard components" not in web.read("META-INF/LICENSE").decode():
                        issues.append("Nested dashboard component index missing")
                    if "licenses/LICENSE-rolldown" not in web.namelist():
                        issues.append("Nested Rolldown terms missing")
        rocksdb_native = False
        for name in names:
            if name.endswith(".jar"):
                with zipfile.ZipFile(io.BytesIO(archive.extractfile(name).read())) as jar:
                    check_distribution_resources(jar, issues)
                    check_binary_attributions(jar, Path(name).name, main, readme,
                                              archive, root, issues, notice)
                    if any("librocksdbjni" in n for n in jar.namelist()):
                        rocksdb_native = True
        target = root + "/licenses/LICENSE.rocksdb-native"
        if rocksdb_native:
            if "licenses/LICENSE.rocksdb-native" not in main or target not in names:
                issues.append("Missing binary root RocksDB native terms/pointer")
            else:
                require_terms(archive.extractfile(target).read().decode(), issues)
        elif target in names or "RocksDB native components" in main:
            issues.append("RocksDB native attribution without a bundled native payload")
    return issues


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("paths", nargs="+", type=Path)
    args = parser.parse_args()
    paths = set()
    for path in args.paths:
        paths.update(path.rglob("*") if path.is_dir() else [path])
    selected = sorted(p for p in paths if p.is_file() and (
        (p.name.endswith(".jar") and any(t in p.name for t in ["-javadoc.jar", "-jar-with-dependencies.jar", "-runtime-", "amoro-web-", "amoro-format-iceberg-"]))
        or ("-bin-hadoop" in p.name and p.name.endswith(".tar.gz"))))
    if not selected:
        parser.error("No release JARs or binary TARs found")
    results = []
    for path in selected:
        issues = check_tar(path) if path.name.endswith(".tar.gz") else check_jar(path)
        results.append({"artifact": str(path), "sha512": hashlib.sha512(path.read_bytes()).hexdigest(),
                        "status": "FAIL" if issues else "PASS", "issues": issues})
    print(json.dumps(results, indent=2))
    return int(any(r["issues"] for r in results))


if __name__ == "__main__":
    sys.exit(main())
