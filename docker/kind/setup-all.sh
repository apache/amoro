#!/bin/bash

###############################################################################
# One-Command Setup: Amoro with Spark on Kind (Kubernetes in Docker)
#
# Usage: ./setup-all.sh [--skip-build] [--skip-cluster] [--force]
#
# This script is fully automated (non-interactive) and performs:
# 1. Builds the Spark optimizer Docker image (unless --skip-build)
# 2. Creates/recreates Kind cluster (unless --skip-cluster)
# 3. Configures RBAC for Spark
# 4. Loads Spark image into Kind
# 5. Generates kubeconfig with correct API server address
# 6. Starts Amoro container
###############################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$SCRIPT_DIR"

# Parse arguments
SKIP_BUILD=false
SKIP_CLUSTER=false
FORCE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --skip-cluster)
            SKIP_CLUSTER=true
            shift
            ;;
        --force)
            FORCE=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [--skip-build] [--skip-cluster] [--force]"
            echo ""
            echo "Options:"
            echo "  --skip-build    Skip building the Spark optimizer image"
            echo "  --skip-cluster  Skip Kind cluster creation (use existing)"
            echo "  --force         Force rebuild image and recreate cluster"
            echo ""
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

CLUSTER_NAME="amoro-spark-cluster"
SPARK_IMAGE="apache/amoro-spark-optimizer:latest"

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║    Amoro + Kind (Spark on Kubernetes) - Automated Setup   ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

#==============================================================================
# Step 1: Check Prerequisites
#==============================================================================
echo -e "${YELLOW}[1/8] Checking prerequisites...${NC}"

check_cmd() {
    if ! command -v "$1" &> /dev/null; then
        echo -e "${RED}✗ $1 is not installed${NC}"
        return 1
    fi
    echo -e "${GREEN}✓ $1 found${NC}"
    return 0
}

MISSING=0
check_cmd docker || MISSING=1

# Auto-install kind if missing
if ! command -v kind &> /dev/null; then
    echo -e "${YELLOW}⚠ Kind not found. Installing...${NC}"
    curl -sLo ./kind https://kind.sigs.k8s.io/dl/v0.20.0/kind-linux-amd64
    chmod +x ./kind
    sudo mv ./kind /usr/local/bin/kind
    echo -e "${GREEN}✓ Kind installed${NC}"
else
    echo -e "${GREEN}✓ kind found${NC}"
fi

# Auto-install kubectl if missing
if ! command -v kubectl &> /dev/null; then
    echo -e "${YELLOW}⚠ kubectl not found. Installing...${NC}"
    curl -sLO "https://dl.k8s.io/release/$(curl -sL https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
    chmod +x kubectl
    sudo mv kubectl /usr/local/bin/kubectl
    echo -e "${GREEN}✓ kubectl installed${NC}"
else
    echo -e "${GREEN}✓ kubectl found${NC}"
fi

if [ $MISSING -eq 1 ]; then
    echo -e "${RED}Missing required dependencies. Please install Docker first.${NC}"
    exit 1
fi

#==============================================================================
# Step 2: Build Spark Optimizer JAR (Maven)
#==============================================================================
echo ""
echo -e "${YELLOW}[2/8] Building Spark optimizer JAR...${NC}"

cd "$PROJECT_ROOT"

# Get Amoro version from pom.xml
AMORO_VERSION=$(cat pom.xml | grep 'amoro-parent' -C 3 | grep -Eo '<version>.*</version>' | awk -F'[><]' '{print $3}')
# JAR name includes Spark major version (3.5) and Scala version (2.12)
SPARK_OPTIMIZER_JAR="amoro-optimizer/amoro-optimizer-spark/target/amoro-optimizer-spark-3.5_2.12-${AMORO_VERSION}-jar-with-dependencies.jar"

if [ "$SKIP_BUILD" = true ]; then
    if [ ! -f "$SPARK_OPTIMIZER_JAR" ]; then
        echo -e "${RED}✗ Spark optimizer JAR not found and --skip-build specified${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Using existing Spark optimizer JAR (--skip-build)${NC}"
