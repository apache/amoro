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