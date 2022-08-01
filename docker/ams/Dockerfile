
FROM openjdk:8u332-jdk
WORKDIR /usr/local/ams
RUN wget https://github.com/NetEase/arctic/releases/download/v0.3.0-rc1/arctic-0.3.0-bin.zip \
    && unzip arctic-0.3.0-bin.zip

WORKDIR /usr/local/ams/arctic-0.3.0

CMD ["bash","-c","./bin/ams.sh start && tail -f /dev/null"]


