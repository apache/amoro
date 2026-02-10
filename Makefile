# Makefile for Amoro + Kind (Spark on Kubernetes)
#
# Usage:
#   make setup-amoro  - One command to start everything (Kind + Docker services + optimizer)
#   make stop-amoro   - Stop Docker services (Kind cluster persists)
#   make teardown     - Remove everything (Kind cluster + Docker services + volumes)
#   make logs         - View Amoro logs
#   make status       - Show status of cluster and Amoro
#   make pods         - List Spark pods in kubernetes
#   make shell        - Open shell in Amoro container

COMPOSE_DIR := docker/kind
KIND_CLUSTER := amoro-cluster
KUBECTL := kubectl --context kind-$(KIND_CLUSTER)

.PHONY: setup-amoro stop-amoro teardown logs status pods shell help

# Default target
.DEFAULT_GOAL := help

help:
	@echo "Amoro + Kind (Spark on Kubernetes)"
	@echo ""
	@echo "Usage:"
	@echo "  make setup-amoro   Start everything (Kind cluster + all services + optimizer)"
	@echo "  make stop-amoro    Stop Docker services (Kind cluster persists)"
	@echo "  make restart-amoro Restart Docker services"
	@echo "  make teardown      Remove everything (Kind cluster + services + volumes)"
	@echo "  make logs          View Amoro logs"
	@echo "  make status        Show cluster and service status"
	@echo "  make pods          List Spark pods in Kubernetes"
	@echo "  make shell         Shell into Amoro container"
	@echo ""
	@echo "Access:"
	@echo "  Amoro Web UI : http://localhost:1630  (admin / admin)"
	@echo "  MinIO Console: http://localhost:9001  (admin / password)"
	@echo ""

setup-amoro:
	@echo "Starting Amoro (Kind cluster + all services)..."
	@docker compose -f $(COMPOSE_DIR)/docker-compose.yml up -d
	@echo ""
	@echo "Exporting Kind kubeconfig to host..."
	@kind export kubeconfig --name $(KIND_CLUSTER) 2>/dev/null
	@echo ""
	@echo "Follow progress:  make logs"
	@echo "Check status:     make status"

stop-amoro:
	@docker compose -f $(COMPOSE_DIR)/docker-compose.yml down

restart-amoro:
	@docker compose -f $(COMPOSE_DIR)/docker-compose.yml down
	@docker compose -f $(COMPOSE_DIR)/docker-compose.yml up -d
	@kind export kubeconfig --name $(KIND_CLUSTER) 2>/dev/null

teardown:
	@echo "Removing Kind clusters..."
	@kind delete cluster --name $(KIND_CLUSTER) 2>/dev/null 
	@kind delete cluster --name amoro-spark-cluster 2>/dev/null
	@echo "Removing Docker services and volumes..."
	@docker compose -f $(COMPOSE_DIR)/docker-compose.yml down -v
	@echo "Teardown complete."

logs:
	@docker compose -f $(COMPOSE_DIR)/docker-compose.yml logs -f amoro

status:
	@echo "=== Kind Cluster ==="
	@kind get clusters 2>/dev/null || echo "  No clusters"
	@echo ""
	@echo "=== Kubernetes Nodes ==="
	@$(KUBECTL) get nodes 2>/dev/null || echo "  Cannot connect to cluster (run: make setup-amoro)"
	@echo ""
	@echo "=== Docker Services ==="
	@docker compose -f $(COMPOSE_DIR)/docker-compose.yml ps
	@echo ""
	@echo "=== Spark Pods ==="
	@$(KUBECTL) get pods -n spark 2>/dev/null || echo "  No pods or cannot connect"

pods:
	@$(KUBECTL) get pods -n spark -o wide 2>/dev/null || echo "No pods or cannot connect"

shell:
	@docker exec -it amoro /bin/bash
