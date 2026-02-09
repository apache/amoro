#!/bin/sh
set -eu

BASE_URL="http://localhost:1630"
HDR_SOURCE="X-Request-Source: Web"
COOKIE_JAR="/tmp/amoro-cookies.txt"

echo "Logging into Amoro..."
curl -sf -c "${COOKIE_JAR}" \
  -X POST "${BASE_URL}/api/ams/v1/login" \
  -H "Content-Type: application/json" \
  -H "${HDR_SOURCE}" \
  -d '{"user":"admin","password":"admin"}' >/dev/null

echo "Updating catalog 'olake_iceberg' with S3 credentials..."
curl -sf -b "${COOKIE_JAR}" \
  -X PUT "${BASE_URL}/api/ams/v1/catalogs/olake_iceberg" \
  -H "Content-Type: application/json" \
  -H "${HDR_SOURCE}" \
  -d '{
    "name":"olake_iceberg",
    "type":"custom",
    "optimizerGroup":"sparkContainer",
    "tableFormatList":["ICEBERG"],
    "storageConfig":{
      "storage.type":"S3",
      "storage.s3.endpoint":"http://172.22.0.101:9000",
      "storage.s3.region":"us-east-1"
    },
    "authConfig":{
      "auth.type":"AKSK",
      "auth.aksk.access-key":"admin",
      "auth.aksk.secret-key":"password"
    },
    "properties":{
      "catalog-impl":"org.apache.iceberg.jdbc.JdbcCatalog",
      "uri":"jdbc:postgresql://172.22.0.99:5432/iceberg?ssl=false",
      "jdbc.user":"iceberg",
      "jdbc.password":"password",
      "jdbc.driver":"org.postgresql.Driver",
      "jdbc.schema-version":"V1",
      "warehouse":"s3://warehouse/olake_iceberg/",
      "s3.endpoint":"http://172.22.0.101:9000",
      "s3.access-key-id":"admin",
      "s3.secret-access-key":"password",
      "s3.path-style-access":"true",
      "fs.s3a.endpoint":"http://172.22.0.101:9000",
      "fs.s3a.access.key":"admin",
      "fs.s3a.secret.key":"password",
      "fs.s3a.path.style.access":"true",
      "fs.s3a.connection.ssl.enabled":"false"
    },
    "tableProperties":{}
  }' >/dev/null

echo "✅ Catalog updated with S3 credentials!"
