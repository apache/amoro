FROM openjdk:8u332-jdk

ARG ARCTIC_VERSION=0.3.2
ARG RELEASE=v0.3.2-rc1

WORKDIR /usr/local/ams
RUN apt update \
    && apt-get install -y vim \
    && apt-get install -y net-tools \
    && apt-get install -y telnet

ARG AMS_FILE
WORKDIR ${AMS_FILE}
ADD arctic-0.4.0-SNAPSHOT-bin.zip /usr/local/ams
RUN unzip arctic-${ARCTIC_VERSION}-bin.zip

WORKDIR /usr/local/ams/arctic-${ARCTIC_VERSION}

CMD ["bash","-c","./bin/ams.sh start && tail -f /dev/null"]


