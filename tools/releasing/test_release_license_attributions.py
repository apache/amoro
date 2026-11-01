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


"""Protect complete upstream grants and per-distribution attribution."""
import io
from pathlib import Path
import re
import tarfile
import tempfile
import unittest
import zipfile

from check_release_license_repairs import check_tar, check_jar
from check_javadoc_fat_licenses import check as check_fat

ROOT = Path(__file__).resolve().parents[2]


class ReleaseLicenseAttributionsTest(unittest.TestCase):
    def check_archive(self, xmlenc=None, snappy=False, main=None, notice=None, terms=None):
        files = {
            "LICENSE": (main or (ROOT / "LICENSE-binary").read_text()).encode(),
            "NOTICE": (notice or (ROOT / "NOTICE-binary").read_text()).encode(),
            "README-LICENSES": (ROOT / "dist/src/main/amoro-bin/README-LICENSES").read_bytes(),
        }
        def jar(name, members):
            data = io.BytesIO()
            with zipfile.ZipFile(data, "w") as archive:
                for path, content in members.items():
                    archive.writestr(path, content)
            files["lib/" + name] = data.getvalue()
        if xmlenc is not None:
            files["licenses/LICENSE.xmlenc-0.52"] = xmlenc
            jar("xmlenc-0.52.jar", {"org/znerd/xmlenc/XMLOutputter.class": b"fixture"})
        if snappy:
            for version in ["1.1.8.4", "1.1.10.5"]:
                jar("snappy-java-" + version + ".jar", {
                    "org/xerial/snappy/native/Linux/x86_64/libsnappyjava.so": b"fixture"})
            target = ROOT / "licenses-binary/LICENSE.snappy-java"
            if target.exists():
                files["licenses/LICENSE.snappy-java"] = target.read_bytes() if terms is None else terms
        jar("jta-1.1.jar", {"javax/transaction/Transaction.class": b"fixture"})
        jar("amoro-shade-zookeeper-3-3.9.1-0.7.0-incubating.jar", {
            "org/apache/amoro/shade/zookeeper3/org/apache/zookeeper/version/Info.class": b"fixture"})
        with tempfile.TemporaryDirectory() as temp:
            path = Path(temp) / "fixture-bin-hadoop3.tar.gz"
            with tarfile.open(path, "w:gz") as archive:
                for name, content in files.items():
                    member = tarfile.TarInfo("amoro/" + name)
                    member.size = len(content)
                    archive.addfile(member, io.BytesIO(content))
            return check_tar(path)

    def test_fat_component_checks_do_not_depend_on_audit_snapshots_or_release_version(self):
        cases = [("amoro-metrics-prometheus", "maven:com.esotericsoftware:minlog:1.3.0"),
                 ("amoro-optimizer-flink", "args4j")]
        with tempfile.TemporaryDirectory() as temp:
            for module, component in cases:
                path = Path(temp) / (module + "-0.9.99-incubating-jar-with-dependencies.jar")
                with zipfile.ZipFile(path, "w") as archive:
                    archive.writestr("META-INF/LICENSE", (ROOT / "LICENSE").read_bytes())
                self.assertIn("Missing component attribution: " + component, check_fat(path)["issues"])

    def test_original_xmlenc_grant_passes(self):
        original = (ROOT / "licenses-binary/LICENSE.xmlenc-0.52").read_bytes()
        self.assertFalse([i for i in self.check_archive(xmlenc=original) if "xmlenc" in i])

    def test_each_xmlenc_condition_copyright_and_disclaimer_are_required(self):
        original = (ROOT / "licenses-binary/LICENSE.xmlenc-0.52").read_text()
        mutations = [re.sub(r"(?ms)^" + str(n) + r"\. .*?(?=\n\n)", "", original)
                     for n in [1, 2, 3]]
        mutations += [original.replace("<wfe.dehaan@gmail.com>", ""),
                      original.replace("LOSS OF USE, DATA, OR PROFITS", "")]
        for text in mutations:
            with self.subTest(text=text):
                self.assertNotEqual(text, original)
                self.assertTrue(any("xmlenc" in i for i in self.check_archive(xmlenc=text.encode())))

    def test_native_snappy_requires_root_disclosure_and_complete_terms(self):
        self.assertEqual(self.check_archive(snappy=True), [])
        main = (ROOT / "LICENSE-binary").read_text().replace("licenses/LICENSE.snappy-java", "missing-pointer")
        self.assertTrue(any("Snappy" in i for i in self.check_archive(snappy=True, main=main)))

    def test_native_snappy_truncated_grant_is_rejected(self):
        text = (ROOT / "licenses-binary/LICENSE.snappy-java").read_bytes()
        text = text.replace(b"IN NO EVENT SHALL THE", b"")
        self.assertTrue(any("Snappy" in i for i in self.check_archive(snappy=True, terms=text)))

    def test_jta_hadoop3_is_not_labeled_hadoop2_only(self):
        main = (ROOT / "LICENSE-binary").read_text().replace(
            "lib/jta-1.1.jar (Hadoop 2 and Hadoop 3)", "lib/jta-1.1.jar (Hadoop 2)")
        self.assertTrue(any("JTA" in i for i in self.check_archive(main=main)))

    def test_fat_snappy_actual_producer_version_is_checked(self):
        with tempfile.TemporaryDirectory() as temp:
            path = Path(temp) / "fixture.jar"
            for version in ["1.1.10.4", "1.1.10.5"]:
                with zipfile.ZipFile(path, "w") as archive:
                    archive.writestr("META-INF/LICENSE", (ROOT / "LICENSE").read_text() +
                                     "\norg.xerial.snappy:snappy-java:" + version +
                                     "\nMETA-INF/licenses/LICENSE.snappy-java")
                    archive.writestr("META-INF/NOTICE", "Snappy Java " + version + " upstream NOTICE")
                    archive.writestr("META-INF/licenses/LICENSE.snappy-java",
                                     (ROOT / "licenses/maven/fat-jar/LICENSE.snappy-java").read_bytes())
                    archive.writestr("org/xerial/snappy/native/Linux/x86_64/libsnappyjava.so", b"fixture")
                issues = check_jar(path)
                self.assertEqual(any("Snappy" in i for i in issues), version == "1.1.10.5")

    def test_independent_runtime_notice_is_checked(self):
        with tempfile.TemporaryDirectory() as temp:
            path = Path(temp) / "runtime.jar"
            for version in ["3.9.1", "3.1.1"]:
                with zipfile.ZipFile(path, "w") as archive:
                    archive.writestr("META-INF/LICENSE", (ROOT / "LICENSE").read_bytes())
                    archive.writestr("META-INF/NOTICE", "org.apache.zookeeper:zookeeper:" + version)
                    archive.writestr("shade/org/apache/zookeeper/version/Info.class", b"fixture")
                issues = check_jar(path)
                self.assertEqual(any("ZooKeeper" in i for i in issues), version == "3.1.1")

    def test_zookeeper_wrong_notice_version_is_rejected(self):
        notice = (ROOT / "NOTICE-binary").read_text().replace(
            "org.apache.zookeeper:zookeeper:3.9.1", "org.apache.zookeeper:zookeeper:3.1.1")
        self.assertTrue(any("ZooKeeper" in i for i in self.check_archive(notice=notice)))


if __name__ == "__main__":
    unittest.main()
