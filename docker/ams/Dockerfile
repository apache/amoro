FROM openjdk:8u332-jdk
WORKDIR /usr/local/ams
RUN apt update \
    && apt-get install -y vim \
    && apt-get install -y net-tools \
    && apt-get install -y telnet

RUN wget https://github.com/NetEase/arctic/releases/download/v0.3.2-rc1/arctic-0.3.2-bin.zip \
    && unzip arctic-0.3.2-bin.zip

WORKDIR /usr/local/ams/arctic-0.3.2

CMD ["bash","-c","./bin/ams.sh start && tail -f /dev/null"]


