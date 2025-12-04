---
title: "Release Guide"
url: validate-release
disableSidebar: true
---
# How to validate a new release

## Download candidate

```shell
# If there is svn locally, you can clone to the local
$ svn co https://dist.apache.org/repos/dist/dev/incubator/amoro/${release_version}-${rc_version}/
# or download the material file directly
$ wget https://dist.apache.org/repos/dist/dev/incubator/amoro/${release_version}-${rc_version}/
```

## validate candidate

### Check GPG signature

Download the KEYS and import it:

```shell
$ curl  https://downloads.apache.org/incubator/amoro/KEYS > KEYS # Download KEYS
$ gpg --import KEYS # Import KEYS to local
```

Trust the KEY used in this version:

```shell
$ gpg --edit-key xxxxxxxxxx #KEY user used in this version
gpg (GnuPG) 2.2.21; Copyright (C) 2020 Free Software Foundation, Inc.
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.

Secret key is available.
gpg> trust #trust
Please decide how far you trust this user to correctly verify other users' keys
(by looking at passports, checking fingerprints from different sources, etc.)

  1 = I don't know or won't say
  2 = I do NOT trust
  3 = I trust marginally
  4 = I trust fully
  5 = I trust ultimately
  m = back to the main menu

Your decision? 5 #choose 5
Do you really want to set this key to ultimate trust? (y/N) y  #choose y
```

Check the gpg signature:

```shell
$ for i in *.tar.gz; do echo $i; gpg --verify $i.asc $i; done
```

### Check sha512 hash

```shell
# Command for Linux
$ for i in *.tar.gz; do echo $i; sha512sum --check  $i.sha512; done
# Command for MacOS
$ for i in *.tar.gz; do echo $i; shasum -a 512 -c $i.sha512; done
```

### Check the binary package

Unzip the binary pakcages: `apache-amoro-${AMORO_VERSION}-bin-${HADOOP_VERSION}.tar.gz`:

```shell
# Hadoop2
$ tar -xzvf apache-amoro-0.8.0-incubating-bin-hadoop2.tar.gz

# Hadoop3 
$ tar -xzvf apache-amoro-0.8.0-incubating-bin-hadoop3.tar.gz
```

check as follows:
- Check whether the package contains unnecessary files, which makes the tar package too large
- Folder contains the word incubating
- There are LICENSE and NOTICE files
- There is a DISCLAIMER file
- Check for extra files or folders, such as empty folders, etc.

### Check the source package

Unzip the binary pakcages: `apache-amoro-${AMORO_VERSION}-src.tar.gz`:

```shell
$ tar -xzvf apache-amoro-0.8.0-incubating-src.tar.gz
```

Check as follows:
- There are LICENSE and NOTICE files
- There is a DISCLAIMER file
- All source files have ASF license at the beginning
- Only source files exist, not binary files

Compile from source:

```shell
# Compile from source
$ mvn clean package

# Or skip the unit test
$ mvn clean package -DskipTests
```

## vote for the release

If all verifications pass, please vote +1 for the new release! If you find any critical issues, pleaste vote -1 for it.
Thanks a lot for your work!