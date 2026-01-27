#!/bin/bash

set -e

# 定义变量
BASE_DIR="/data/work/frame"
AMORO_DIR="${BASE_DIR}/amoro-0.9-SNAPSHOT"
AMORO_BACKUP_DIR="${BASE_DIR}/amoro-0.9-SNAPSHOT-BACK"
TAR_FILE="/tmp/apache-amoro-0.9-SNAPSHOT-bin.tar.gz"

echo "========================================="
echo "ECS 使用，开始更新 Amoro"
echo "========================================="

# 1. 停止 Amoro
echo "[1/5] 停止 Amoro 服务..."
APP_ID_FILE="${AMORO_DIR}/run/app.id"
PID=""
if [ -f "${APP_ID_FILE}" ]; then
    PID=$(cat "${APP_ID_FILE}")
    echo "获取到进程ID: ${PID}"
else
    echo "警告: 未找到 app.id 文件"
fi

cd "${AMORO_DIR}/bin"
./ams.sh stop

# 等待服务完全停止
if [ -n "${PID}" ]; then
    echo "等待进程 ${PID} 停止..."
    for i in {1..60}; do
        if ! ps -p "${PID}" > /dev/null 2>&1; then
            echo "进程已停止, 耗时 ${i} 秒"
            break
        fi
        if [ $i -eq 60 ]; then
            echo "警告: 进程 ${PID} 在60秒后仍在运行, 请手动检查"
        fi
        sleep 1
    done
else
    echo "未获取到进程ID, 等待 60 秒..."
    sleep 60
fi

# 2. 重命名旧版本目录为备份
echo "[2/5] 备份旧版本..."
cd "${BASE_DIR}"
if [ -d "${AMORO_BACKUP_DIR}" ]; then
    echo "警告: 备份目录 ${AMORO_BACKUP_DIR} 已存在, 将其删除"
    rm -rf "${AMORO_BACKUP_DIR}"
fi
mv "${AMORO_DIR}" "${AMORO_BACKUP_DIR}"

# 3. 解压新版本
echo "[3/5] 解压新版本..."
if [ ! -f "${TAR_FILE}" ]; then
    echo "错误: 安装包 ${TAR_FILE} 不存在!"
    exit 1
fi
tar -zxvf "${TAR_FILE}" -C "${BASE_DIR}/"

# 授权新目录所有文件权限为 777
echo "授权新目录文件权限..."
chmod -R 777 "${AMORO_DIR}"

# 4. 复制备份的配置文件
echo "[4/5] 恢复配置文件..."
cp -rf "${AMORO_BACKUP_DIR}/conf/"* "${AMORO_DIR}/conf/"

# 5. 启动 Amoro
echo "[5/5] 启动 Amoro 服务..."
export JAVA_HOME=/data/work/frame/jdk-11.0.29
export PATH=$JAVA_HOME/bin:$PATH
cd "${AMORO_DIR}/bin"
./ams.sh start

echo "========================================="
echo "Amoro 更新完成!"
echo "备份目录: ${AMORO_BACKUP_DIR}"
echo "========================================="