elif [ -f "$SPARK_OPTIMIZER_JAR" ] && [ "$FORCE" = false ]; then
    echo -e "${GREEN}✓ Spark optimizer JAR already exists${NC}"
else
    echo "Building Spark optimizer JAR with Maven (this may take several minutes)..."
    ./mvnw clean package -pl amoro-optimizer/amoro-optimizer-spark -am -e -DskipTests -Pspark-3.5 \
        -Dspotless.check.skip=true -Dspotless.apply.skip=true -q
    echo -e "${GREEN}✓ Spark optimizer JAR built${NC}"
fi

#==============================================================================
# Step 3: Build Spark Optimizer Docker Image
#==============================================================================
echo ""
echo -e "${YELLOW}[3/8] Building Spark optimizer Docker image...${NC}"

IMAGE_EXISTS=$(docker images -q apache/amoro-spark-optimizer 2>/dev/null | head -n1)

if [ "$SKIP_BUILD" = true ]; then
    if [ -z "$IMAGE_EXISTS" ]; then
        echo -e "${RED}✗ Spark optimizer image not found and --skip-build specified${NC}"
        echo "Run without --skip-build or build manually: ./docker/build.sh amoro-spark-optimizer"
        exit 1
    fi
    echo -e "${GREEN}✓ Using existing Spark optimizer image (--skip-build)${NC}"
elif [ -n "$IMAGE_EXISTS" ] && [ "$FORCE" = false ]; then
    echo -e "${GREEN}✓ Spark optimizer image already exists${NC}"
else
    echo "Building Spark optimizer Docker image..."
    ./docker/build.sh amoro-spark-optimizer
    
    # Tag with 'latest' for easier reference
    VERSIONED_TAG=$(docker images --format '{{.Repository}}:{{.Tag}}' | grep 'apache/amoro-spark-optimizer' | grep -v 'latest' | head -n1)
    if [ -n "$VERSIONED_TAG" ]; then
        docker tag "$VERSIONED_TAG" apache/amoro-spark-optimizer:latest
    fi
    echo -e "${GREEN}✓ Spark optimizer Docker image built and tagged as latest${NC}"
fi

cd "$SCRIPT_DIR"

#==============================================================================
# Step 4: Create Docker Network
#==============================================================================
echo ""
echo -e "${YELLOW}[4/8] Setting up Docker network...${NC}"

if ! docker network ls --format '{{.Name}}' | grep -q '^kind$'; then
    docker network create kind
    echo -e "${GREEN}✓ Created 'kind' network${NC}"
else
    echo -e "${GREEN}✓ 'kind' network exists${NC}"
fi

#==============================================================================
# Step 5: Create Kind Cluster
#==============================================================================
echo ""
echo -e "${YELLOW}[5/8] Setting up Kind cluster...${NC}"

CLUSTER_EXISTS=$(kind get clusters 2>/dev/null | grep -q "$CLUSTER_NAME" && echo "yes" || echo "no")

if [ "$SKIP_CLUSTER" = true ]; then
    if [ "$CLUSTER_EXISTS" = "no" ]; then
        echo -e "${RED}✗ Kind cluster not found and --skip-cluster specified${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Using existing Kind cluster (--skip-cluster)${NC}"
elif [ "$CLUSTER_EXISTS" = "yes" ] && [ "$FORCE" = false ]; then
    echo -e "${GREEN}✓ Kind cluster already exists${NC}"
else
    if [ "$CLUSTER_EXISTS" = "yes" ]; then
        echo "Deleting existing cluster..."
        kind delete cluster --name "$CLUSTER_NAME"
    fi
    
    echo "Creating Kind cluster..."
    kind create cluster --config kind-config.yaml --name "$CLUSTER_NAME"
    
    echo "Waiting for nodes to be ready..."
    kubectl wait --for=condition=Ready nodes --all --timeout=300s
    echo -e "${GREEN}✓ Kind cluster created${NC}"
fi

#==============================================================================
# Step 6: Configure RBAC
#==============================================================================
echo ""
echo -e "${YELLOW}[6/8] Configuring Kubernetes RBAC for Spark...${NC}"

