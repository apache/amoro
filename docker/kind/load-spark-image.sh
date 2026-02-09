#!/bin/bash

# Script to load the Spark optimizer Docker image into Kind cluster
# This makes the image available to Kubernetes pods

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

IMAGE_NAME="${1:-apache/amoro-spark-optimizer:latest}"
CLUSTER_NAME="${2:-amoro-spark-cluster}"

echo "Loading Spark optimizer image into Kind cluster..."
echo "Image: $IMAGE_NAME"
echo "Cluster: $CLUSTER_NAME"

# Check if image exists
if ! docker images | grep -q "$(echo $IMAGE_NAME | cut -d: -f1)"; then
    echo "Error: Image $IMAGE_NAME not found locally."
    echo "Please build it first:"
    echo "  cd /path/to/olake-amoro"
    echo "  ./docker/build.sh amoro-spark-optimizer"
    exit 1
fi

# Load image into Kind
kind load docker-image "$IMAGE_NAME" --name "$CLUSTER_NAME"

echo "✓ Image loaded successfully!"
echo ""
echo "You can verify by checking if pods can pull the image:"
echo "  kubectl get pods -n spark"
