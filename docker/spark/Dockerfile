
FROM openjdk:8u332-jdk

WORKDIR /usr/local/arctic_spark
RUN wget https://dlcdn.apache.org/spark/spark-3.1.3/spark-3.1.3-bin-hadoop2.7.tgz \
    && tar -zxvf spark-3.1.3-bin-hadoop2.7.tgz \
    && rm -rf spark-3.1.3-bin-hadoop2.7.tgz
ENV SPARK_HOME=/usr/local/arctic_spark/spark-3.1.3-bin-hadoop2.7 \
    PATH=${PATH}:${SPARK_HOME}/bin
RUN wget https://github.com/NetEase/arctic/releases/download/v0.3.0-rc1/arctic-spark_3.1-runtime-0.3.0.jar \
    && cp arctic-spark_3.1-runtime-0.3.0.jar ${SPARK_HOME}/jars

CMD ["/bin/bash"]


