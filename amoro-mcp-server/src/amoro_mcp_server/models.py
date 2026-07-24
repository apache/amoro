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

from typing import Literal

from pydantic import BaseModel, ConfigDict, Field

Identifier = str
Page = int
PageSize = int


class StrictParams(BaseModel):
    model_config = ConfigDict(extra="forbid", str_strip_whitespace=True)


class EmptyParams(StrictParams):
    pass


class CatalogParams(StrictParams):
    catalog: Identifier = Field(min_length=1, max_length=255)


class CatalogSearchParams(CatalogParams):
    keywords: str | None = Field(default=None, max_length=255)


class TableListParams(CatalogSearchParams):
    database: Identifier = Field(min_length=1, max_length=255)


class TableParams(StrictParams):
    catalog: Identifier = Field(min_length=1, max_length=255)
    database: Identifier = Field(min_length=1, max_length=255)
    table: Identifier = Field(min_length=1, max_length=255)


class PageParams(StrictParams):
    page: Page = Field(default=1, ge=1)
    page_size: PageSize = Field(default=20, ge=1, le=100)


class TablePageParams(TableParams, PageParams):
    pass


class OptimizingProcessParams(TablePageParams):
    type: str | None = Field(default=None, max_length=128)
    status: str | None = Field(default=None, max_length=128)


class OptimizingTaskParams(TablePageParams):
    process_id: Identifier = Field(min_length=1, max_length=255)


class SnapshotParams(TablePageParams):
    ref: Identifier | None = Field(default=None, min_length=1, max_length=255)
    commit_type: Literal["all", "optimizing", "non-optimizing"] = "all"


class OptimizerParams(PageParams):
    optimizer_group: Identifier = Field(default="all", min_length=1, max_length=50)


class OptimizingTableParams(PageParams):
    optimizer_group: Identifier = Field(min_length=1, max_length=50)
