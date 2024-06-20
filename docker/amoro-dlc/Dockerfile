# refer: https://git.woa.com/woodpecker/emr-base
FROM csighub.tencentyun.com/dlchub/dlc-base-maven:tlinux4.0-rc0 AS builder

ADD . /workspace/amoro
WORKDIR /workspace/amoro

RUN AMORO_VERSION=`cat pom.xml | grep 'amoro-parent' -C 3 | grep -Eo '<version>.*</version>' | awk -F'[><]' '{print $3}'` \
    && cp amoro-ams/dist/target/*.tar.gz /usr/local \
    && tar -xzvf /usr/local/apache-amoro-${AMORO_VERSION}-bin.tar.gz -C /usr/local \
    && cp docker/amoro/entrypoint.sh /usr/local/amoro \
    && rm /usr/local/amoro-${AMORO_VERSION}/plugin/optimizer/flink -rf \
    && rm /usr/local/amoro-${AMORO_VERSION}/plugin/optimizer/spark -rf \
    && mv /usr/local/amoro-${AMORO_VERSION} /usr/local/amoro \
    && rm -rf /workspace/amoro

RUN cd /usr/local/amoro/lib \
    && wget https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.30/mysql-connector-java-8.0.30.jar


FROM csighub.tencentyun.com/dlchub/dlc-base-maven:tlinux4.0-rc0

ENV TZ Asia/Shanghai
ENV AMORO_HOME /usr/local/amoro
ENV AMORO_CONF_DIR ${AMORO_HOME}/conf
ENV LOG_LEVEL info
EXPOSE 1630 1260 1261

RUN chmod +x ${AMORO_HOME}/entrypoint.sh
COPY --from=builder "/usr/local/amoro" "/usr/local/amoro"

WORKDIR ${AMORO_HOME}
ENTRYPOINT ["./entrypoint.sh", "ams"]
CMD ["help"]