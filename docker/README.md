# How to build docker images

We provide a bash script to help you build docker image easier.

You can build all image via script in current dir.

```shell
./build all 
```

or just build only one image.

```shell
./build ams
```

- NOTICE: The ams image and flink image required the project had been packaged. 
so run `mvn package -p '!trino'` before build ams or flink image.

You can speed up image building via 

```shell
./build.sh \
  --apache-archive https://mirrors.tuna.tsinghua.edu.cn/apache \
  --debian-mirror https://mirrors.tuna.tsinghua.edu.cn/  \
  flink
```

more options see `./build.sh --help`