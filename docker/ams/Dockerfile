FROM openjdk:8u332-jdk

ARG ARCTIC_VERSION=0.4.0-SNAPSHOT
ARG RELEASE=v0.3.2-rc1

WORKDIR /usr/local/ams
RUN apt update \
    && apt-get install -y vim \
    && apt-get install -y net-tools \
    && apt-get install -y telnet \
    && apt-get clean

ARG AMS_FILE
WORKDIR /usr/local/ams

ADD arctic-${ARCTIC_VERSION}-bin.zip /usr/local/ams/arctic-${ARCTIC_VERSION}-bin.zip
RUN unzip arctic-${ARCTIC_VERSION}-bin.zip

WORKDIR /usr/local/ams/arctic-${ARCTIC_VERSION}

EXPOSE 1630/tcp
EXPOSE 1260/tcp

CMD ["bash","-c","./bin/ams.sh start && tail -f /dev/null"]


