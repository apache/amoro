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


"""Exercise release archive repairs without touching the Maven repository."""
import hashlib
import os
import shutil
import subprocess
import tempfile
import unittest
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TOOL = ROOT / "tools/releasing/RepairDistributionResources.java"
RESOURCES = ROOT / "tools/releasing/jsp-resources"
JAVA = str(Path(os.environ["JAVA_HOME"]) / "bin/java") if "JAVA_HOME" in os.environ else shutil.which("java")

class RepairDistributionResourcesTest(unittest.TestCase):
    def run_tool(self, directory, success=True):
        result = subprocess.run([JAVA, str(TOOL), str(ROOT), str(directory)], capture_output=True, text=True)
        self.assertEqual(result.returncode == 0, success, result.stdout + result.stderr)

    def test_replaces_existing_schemas_and_preserves_code_and_paths(self):
        with tempfile.TemporaryDirectory() as temp:
            target = Path(temp)
            jars = [target / "fat.jar", target / "amoro-ams-dependency/lib/hadoop.jar"]
            for path, prefix in zip(jars, ["javax/servlet/jsp/resources/", "org/apache/hadoop/shaded/javax/servlet/jsp/resources/"]):
                path.parent.mkdir(parents=True, exist_ok=True)
                with zipfile.ZipFile(path, "w") as jar:
                    jar.writestr("sample/Keep.class", b"unchanged class bytes")
                    jar.writestr(prefix + "jspxml.dtd", b"already Apache licensed")
                    jar.writestr(prefix + "jsp_2_0.xsd", b"old restricted schema")
            self.run_tool(target)
            for path, prefix in zip(jars, ["javax/servlet/jsp/resources/", "org/apache/hadoop/shaded/javax/servlet/jsp/resources/"]):
                with zipfile.ZipFile(path) as jar:
                    self.assertEqual(jar.read(prefix + "jsp_2_0.xsd"), (RESOURCES / "jsp_2_0.xsd").read_bytes())
                    self.assertEqual(jar.read(prefix + "jspxml.dtd"), b"already Apache licensed")
                    self.assertEqual(jar.read("sample/Keep.class"), b"unchanged class bytes")
                    self.assertNotIn(prefix + "web-jsptaglibrary_1_1.dtd", jar.namelist())
                    self.assertIn("CDDL", jar.read("META-INF/LICENSE").decode())
                    self.assertIn("META-INF/licenses/LICENSE.CDDL-1.1", jar.namelist())
                before = hashlib.sha512(path.read_bytes()).digest()
                self.run_tool(target)
                self.assertEqual(hashlib.sha512(path.read_bytes()).digest(), before)

    def test_leaves_unrelated_and_sources_jars_byte_identical(self):
        with tempfile.TemporaryDirectory() as temp:
            target = Path(temp)
            for name in ["unrelated.jar", "example-sources.jar", "original-runtime.jar"]:
                path = target / name
                with zipfile.ZipFile(path, "w") as jar:
                    jar.writestr("sample.class", b"unchanged")
                before = path.read_bytes()
                self.run_tool(target)
                self.assertEqual(path.read_bytes(), before)

    def test_removes_only_static_tcnative_payloads(self):
        with tempfile.TemporaryDirectory() as temp:
            path = Path(temp) / "runtime.jar"
            with zipfile.ZipFile(path, "w") as jar:
                jar.writestr("META-INF/native/libnetty_tcnative_linux_x86_64.so", b"static BoringSSL")
                jar.writestr("META-INF/native/libnetty_transport_native_epoll_x86_64.so", b"epoll must remain")
                jar.writestr("io/netty/handler/ssl/OpenSsl.class", b"Apache Java code must remain")
                jar.writestr("META-INF/LICENSE", b"Original Apache terms")
            self.run_tool(path.parent)
            with zipfile.ZipFile(path) as jar:
                self.assertNotIn("META-INF/native/libnetty_tcnative_linux_x86_64.so", jar.namelist())
                self.assertEqual(jar.read("META-INF/native/libnetty_transport_native_epoll_x86_64.so"), b"epoll must remain")
                self.assertEqual(jar.read("io/netty/handler/ssl/OpenSsl.class"), b"Apache Java code must remain")
                self.assertIn("JDK", jar.read("META-INF/README-amoro-resource-changes").decode())
            before = path.read_bytes()
            self.run_tool(path.parent)
            self.assertEqual(path.read_bytes(), before)

    def test_refuses_to_silently_invalidate_signed_input(self):
        with tempfile.TemporaryDirectory() as temp:
            path = Path(temp) / "signed.jar"
            with zipfile.ZipFile(path, "w") as jar:
                jar.writestr("javax/servlet/jsp/resources/jsp_2_0.xsd", b"old schema")
                jar.writestr("META-INF/OWNER.SF", b"signature metadata")
            before = path.read_bytes()
            self.run_tool(path.parent, success=False)
            self.assertEqual(path.read_bytes(), before)

if __name__ == "__main__":
    unittest.main()
