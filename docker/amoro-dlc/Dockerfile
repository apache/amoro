# refer https://git.code.oa.com/woodpecker/emr-base
FROM csighub.tencentyun.com/dlchub/dlc-base-maven:tlinux4.0-rc0 AS builder
ADD . /root/

RUN cd /root/   \
    && mvn clean package -pl amoro-ams/dist  -am -e -DskipTests -Ptencent-dlc
RUN yum install unzip -y

RUN mkdir -p /workspace

RUN pwd \
    && ls /root/

RUN AMORO_VERSION=`cat /root/pom.xml | grep 'amoro-parent' -C 3 | grep -Eo '<version>.*</version>' | awk -F'[><]' '{print $3}'` \
    && cp /root/amoro-ams/dist/target/*.zip /workspace/ \
    && unzip /workspace/amoro-${AMORO_VERSION}-bin.zip -d /workspace \
    && mv /workspace/amoro-${AMORO_VERSION} /workspace/amoro  \
    && echo "${AMORO_VERSION}" > /workspace/amoro/versions  \
    && cp /root/docker/amoro/entrypoint.sh /workspace/amoro/

RUN AMORO_VERSION=`cat /workspace/amoro/versions`  \
    && cd /workspace/amoro/lib \
    && cp /root/docker/dlc/lib/* ./  \
    && wget https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.30/mysql-connector-java-8.0.30.jar


FROM csighub.tencentyun.com/dlchub/dlc-base-maven:tlinux4.0-rc0
ENV TZ Asia/Shanghai

COPY --from=builder "/workspace/amoro" "/usr/local/services/amoro"

RUN pwd \
    && ls /usr/local/services/amoro

ENV AMORO_HOME /usr/local/services/amoro
ENV AMORO_CONF_DIR ${AMORO_HOME}/conf
ENV AMORO_LOG_DIR /data/logs
ENV LOG_LEVEL info
EXPOSE 1630 1260 1261

RUN echo ${AMORO_HOME}
RUN chmod +x ${AMORO_HOME}/entrypoint.sh

WORKDIR ${AMORO_HOME}
ENTRYPOINT ["./entrypoint.sh", "ams"]
CMD ["help"]