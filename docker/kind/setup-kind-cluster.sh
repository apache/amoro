#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=========================================="
echo "Setting up Kind cluster for Spark"
echo "=========================================="

# Check if kind is installed
if ! command -v kind &> /dev/null; then
    echo "Kind is not installed. Installing..."
    # Install kind
    curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.20.0/kind-linux-amd64
    chmod +x ./kind
    sudo mv ./kind /usr/local/bin/kind
fi

# Check if kubectl is installed
if ! command -v kubectl &> /dev/null; then
    echo "kubectl is not installed. Please install kubectl first."
    exit 1
fi

# Create network for Kind (if it doesn't exist)
if ! docker network ls | grep -q "^.* kind "; then
    echo "Creating Docker network 'kind'..."
    docker network create kind
fi

# Delete existing cluster if it exists
if kind get clusters | grep -q "amoro-spark-cluster"; then
    echo "Deleting existing cluster..."
    kind delete cluster --name amoro-spark-cluster
fi

# Create Kind cluster
echo "Creating Kind cluster..."
kind create cluster --config kind-config.yaml --name amoro-spark-cluster

# Wait for cluster to be ready
echo "Waiting for cluster to be ready..."
kubectl wait --for=condition=Ready nodes --all --timeout=300s

# Apply RBAC configuration
echo "Creating Spark namespace and RBAC resources..."
kubectl apply -f spark-rbac.yaml

# Wait for service account
kubectl wait --for=condition=Ready serviceaccount/spark -n spark --timeout=60s || true

# Get API server address
API_SERVER=$(kubectl config view --minify -o jsonpath='{.clusters[0].cluster.server}')
echo ""
echo "=========================================="
echo "Kind cluster setup complete!"
echo "=========================================="
echo "Kubernetes API Server: $API_SERVER"
echo "Cluster Name: amoro-spark-cluster"
echo "Spark Namespace: spark"
echo "Spark Service Account: spark"
echo ""
echo "To use this cluster with Amoro, configure:"
echo "  master: k8s://$API_SERVER"
echo "  spark-conf.spark.kubernetes.namespace: spark"
echo "  spark-conf.spark.kubernetes.authenticate.driver.serviceAccountName: spark"
echo ""
echo "To export kubeconfig:"
echo "  kubectl config view --flatten > kubeconfig"
echo ""
echo "To start Amoro:"
echo "  docker-compose up -d"
echo "=========================================="

# Export kubeconfig for Amoro container
echo "Exporting kubeconfig..."
kubectl config view --flatten > kubeconfig
chmod 644 kubeconfig

# Check if Spark optimizer image exists and load it into Kind
echo ""
echo "Checking for Spark optimizer image..."
if docker images | grep -q "apache/amoro-spark-optimizer"; then
    echo "Found Spark optimizer image. Loading into Kind cluster..."
    kind load docker-image apache/amoro-spark-optimizer:latest --name amoro-spark-cluster
    echo "✓ Spark optimizer image loaded into Kind cluster"
else
    echo "⚠ Spark optimizer image not found. You need to build it first:"
    echo "  cd /path/to/olake-amoro"
    echo "  ./docker/build.sh amoro-spark-optimizer"
    echo "  Then run: kind load docker-image apache/amoro-spark-optimizer:latest --name amoro-spark-cluster"
fi

echo ""
echo "Setup complete! You can now start Amoro with: docker-compose up -d"
