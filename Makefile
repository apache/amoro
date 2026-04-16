#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Modified by Datazip Inc. in 2026

COMPOSE_DIR  := local-test
KIND_CLUSTER := fusion-cluster
FUSION_LOG_DIR ?= $(CURDIR)/docker/kind/fusion-logs
DIST_TAR     := $(CURDIR)/dist/target/apache-amoro-0.9-SNAPSHOT-bin.tar.gz
RUNTIME_HOME := $(CURDIR)/dist/target/amoro-0.9-SNAPSHOT
BIN_HOME     := $(CURDIR)/dist/src/main/amoro-bin

# Auto-detect JDK 17: prefer JAVA_HOME if already JDK 17,
# then macOS java_home utility, then common Linux paths.
JAVA_HOME_17 := $(shell \
  _cur="$(JAVA_HOME)"; \
  if [ -n "$$_cur" ] && "$$_cur/bin/java" -version 2>&1 | grep -q '"17\.'; then \
    echo "$$_cur"; \
  elif [ "$$(uname -s)" = "Darwin" ]; then \
    /usr/libexec/java_home -v 17 2>/dev/null || true; \
  else \
    for d in /usr/lib/jvm/java-17-openjdk-amd64 /usr/lib/jvm/java-17-openjdk \
              /usr/lib/jvm/temurin-17 /usr/lib/jvm/jdk-17; do \
      [ -x "$$d/bin/java" ] && echo "$$d" && break; \
    done; \
  fi)

MVN := JAVA_HOME="$(JAVA_HOME_17)" PATH="$(JAVA_HOME_17)/bin:$(PATH)" ./mvnw

.DEFAULT_GOAL := help

.PHONY: help build sync-libs setup-debug-mode clean-debug-mode \
        start-deps stop-deps start-fusion-docker clean-fusion-docker \
        sync-frontend spotless-fix

help:
	@echo "Fusion (Amoro) Development Makefile"
	@echo ""
	@echo "Development:"
	@echo "  make build                 Clean build + install to ~/.m2 + sync optimizer libs (requires JDK 17)"
	@echo "  make sync-libs             Re-extract dist tar and sync optimizer libs (no rebuild)"
	@echo "  make setup-debug-mode      Start deps + build (one-shot setup for IDE debugging)"
	@echo "  make clean-debug-mode      Stop deps + cleanup extracted runtime"
	@echo "  make sync-frontend         Sync built frontend assets to target/ (fixes blank UI)"
	@echo "  make spotless-fix          Auto-fix Spotless (Google Java Format) violations"
	@echo ""
	@echo "Docker:"
	@echo "  make start-deps            Start Postgres and Minio for IDE debugging"
	@echo "  make stop-deps             Stop Postgres and Minio"
	@echo "  make start-fusion-docker   Start Kind cluster + all services + optimizer"
	@echo "  make clean-fusion-docker   Remove Kind cluster + services + volumes"
	@echo ""
	@echo "Access:"
	@echo "  Fusion Web UI : http://localhost:1630  (admin / password)"
	@echo "  MinIO Console : http://localhost:9001  (admin / password)"

# ---------------------------------------------------------------------------
# Build
# ---------------------------------------------------------------------------

build:
	@if [ -z "$(JAVA_HOME_17)" ]; then \
		echo "ERROR: JDK 17 not found. Install JDK 17 or set JAVA_HOME."; \
		exit 1; \
	fi
	@echo "Using JDK 17: $(JAVA_HOME_17)"
	@find "$(BIN_HOME)/logs" -name 'optimizer-local-test-*' -exec rm -rf {} + 2>/dev/null || true
	@echo "Building all modules..."
	@$(MVN) clean install -DskipTests -Drat.skip=true -Dmaven.clean.failOnError=false -B -ntp
	@$(MAKE) sync-libs

sync-libs:
	@if [ ! -f "$(DIST_TAR)" ]; then \
		echo "ERROR: $(DIST_TAR) not found. Run 'make build' first."; \
		exit 1; \
	fi
	@rm -rf "$(RUNTIME_HOME)"
	@tar -xzf "$(DIST_TAR)" -C "$(CURDIR)/dist/target"
	@rm -rf "$(BIN_HOME)/lib"
	@cp -R "$(RUNTIME_HOME)/lib" "$(BIN_HOME)/lib"
	@echo "Synced optimizer libs → $(BIN_HOME)/lib"

# ---------------------------------------------------------------------------
# Debug mode (IDE workflow)
# ---------------------------------------------------------------------------

setup-debug-mode: start-deps build
	@echo ""
	@echo "Setup complete. Run 'AmoroServiceContainer' from your IDE. See .vscode/debug.md"

clean-debug-mode: stop-deps
	@rm -rf "$(RUNTIME_HOME)"
	@echo "Teardown complete."

# ---------------------------------------------------------------------------
# Docker
# ---------------------------------------------------------------------------

start-deps:
	@echo "Starting local dependencies (Postgres & Minio)..."
	@docker compose -f $(COMPOSE_DIR)/docker-compose.yml --profile dev up -d

stop-deps:
	@echo "Stopping local dependencies..."
	@docker compose -f $(COMPOSE_DIR)/docker-compose.yml --profile dev down

start-fusion-docker:
	@echo "Starting Fusion (Kind cluster + all services)..."
	@mkdir -p "$(FUSION_LOG_DIR)"
	@FUSION_LOG_DIR="$(FUSION_LOG_DIR)" docker compose -f $(COMPOSE_DIR)/docker-compose.yml --profile prod up -d
	@kind export kubeconfig --name $(KIND_CLUSTER) 2>/dev/null

clean-fusion-docker:
	@kind delete cluster --name $(KIND_CLUSTER) 2>/dev/null || true
	@kind delete cluster --name fusion-spark-cluster 2>/dev/null || true
	@docker compose -f $(COMPOSE_DIR)/docker-compose.yml --profile prod down -v
	@echo "Teardown complete."

# ---------------------------------------------------------------------------
# Utilities
# ---------------------------------------------------------------------------

spotless-fix:
	@$(MVN) spotless:apply -B -ntp

sync-frontend:
	@SRC=amoro-web/src/main/resources/static; \
	DST=amoro-web/target/classes/static; \
	if [ ! -d "$$SRC" ]; then \
		echo "ERROR: $$SRC not found. Run 'pnpm build' inside amoro-web/ first."; \
		exit 1; \
	fi; \
	mkdir -p "$$DST"; \
	cp -r "$$SRC"/. "$$DST"/
	@echo "Frontend synced. Refresh http://localhost:1630"
