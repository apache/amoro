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

"""Check zstd-jni attribution in actual shaded Flink runtime JARs.

Usage: python3 tools/releasing/check_runtime_licenses.py JAR_OR_DIRECTORY [...]
This verifies the known packaged license obligation, not overall ASF approval.
"""

import argparse
import json
from pathlib import Path
import sys
import zipfile


def check_jar(path):
    with zipfile.ZipFile(path) as archive:
        names = set(archive.namelist())
        if "com/github/luben/zstd/Zstd.class" not in names:
            return None
        issues = []
        main = archive.read("META-INF/LICENSE").decode("utf-8")
        license_path = "META-INF/LICENSE.zstd-jni-1.5.6-6"
        if "zstd-jni 1.5.6-6" not in main:
            issues.append("Main LICENSE omits the component and version")
        if license_path not in main:
            issues.append("Main LICENSE omits the bundled license reference")
        if license_path not in names:
            issues.append("Referenced license file is absent")
        else:
            text = archive.read(license_path).decode("utf-8")
            for required in ["Luben Karavelov", "Facebook, Inc.",
                             "Redistribution and use in source and binary forms",
                             "Neither the name Facebook", "DISCLAIMED"]:
                if required not in text:
                    issues.append("Incomplete license: " + required)
        return {"jar": str(path), "status": "FAIL" if issues else "PASS", "issues": issues}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("paths", nargs="+", type=Path)
    args = parser.parse_args()
    jars = set()
    for path in args.paths:
        if path.is_dir():
            jars.update(path.rglob("amoro-format-mixed-flink-runtime-*.jar"))
        else:
            jars.add(path)
    results = []
    for jar in sorted(jars):
        result = check_jar(jar)
        if result is not None:
            results.append(result)
    print(json.dumps(results, indent=2))
    return 0 if results and all(row["status"] == "PASS" for row in results) else 1


if __name__ == "__main__":
    sys.exit(main())
