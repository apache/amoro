<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements. See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership. The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied. See the License for the
  specific language governing permissions and limitations
  under the License.
-->

# Grafana Dashboard Import Guide

## Overview

This guide explains how to import and configure the provided Grafana dashboard.

## Steps to Import

1. **Download JSON**: Get the `dashboard.json` file.
2. **Log in to Grafana**: Access your Grafana instance.
3. **Import Dashboard**:
    - Go to the "+" menu, select "Import".
    - Upload the JSON file or paste its contents.
4. **Configure Data Sources**: Map any required data sources to your existing ones.
5. **Save**: Confirm and save the dashboard.

## Notes

- Ensure data sources are properly configured. For details, please refer to: [Grafana data source](https://grafana.com/docs/grafana/latest/datasources/prometheus/).
- Customize variables, thresholds, and layouts as needed. For details, please refer to: [Grafana build dashboards](https://grafana.com/docs/grafana/latest/dashboards/build-dashboards/).