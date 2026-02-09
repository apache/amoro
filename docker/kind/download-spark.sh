#!/bin/bash
#
# Downloads Apache Spark for use with Amoro's SparkOptimizerContainer
# This is required because the apache/amoro image doesn't include Spark binaries
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SPARK_DIR="${SCRIPT_DIR}/spark"

# Spark version should match what's used in the amoro-spark-optimizer image
SPARK_VERSION="3.5.0"
HADOOP_VERSION="3"
SPARK_PACKAGE="spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}"
SPARK_URL="https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/${SPARK_PACKAGE}.tgz"

if [ -d "${SPARK_DIR}/bin" ] && [ -f "${SPARK_DIR}/bin/spark-submit" ]; then
    echo "✅ Spark CLI already installed at ${SPARK_DIR}"
    exit 0
fi

echo "📦 Downloading Apache Spark ${SPARK_VERSION}..."
echo "   URL: ${SPARK_URL}"

# Create temp directory for download
TEMP_DIR=$(mktemp -d)
trap "rm -rf ${TEMP_DIR}" EXIT

# Download Spark
echo "⬇️  Downloading..."
curl -fSL "${SPARK_URL}" -o "${TEMP_DIR}/${SPARK_PACKAGE}.tgz"

# Extract to target directory
echo "📂 Extracting minimal files (bin/, conf/, jars/ only)..."
mkdir -p "${SPARK_DIR}"
tar -xzf "${TEMP_DIR}/${SPARK_PACKAGE}.tgz" -C "${TEMP_DIR}"

# Copy only essential files for spark-submit CLI (K8s submission)
# We only need: bin/ (spark-submit), conf/ (config), jars/ (libraries)
cp -r "${TEMP_DIR}/${SPARK_PACKAGE}/bin" "${SPARK_DIR}/"
cp -r "${TEMP_DIR}/${SPARK_PACKAGE}/conf" "${SPARK_DIR}/"
cp -r "${TEMP_DIR}/${SPARK_PACKAGE}/jars" "${SPARK_DIR}/"
if [ -f "${TEMP_DIR}/${SPARK_PACKAGE}/RELEASE" ]; then
    cp "${TEMP_DIR}/${SPARK_PACKAGE}/RELEASE" "${SPARK_DIR}/"
fi

echo ""
echo "✅ Minimal Spark ${SPARK_VERSION} installed successfully!"
echo "   Included: bin/, conf/, jars/ (CLI only for K8s submission)"
echo "   Excluded: examples/, python/, R/, yarn/, data/ (not needed)"
echo ""
echo "Next steps:"
echo "  1. Restart the Amoro container: docker compose down && docker compose up -d"
echo "  2. Create an optimizer group in the Amoro UI using container 'sparkContainer'"
echo ""