kubectl apply -f spark-rbac.yaml
echo -e "${GREEN}✓ RBAC configured (namespace: spark, serviceAccount: spark)${NC}"

#==============================================================================
# Step 7: Load Image & Generate Kubeconfig
#==============================================================================
echo ""
echo -e "${YELLOW}[7/8] Loading image and generating kubeconfig...${NC}"

# Load Spark optimizer image into Kind
echo "Loading Spark optimizer image into Kind cluster..."
kind load docker-image "$SPARK_IMAGE" --name "$CLUSTER_NAME"
echo -e "${GREEN}✓ Image loaded into Kind${NC}"

# Generate kubeconfig with internal API server address
# The Kind control-plane container name is: {cluster-name}-control-plane
CONTROL_PLANE_CONTAINER="${CLUSTER_NAME}-control-plane"
CONTROL_PLANE_IP=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' "$CONTROL_PLANE_CONTAINER")

mkdir -p kubeconfig

# Export kubeconfig and fix the server address for container-to-container communication
kubectl config view --flatten --minify | \
    sed "s|https://127.0.0.1:6443|https://${CONTROL_PLANE_IP}:6443|g" | \
    sed "s|https://localhost:6443|https://${CONTROL_PLANE_IP}:6443|g" \
    > kubeconfig/config

chmod 644 kubeconfig/config
echo -e "${GREEN}✓ Kubeconfig generated with internal IP: ${CONTROL_PLANE_IP}${NC}"

#==============================================================================
# Step 8: Start Amoro
#==============================================================================
echo ""
echo -e "${YELLOW}[8/8] Starting Amoro...${NC}"

# Stop existing Amoro container if running
docker compose down 2>/dev/null || true

# Start Amoro
docker compose up -d

# Wait for Amoro to be healthy
echo "Waiting for Amoro to start..."
for i in {1..30}; do
    if curl -s http://localhost:1630 > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Amoro is running${NC}"
        break
    fi
    sleep 2
    echo -n "."
done
echo ""

#==============================================================================
# Summary
#==============================================================================
echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                    Setup Complete!                         ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${GREEN}Kubernetes Cluster:${NC}"
echo "  Cluster Name:     $CLUSTER_NAME"
echo "  API Server:       https://${CONTROL_PLANE_IP}:6443"
echo "  Spark Namespace:  spark"
echo "  Service Account:  spark"
echo ""
echo -e "${GREEN}Amoro Web UI:${NC} http://localhost:1630"
echo "  Login: admin / admin"
echo ""
echo -e "${GREEN}Next Step - Configure Spark Optimizer in Amoro:${NC}"
echo ""
echo "  1. Open Amoro UI: http://localhost:1630"
echo "  2. Go to: Settings → Containers → Add"
echo "  3. Use these settings:"
echo ""
echo "     Name:         sparkContainer"
echo "     Type:         Spark"
echo "     master:       k8s://https://${CONTROL_PLANE_IP}:6443"
echo "     deploy-mode:  cluster"
echo "     spark-home:   /opt/spark/"
echo "     job-uri:      local:///opt/spark/usrlib/optimizer-job.jar"
echo ""
echo "     Spark Config (add all):"
echo "       spark.kubernetes.container.image: apache/amoro-spark-optimizer:latest"
echo "       spark.kubernetes.container.image.pullPolicy: IfNotPresent"
echo "       spark.kubernetes.namespace: spark"
echo "       spark.kubernetes.authenticate.driver.serviceAccountName: spark"
echo "       spark.kubernetes.authenticate.executor.serviceAccountName: spark"
echo "       spark.driver.memory: 1g"
echo "       spark.executor.memory: 1g"
echo ""
echo -e "${YELLOW}Useful Commands:${NC}"
echo "  View Spark pods:  kubectl get pods -n spark"
echo "  View logs:        docker compose logs -f amoro"
echo "  Restart:          docker compose restart"
echo "  Stop all:         ./teardown.sh"
echo ""
