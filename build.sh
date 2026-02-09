  #!/bin/bash
  # 作用：Maven编译并复制amoro包到output目录

  # 有错误立即停止
  set -e
  # 详细打印执行的命令
  set -x

  function build_and_copy() {
      # 定义源tar.gz包所在目录
      SOURCE_DIR="./dist/target"

      # Maven编译
      echo "[Starting Maven build...]"
      mvn -v
      mvn clean install -DskipTests -Drat.skip=true -Dspark.version=3.5.4 -P spark-3.5,scala-2.12,java11

      # 查找dist模块的tar.gz包
      if [ ! -d "$SOURCE_DIR" ]; then
          echo "Error: $SOURCE_DIR directory not found" >&2
          exit 1
      fi

      # 查找tar.gz文件
      TAR_FILE=$(ls "$SOURCE_DIR"/apache-amoro-*-bin.tar.gz 2>/dev/null | head -1)

      if [ -z "$TAR_FILE" ]; then
          echo "Error: No matching package found in $SOURCE_DIR (apache-amoro-*-bin.tar.gz)" >&2
          exit 1
      fi

      echo "Found package: $TAR_FILE"

      # 创建output目录并复制构建结果
      echo "Creating output directory and copying build result..."
      rm -rf output
      mkdir -p output
      cp "$TAR_FILE" output/
      echo "Build result copied to output/"
  }

  build_and_copy

  echo "[build and copy done!]"
  exit 0