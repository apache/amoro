# 源镜像
FROM --platform=linux/amd64 ubuntu:22.04

# 维护者<姓名拼音>
MAINTAINER 'chenzhengyu'

ARG SOURCE_DIR=/data/services/lakehouse-manager
WORKDIR ${SOURCE_DIR}

# 查找 apache-amoro 开头的 tar.gz 包，解压到 /opt/amoro，然后删除压缩包
RUN mkdir -p /opt/amoro && \
    tar_file=$(find . -maxdepth 1 -name "apache-amoro*.tar.gz" -type f | head -n 1) && \
    if [ -n "$tar_file" ]; then \
      echo "Found: $tar_file"; \
      tar -xzf "$tar_file" -C /opt/amoro --strip-components=1; \
      rm -f "$tar_file"; \
    else \
      echo "Error: No apache-amoro*.tar.gz file found"; \
      exit 1; \
fi

# 设置环境变量
ENV AMORO_HOME /opt/amoro
ENV AMORO_CONF_DIR ${AMORO_HOME}/conf

EXPOSE 1630 1260 1261

WORKDIR ${AMORO_HOME}

# 授予 /opt/amoro 下所有文件 777 权限
RUN chmod -R 777 /opt/amoro && \
  chmod +x /opt/amoro/entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
CMD ["help"]