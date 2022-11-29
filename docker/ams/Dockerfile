FROM openjdk:8u332-jdk

ARG ARCTIC_VERSION=0.4.0-SNAPSHOT
ARG RELEASE=v0.3.2-rc1

WORKDIR /usr/local/ams
RUN apt update \
    && apt-get install -y vim \
    && apt-get install -y net-tools \
    && apt-get install -y telnet

ARG AMS_FILE
WORKDIR /usr/local/ams

ADD run_dn.sh /run_dn.sh
RUN chmod a+x /run_dn.sh
CMD ["/run_dn.sh"]

RUN unzip arctic-${ARCTIC_VERSION}-bin.zip

WORKDIR /usr/local/ams/arctic-${ARCTIC_VERSION}

CMD ["bash","-c","./bin/ams.sh start && tail -f /dev/null"]


