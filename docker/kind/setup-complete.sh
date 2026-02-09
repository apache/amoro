#!/bin/bash

###############################################################################
# Complete Setup Script for Amoro with Kind (Kubernetes in Docker)
# This script does everything needed to run Amoro with Spark optimizer on Kind
###############################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# From docker/kind, go up 2 levels to reach project root
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Amoro + Kind Complete Setup${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Step 1: Check prerequisites
echo -e "${YELLOW}[1/7] Checking prerequisites...${NC}"
MISSING_DEPS=0

if ! command -v docker &> /dev/null; then
    echo -e "${RED}✗ Docker is not installed${NC}"
    MISSING_DEPS=1
else
    echo -e "${GREEN}✓ Docker found${NC}"
fi

if ! command -v kind &> /dev/null; then
    echo -e "${YELLOW}⚠ Kind is not installed. Installing...${NC}"
    curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.20.0/kind-linux-amd64
    chmod +x ./kind
    sudo mv ./kind /usr/local/bin/kind
    echo -e "${GREEN}✓ Kind installed${NC}"
else
    echo -e "${GREEN}✓ Kind found${NC}"
fi

if ! command -v kubectl &> /dev/null; then
    echo -e "${RED}✗ kubectl is not installed. Please install kubectl first.${NC}"
    echo "  Visit: https://kubernetes.io/docs/tasks/tools/"
    MISSING_DEPS=1
else
    echo -e "${GREEN}✓ kubectl found${NC}"
fi

if [ $MISSING_DEPS -eq 1 ]; then
    exit 1
fi

# Step 2: Build Spark optimizer image
echo ""
echo -e "${YELLOW}[2/7] Building Spark optimizer Docker image...${NC}"
cd "$PROJECT_ROOT"

if [ ! -f "$PROJECT_ROOT/docker/build.sh" ]; then
    echo -e "${RED}✗ Cannot find docker/build.sh. Are you in the correct directory?${NC}"
    exit 1
fi

# Check if image already exists
if docker images | grep -q "apache/amoro-spark-optimizer"; then
    echo -e "${YELLOW}⚠ Spark optimizer image already exists. Rebuild? (y/n)${NC}"
    read -r REBUILD
    if [ "$REBUILD" = "y" ] || [ "$REBUILD" = "Y" ]; then
        echo "Building Spark optimizer image..."
        ./docker/build.sh amoro-spark-optimizer
        echo -e "${GREEN}✓ Spark optimizer image built${NC}"
    else
        echo -e "${GREEN}✓ Using existing Spark optimizer image${NC}"
    fi
else
    echo "Building Spark optimizer image (this may take a few minutes)..."
    ./docker/build.sh amoro-spark-optimizer
    echo -e "${GREEN}✓ Spark optimizer image built${NC}"
fi

# Step 3: Setup Kind cluster
echo ""
echo -e "${YELLOW}[3/7] Setting up Kind cluster...${NC}"
cd "$SCRIPT_DIR"

# Create network if it doesn't exist
if ! docker network ls | grep -q "^.* kind "; then
    echo "Creating Docker network 'kind'..."
    docker network create kind
fi

# Delete existing cluster if it exists
if kind get clusters 2>/dev/null | grep -q "amoro-spark-cluster"; then
    echo -e "${YELLOW}⚠ Existing cluster found. Deleting...${NC}"
    kind delete cluster --name amoro-spark-cluster
fi

# Create Kind cluster
echo "Creating Kind cluster (this may take a minute)..."
kind create cluster --config kind-config.yaml --name amoro-spark-cluster

# Wait for cluster to be ready
echo "Waiting for cluster to be ready..."
kubectl wait --for=condition=Ready nodes --all --timeout=300s || {
    echo -e "${RED}✗ Cluster failed to become ready${NC}"
    exit 1
}
echo -e "${GREEN}✓ Kind cluster is ready${NC}"

# Step 4: Apply RBAC configuration
echo ""
echo -e "${YELLOW}[4/7] Configuring Kubernetes RBAC for Spark...${NC}"
kubectl apply -f spark-rbac.yaml
kubectl wait --for=condition=Ready serviceaccount/spark -n spark --timeout=60s || true
echo -e "${GREEN}✓ RBAC configured${NC}"

# Step 5: Load Spark optimizer image into Kind
echo ""
echo -e "${YELLOW}[5/7] Loading Spark optimizer image into Kind cluster...${NC}"
kind load docker-image apache/amoro-spark-optimizer:latest --name amoro-spark-cluster
echo -e "${GREEN}✓ Image loaded into Kind${NC}"

# Step 6: Export kubeconfig
echo ""
echo -e "${YELLOW}[6/7] Exporting kubeconfig...${NC}"
kubectl config view --flatten > kubeconfig
chmod 644 kubeconfig
echo -e "${GREEN}✓ Kubeconfig exported${NC}"

# Step 7: Get API server address and create config
echo ""
echo -e "${YELLOW}[7/7] Preparing configuration...${NC}"
API_SERVER=$(kubectl config view --minify -o jsonpath='{.clusters[0].cluster.server}')
echo -e "${GREEN}✓ Configuration ready${NC}"

# Create a summary
echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}Setup Complete!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "Kubernetes API Server: $API_SERVER"
echo "Cluster Name: amoro-spark-cluster"
echo "Spark Namespace: spark"
echo "Spark Service Account: spark"
echo ""
echo -e "${YELLOW}Next Steps:${NC}"
echo ""
echo "1. Start Amoro:"
echo "   ${BLUE}cd docker/kind${NC}"
echo "   ${BLUE}docker-compose up -d${NC}"
echo ""
echo "2. Configure Amoro (via Web UI at http://localhost:1630):"
echo "   - Login: admin/admin"
echo "   - Go to: Optimizing > Optimizer Groups > Add Group"
echo "   - Go to: Optimizing > Containers > Add Container"
echo "   - Use the configuration from: example-ams-config.yaml"
echo ""
echo "3. Or configure via config.yaml (if you have access to it):"
echo "   Add the container configuration from example-ams-config.yaml"
echo "   Make sure to update the master URL to: ${BLUE}k8s://$API_SERVER${NC}"
echo ""
echo -e "${YELLOW}Useful Commands:${NC}"
echo "  View Spark pods:     ${BLUE}kubectl get pods -n spark${NC}"
echo "  View cluster status: ${BLUE}kubectl get nodes${NC}"
echo "  Get API server:     ${BLUE}./get-api-server.sh${NC}"
echo "  Reload image:       ${BLUE}./load-spark-image.sh${NC}"
echo ""
echo -e "${GREEN}Setup complete! You can now start Amoro.${NC}"
