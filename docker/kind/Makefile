# Makefile for Amoro + Kind (Spark on Kubernetes)
#
# Usage:
#   make setup      - Full setup (build image, create cluster, start Amoro)
#   make start      - Start Amoro only (cluster must exist)
#   make stop       - Stop Amoro
#   make restart    - Restart Amoro
#   make teardown   - Remove everything (cluster + containers)
#   make logs       - View Amoro logs
#   make status     - Show status of cluster and Amoro
#   make pods       - List Spark pods in kubernetes
#   make shell      - Open shell in Amoro container

.PHONY: setup start stop restart teardown logs status pods spark-ui shell help

# Default target
.DEFAULT_GOAL := help

help:
	@echo "Amoro + Kind (Spark on Kubernetes) Management"
	@echo ""
	@echo "Usage:"
	@echo "  make setup       Full setup (build, create cluster, start Amoro)"
	@echo "  make setup-quick Skip image build if exists"
	@echo "  make start       Start Amoro (cluster must exist)"
	@echo "  make stop        Stop Amoro"
	@echo "  make restart     Restart Amoro"
	@echo "  make teardown    Remove everything"
	@echo "  make logs        View Amoro logs"
	@echo "  make status      Show status"
	@echo "  make pods        List Spark pods"
	@echo "  make spark-ui    Access Spark UI (port-forward to driver pod)"
	@echo "  make check       Run diagnostic check of setup"
	@echo "  make shell       Shell into Amoro container"
	@echo ""

setup:
	@./setup-all.sh --force

setup-quick:
	@./setup-all.sh

start:
	@docker compose up -d
	@echo "Amoro started. UI: http://localhost:1630"

stop:
	@docker compose down

restart:
	@docker compose restart

teardown:
	@./teardown.sh

logs:
	@docker compose logs -f amoro

status:
	@echo "=== Kind Cluster ==="
	@kind get clusters 2>/dev/null || echo "No clusters"
	@echo ""
	@echo "=== Kubernetes Nodes ==="
	@kubectl get nodes 2>/dev/null || echo "Cannot connect to cluster"
	@echo ""
	@echo "=== Amoro Container ==="
	@docker compose ps
	@echo ""
	@echo "=== Spark Pods ==="
	@kubectl get pods -n spark 2>/dev/null || echo "No pods or cannot connect"

pods:
	@kubectl get pods -n spark -o wide

spark-ui:
	@./access-spark-ui.sh

check:
	@./check-setup.sh

shell:
	@docker exec -it amoro /bin/bash
