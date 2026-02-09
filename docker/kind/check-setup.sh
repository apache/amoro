#!/bin/bash

###############################################################################
# Diagnostic Script - Check Amoro + Kind Setup
# This script verifies all components needed for optimizer creation
###############################################################################

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║        Amoro + Kind Setup Diagnostic Check                ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

ERRORS=0

# Check 1: Amoro Container
echo -e "${YELLOW}[1/7] Checking Amoro Container...${NC}"
if docker compose ps amoro 2>/dev/null | grep -q "Up"; then
    echo -e "${GREEN}✓ Amoro container is running${NC}"
else
    echo -e "${RED}✗ Amoro container is not running${NC}"
    echo "  Fix: docker compose up -d"
    ERRORS=$((ERRORS + 1))
fi

# Check 2: Amoro UI Accessible
echo -e "${YELLOW}[2/7] Checking Amoro UI...${NC}"
if curl -s http://localhost:1630 > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Amoro UI is accessible at http://localhost:1630${NC}"
else
    echo -e "${RED}✗ Amoro UI is not accessible${NC}"
    echo "  Fix: Wait for Amoro to start or check logs: docker compose logs amoro"
    ERRORS=$((ERRORS + 1))
fi

# Check 3: Kind Cluster
echo -e "${YELLOW}[3/7] Checking Kind Cluster...${NC}"
if kind get clusters 2>/dev/null | grep -q "amoro-spark-cluster"; then
    echo -e "${GREEN}✓ Kind cluster 'amoro-spark-cluster' exists${NC}"
    
    # Check nodes
    NODES=$(kubectl get nodes --no-headers 2>/dev/null | wc -l)
    if [ "$NODES" -gt 0 ]; then
        echo -e "${GREEN}✓ Kubernetes nodes are ready ($NODES nodes)${NC}"
    else
        echo -e "${RED}✗ No Kubernetes nodes found${NC}"
        ERRORS=$((ERRORS + 1))
    fi
else
    echo -e "${RED}✗ Kind cluster not found${NC}"
    echo "  Fix: ./setup-all.sh"
    ERRORS=$((ERRORS + 1))
fi

# Check 4: Container Configuration
echo -e "${YELLOW}[4/7] Checking Container Configuration...${NC}"
if docker exec amoro grep -q "sparkContainer" /usr/local/amoro/conf/config.yaml 2>/dev/null; then
    echo -e "${GREEN}✓ sparkContainer configuration found in config.yaml${NC}"
    
    # Check master URL
    MASTER=$(docker exec amoro grep "master:" /usr/local/amoro/conf/config.yaml | grep -v "^#" | head -1 | awk '{print $2}')
    if [ -n "$MASTER" ]; then
        echo -e "${GREEN}✓ Master URL configured: $MASTER${NC}"
    else
        echo -e "${YELLOW}⚠ Master URL not found in config${NC}"
    fi
else
    echo -e "${RED}✗ sparkContainer not found in config.yaml${NC}"
    echo "  Fix: Check docker/kind/config.yaml"
    ERRORS=$((ERRORS + 1))
fi

# Check 5: Kubeconfig
echo -e "${YELLOW}[5/7] Checking Kubeconfig...${NC}"
if docker exec amoro test -f /root/.kube/config 2>/dev/null; then
    echo -e "${GREEN}✓ Kubeconfig file exists in Amoro container${NC}"
    
    # Check if it's readable
    if docker exec amoro cat /root/.kube/config 2>/dev/null | grep -q "server:"; then
        SERVER=$(docker exec amoro cat /root/.kube/config 2>/dev/null | grep "server:" | head -1 | awk '{print $2}')
        echo -e "${GREEN}✓ Kubeconfig contains server: $SERVER${NC}"
    else
        echo -e "${RED}✗ Kubeconfig appears invalid${NC}"
        ERRORS=$((ERRORS + 1))
    fi
else
    echo -e "${RED}✗ Kubeconfig not found in Amoro container${NC}"
    echo "  Fix: ./setup-all.sh (regenerates kubeconfig)"
    ERRORS=$((ERRORS + 1))
fi

# Check 6: Spark Image
echo -e "${YELLOW}[6/7] Checking Spark Optimizer Image...${NC}"
if docker images | grep -q "amoro-spark-optimizer"; then
    echo -e "${GREEN}✓ Spark optimizer image exists locally${NC}"
    
    # Check if loaded into Kind
    if kind get nodes --name amoro-spark-cluster 2>/dev/null | head -1 | xargs -I {} docker exec {} crictl images 2>/dev/null | grep -q "amoro-spark-optimizer"; then
        echo -e "${GREEN}✓ Spark optimizer image loaded into Kind cluster${NC}"
    else
        echo -e "${YELLOW}⚠ Spark optimizer image not loaded into Kind${NC}"
        echo "  Fix: kind load docker-image apache/amoro-spark-optimizer:latest --name amoro-spark-cluster"
    fi
else
    echo -e "${RED}✗ Spark optimizer image not found${NC}"
    echo "  Fix: ./docker/build.sh amoro-spark-optimizer"
    ERRORS=$((ERRORS + 1))
fi

# Check 7: RBAC Configuration
echo -e "${YELLOW}[7/7] Checking RBAC Configuration...${NC}"
if kubectl get serviceaccount spark -n spark > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Spark service account exists${NC}"
else
    echo -e "${RED}✗ Spark service account not found${NC}"
    echo "  Fix: kubectl apply -f spark-rbac.yaml"
    ERRORS=$((ERRORS + 1))
fi

if kubectl get clusterrolebinding spark-role-binding > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Spark RBAC role binding exists${NC}"
else
    echo -e "${RED}✗ Spark RBAC role binding not found${NC}"
    echo "  Fix: kubectl apply -f spark-rbac.yaml"
    ERRORS=$((ERRORS + 1))
fi

# Summary
echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
if [ $ERRORS -eq 0 ]; then
    echo -e "${BLUE}║                    ${GREEN}✓ All Checks Passed!${BLUE}                      ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${GREEN}You should be able to create optimizers now!${NC}"
    echo ""
    echo "Next steps:"
    echo "  1. Open Amoro UI: http://localhost:1630 (admin/admin)"
    echo "  2. Go to: Optimizing → Optimizer Groups"
    echo "  3. Click: Add Group"
    echo "  4. Select container: sparkContainer"
    echo "  5. Set thread count and save"
    echo ""
else
    echo -e "${BLUE}║              ${RED}✗ Found $ERRORS Issue(s)${BLUE}                        ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${YELLOW}Please fix the issues above and run this script again.${NC}"
    echo ""
    echo "For detailed troubleshooting, see: TROUBLESHOOTING.md"
    echo ""
    exit 1
fi
