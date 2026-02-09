#!/bin/bash

###############################################################################
# Teardown script - Stops and removes all Amoro + Kind resources
###############################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

CLUSTER_NAME="amoro-spark-cluster"

echo -e "${YELLOW}Tearing down Amoro + Kind setup...${NC}"
echo ""

# Stop Amoro
echo "Stopping Amoro container..."
docker compose down 2>/dev/null || true
echo -e "${GREEN}✓ Amoro stopped${NC}"

# Delete Kind cluster
if kind get clusters 2>/dev/null | grep -q "$CLUSTER_NAME"; then
    echo "Deleting Kind cluster: $CLUSTER_NAME..."
    kind delete cluster --name "$CLUSTER_NAME"
    echo -e "${GREEN}✓ Kind cluster deleted${NC}"
else
    echo "Kind cluster not found, skipping..."
fi

# Clean up kubeconfig
if [ -f kubeconfig/config ]; then
    rm -f kubeconfig/config
    echo -e "${GREEN}✓ Kubeconfig cleaned${NC}"
fi

echo ""
echo -e "${GREEN}Teardown complete!${NC}"
echo ""
echo "To restart everything, run: ./setup-all.sh"
