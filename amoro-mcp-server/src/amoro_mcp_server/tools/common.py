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

from typing import Annotated

from pydantic import Field

READ_ONLY_ANNOTATIONS = {
    "readOnlyHint": True,
    "destructiveHint": False,
    "idempotentHint": True,
    "openWorldHint": True,
}

NameArg = Annotated[str, Field(min_length=1, max_length=255)]
GroupArg = Annotated[str, Field(min_length=1, max_length=50)]
OptionalKeywordArg = Annotated[str | None, Field(max_length=255)]
PageArg = Annotated[int, Field(ge=1)]
PageSizeArg = Annotated[int, Field(ge=1, le=100)]
