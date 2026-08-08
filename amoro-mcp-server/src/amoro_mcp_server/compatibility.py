# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

from __future__ import annotations

import re
from dataclasses import dataclass
from typing import Literal

CompatibilityStatus = Literal["supported", "unsupported", "unverified"]

MINIMUM_AMS_VERSION = (0, 8, 0)
SUPPORTED_AMS_LINES = {(0, 8), (0, 9)}
_VERSION_PATTERN = re.compile(
    r"^\s*(?P<major>\d+)\.(?P<minor>\d+)\.(?P<patch>\d+)"
    r"(?:[-+][0-9A-Za-z.-]+)?(?:\([^)]*\))?\s*$"
)


@dataclass(frozen=True)
class AmsCompatibility:
    normalized_version: str | None
    status: CompatibilityStatus


def classify_ams_version(raw_version: object) -> AmsCompatibility:
    """Normalize the public version while discarding suffixes and commit metadata."""

    if not isinstance(raw_version, str):
        return AmsCompatibility(None, "unverified")
    match = _VERSION_PATTERN.fullmatch(raw_version)
    if match is None:
        return AmsCompatibility(None, "unverified")

    version = tuple(int(match.group(part)) for part in ("major", "minor", "patch"))
    normalized = ".".join(str(part) for part in version)
    if version < MINIMUM_AMS_VERSION:
        return AmsCompatibility(normalized, "unsupported")
    if version[:2] in SUPPORTED_AMS_LINES:
        return AmsCompatibility(normalized, "supported")
    return AmsCompatibility(normalized, "unverified")
