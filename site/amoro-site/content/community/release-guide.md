---
title: "Release Guide"
url: release-guide
disableSidebar: true
---
# How to release a new version

## Preparation

### Apache release documentation
Please refer to the following link to understand the ASF release process:

- [Apache Release Guide](http://www.apache.org/dev/release-publishing)
- [Apache Release Policy](http://www.apache.org/dev/release.html)
- [Publishing Maven Artifacts](http://www.apache.org/dev/publishing-maven-artifacts.html)

### Environmental requirements

- JDK 11
- Apache Maven 3.8+
- GnuPG 2.1+
- Git
- SVN

### GPG signature

Follow the Apache release guidelines, you need the GPG signature to sign the release version, users can also use this to determine if the downloaded version has been tampered with.

Create a pgp key for version signing, use `<your Apache ID>@apache.org` as the USER-ID for the key.

For more details, refer to [Apache Releases Signing documentation](https://infra.apache.org/release-signing)，[Cryptography with OpenPGP](http://www.apache.org/dev/openpgp.html).

Brief process for generating a key：

* Generate a new GPG key using `gpg --gen-key`, set the key length to 4096 and set it to never expire
* Upload the key to the public key server using `gpg --keyserver keys.openpgp.org --send-key <your key id>`
* Export the public key to a text file using `gpg --armor --export <your key id> >> gpgapachekey.txt`
* Obtain the keys of other committers for signing (optional)
* Add the generated key to the KEYS file (uploaded to the svn repository by the release manager)

You can follow the steps below to create the GPG key:

{{< hint info >}}
You should replace `amoro` with your `Apache ID` in following guides.
{{< /hint >}}

```shell
$ gpg --full-gen-key
gpg (GnuPG) 2.2.27; Copyright (C) 2021 Free Software Foundation, Inc.
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.

Please select what kind of key you want:
(1) RSA and RSA (default)
(2) DSA and Elgamal
(3) DSA (sign only)
(4) RSA (sign only)
(14) Existing key from card
Your selection? 1 # Please enter 1
RSA keys may be between 1024 and 4096 bits long.
What keysize do you want? (3072) 4096 # Please enter 4096 here
Requested keysize is 4096 bits
Please specify how long the key should be valid.
0 = key does not expire
<n> = key expires in n days
<n>w = key expires in n weeks
<n>m = key expires in n months
<n>y = key expires in n years
Key is valid for? (0) 0 # Please enter 0
Key does not expire at all
Is this correct? (y/N) y # Please enter y here

GnuPG needs to construct a user ID to identify your key.

Real name: amoro # Please enter 'gpg real name'
Email address: amoro@apache.org # Please enter your apache email address here
Comment: amoro # Please enter some comments here
You selected this USER-ID:
    "amoro (amoro) <amoro@apache.org>"

Change (N)ame, (C)omment, (E)mail or (O)kay/(Q)uit? O # Please enter O here
We need to generate a lot of random bytes. It is a good idea to perform
some other action (type on the keyboard, move the mouse, utilize the
disks) during the prime generation; this gives the random number
generator a better chance to gain enough entropy.

# At this time, a dialog box will pop up, asking you to enter the key for this gpg. 
# you need to remember that it will be used in subsequent steps.
┌─────────────────────────────────────────────────────┐
│ Please enter this passphrase to                     │
│ protect your new key                                │
│                                                     │
│ Passphrase: _______________________________________ │
│                                                     │
│     <OK>                    <Cancel>                │
└─────────────────────────────────────────────────────┘

# Here you need to re-enter the password in the previous step.
┌─────────────────────────────────────────────────────┐
│ Please re-enter this passphrase                     │
│                                                     │
│ Passphrase: _______________________________________ │
│                                                     │
│     <OK>                    <Cancel>                │
└─────────────────────────────────────────────────────┘
gpg: key ACFB69E705016886 marked as ultimately trusted
gpg: revocation certificate stored as '/root/.gnupg/openpgp-revocs.d/DC12398CCC33A5349EB9663DF9D970AB18C9EDF6.rev'
public and secret key created and signed.

pub   rsa4096 2023-05-01 [SC]
      85778A4CE4DD04B7E07813ABACFB69E705016886
uid                      amoro (amoro) <amoro@apache.org>
sub   rsa4096 2023-05-01 [E]

```

Then you can follow the steps below to upload the GPG key to the public server:

```shell
$ gpg --keyid-format SHORT --list-keys
/root/.gnupg/pubring.kbx
------------------------
pub   rsa4096/05016886 2023-05-01 [SC]
      85778A4CE4DD04B7E07813ABACFB69E705016886
uid         [ultimate] amoro (amoro) <amoro@apache.org>
sub   rsa4096/0C5A4E1C 2023-05-01 [E]

# Send public key to keyserver via key id
$ gpg --keyserver keyserver.ubuntu.com --send-key 05016886 # send key should be found in the --list-keys result
# Among them, keyserver.ubuntu.com is the selected keyserver, it is recommended to use this, because the Apache Nexus verification uses this keyserver
```

Check if the key is uploaded successfully:

```shell
$ gpg --keyserver keyserver.ubuntu.com --recv-keys 05016886   # If the following content appears, it means success
gpg: key ACFB69E705016886: "amoro (amoro) <amoro@apache.org>" not changed
gpg: Total number processed: 1
gpg:              unchanged: 1

```

Add the GPG public key to the KEYS file of the Apache SVN project warehouse:

```shell
# Add public key to KEYS in dev branch
$ mkdir -p ~/amoro_svn/dev
$ cd ~/amoro_svn/dev
$ svn co https://dist.apache.org/repos/dist/dev/incubator/amoro
$ cd ~/amoro_svn/dev/amoro
# Append the KEY you generated to the file KEYS, and check if it is added correctly
$ (gpg --list-sigs amoro@apache.org && gpg --export --armor amoro@apache.org) >> KEYS 

$ svn ci -m "add gpg key for amoro"

# Add public key to KEYS in release branch
$ mkdir -p ~/amoro_svn/release
$ cd ~/amoro_svn/release

$ svn co https://dist.apache.org/repos/dist/release/incubator/amoro/
$ cd ~/amoro_svn/release/amoro

# Append the KEY you generated to the file KEYS, and check if it is added correctly
$ (gpg --list-sigs amoro@apache.org && gpg --export --armor amoro@apache.org) >> KEYS 

$ svn ci -m "add gpg key for amoro"
```

### Maven settings

During the release process, frequent access to your Apache password is required. To prevent exposure in plaintext storage, we need to encrypt it.

```shell
# Generate master password
$ mvn --encrypt-master-password <apache password>
{EM+4/TYVDXYHRbkwjjAS3mE1RhRJXJUSG8aIO5RSxuHU26rKCjuS2vG+/wMjz9te}
```

Create the file `${user.home}/.m2/settings-security.xml` and configure the password created in the previous step:

```xml
<settingsSecurity>
 <master>{EM+4/TYVDXYHRbkwjjAS3mE1RhRJXJUSG8aIO5RSxuHU26rKCjuS2vG+/wMjz9te}</master>
</settingsSecurity>
```

In the maven configuration file `~/.m2/settings.xml`, add the following item:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  
  <servers>
    <server>
        <id>apache.snapshots.https</id>
        <!-- APACHE LDAP UserName --> 
        <username>amoro</username>
        <!-- APACHE LDAP password (Fill in the password you just created with the command `mvn --encrypt-password <apache passphrase>`) -->
        <password>{/ZLaH78TWboH5IRqNv9pgU4uamuqm9fCIbw0gRWT01c=}</password>
    </server>
    <server>
        <id>apache.releases.https</id>
        <!-- APACHE LDAP UserName --> 
        <username>amoro</username>
        <!-- APACHE LDAP password (Fill in the password you just created with the command `mvn --encrypt-password <apache passphrase>`) -->
        <password>{/ZLaH78TWboH5IRqNv9pgU4uamuqm9fCIbw0gRWT01c=}</password>
    </server>
  </servers>

  <profiles>
        <profile>
          <id>apache-release</id>
          <properties>
            <gpg.keyname>05016886</gpg.keyname>
            <!-- Use an agent: Prevents being asked for the password during the build -->
            <gpg.useagent>true</gpg.useagent>
            <gpg.passphrase>passphrase for your gpg key</gpg.passphrase>
          </properties>
        </profile>
  </profiles>
</settings>
```

Generate the final encrypted password and add it to the `~/.m2/settings.xml` file:

```shell
$ mvn --encrypt-password <apache password>
{/ZLaH78TWboH5IRqNv9pgU4uamuqm9fCIbw0gRWT01c=}
```

## Build release

### Cut the release branch
> The version of Apache Amoro follows the [Semantic Versioning](https://semver.org/), which looks like `{major}.{mior}.{patch}`

Cut a branch for the `minor` version if it is not created in the Apache remote repository, if it already exists, check it out and pull the latest changes.

```shell
$ cd ${AMORO_SOURCE_HOME}

# Cut the release branch if it is not created, then push to the remote 
$ git checkout -b 0.8.x
$ git push apache 0.8.x

# Or check it out and pull the latest changes if it is created
$ git checkout 0.8.x
$ git pull apache 0.8.x
```

Checkout a `patch` branch using the following command
```
$ git checkout -b 0.8.0-branch
```

Update the version in the new branch using the tool `tools/change-version.sh`:
```bash
OLD="0.8-SNAPSHOT"
NEW="0.8.0-incubating"

HERE=` basename "$PWD"`
if [[ "$HERE" != "tools" ]]; then
    echo "Please only execute in the tools/ directory";
    exit 1;
fi

# change version in all pom files
find .. -name 'pom.xml' -type f -exec perl -pi -e 's#<version>'"$OLD"'</version>#<version>'"$NEW"'</version>#' {} \;
```

Then run the scripts and commit the change:

```shell
$ cd tools
$ bash change-version.sh
$ cd ..
$ git add *
$ git commit -m "Change project version to 0.8.0-incubating"
$ git push apache 0.8.0-branch
```

### Create the release tag

Create the release tag and push it to the Apache repo:

```shell
$ git tag -a v0.8.0-rc1 -m "Release Apache Amoro 0.8.0 rc1"
$ git push apache v0.8.0-rc1
```

### Build binary and source release

Build Amoro binary release with scripts:

```shell
$ cd ${AMORO_SOURCE_HOME}/tools
$ RELEASE_VERSION=0.8.0-incubating bash ./releasing/create_binary_release.sh
```

Then build source release with scripts:

```shell
$ cd ${AMORO_SOURCE_HOME}/tools
$ RELEASE_VERSION=0.8.0-incubating bash ./releasing/create_source_release.sh
```

Validate the source and binary packages according to the [How to validate a new release](../validate-release/) guides.
After that, publish the dev directory of the Apache SVN warehouse of the material package:

```shell
$ cd ~/amoro_svn/dev/amoro
$ mkdir 0.8.0-incubating-RC1
$ cp ${AMORO_SOURCE_HOME}/tools/releasing/release/* 0.8.0-incubating-RC1
$ svn add 0.8.0-incubating-RC1
$ svn commit -m "Release Apache Amoro 0.8.0 rc1"

```

### Release Apache Nexus

Next, we will publish the required JAR files to the ​Apache Nexus​ repository to achieve the final goal of releasing them to the ​Maven Central Repository.

```shell
$ cd ${$AMORO_SOURCE_HOME}/tools
$ RELEASE_VERSION=0.8.0-incubating bash ./releasing/deploy_staging_jars.sh
```

You can visit https://repository.apache.org/ and log in to check the publishment status. You can find the publishment process in the `Staging Repositories` section. You need to close the process when all jars are publised.

## Vote for the new release

Next, vote for the new release via email. First complete the vote within the Amoro community, and upon approval, complete the vote within the Amoro community in the Incubator community. For detailed voting guidelines, please refer to [voting process](https://www.apache.org/foundation/voting.html).

### Vote in the Amoro community

Send a vote email to `dev@amoro.apache.org` to start the vote process in Apache Amoro community, you can take [[VOTE] Release Apache Amoro(incubating) 0.8.0-incubating rc3](https://lists.apache.org/thread/22rrpzwtzkby8vnhfvcwzmpfxxz8qhns) as an example.

You can validate the source and binary packages according to the [How to validate a new release](../validate-release/) guides and give your vote resulut. If other developers identify critical issues, you need to cancel the current vote, wait for the fixes to be implemented, and then restart the release process with a new release candidate.

After 72 hours, if there are at least 3 binding votes from Amoro PPMC members and no votes against, send the result email to celebrate the release of the version like [[RESULT][VOTE] Release Apache Amoro(incubating) 0.8.0-incubating rc3](https://lists.apache.org/thread/gokj30ldgh3p5866tw40h41mhdw90whs).

### Vote in the Incubator community

> We recommend to use gmail when sending the email.

Send a vote email to `general@incubator.apache.org` to start the vote process in Apache Incubator community, you can take [[VOTE] Release Apache Amoro(incubating) 0.8.0-incubating rc3](https://lists.apache.org/thread/bqj7gohrjwxp5gwycdgh78xmpymfm6jr) as an example.

After 72 hours, if there are at least 3 binding votes from IPMC members and no votes against, send the result email to celebrate the release of the version like [[RESULT][VOTE] Release Apache Amoro(incubating) 0.8.0-incubating rc3](https://lists.apache.org/thread/qmvg3tcds0p0pbn05w0mzchm85o581rv).

## Complete the final publishing steps

### Migrate source and binary packages

Migrate the source and binary packages to the release directory of the Apache SVN warehouse:

```shell
$ svn mv https://dist.apache.org/repos/dist/dev/incubator/amoro/0.8.0-incubating-RC1 https://dist.apache.org/repos/dist/release/incubator/amoro/0.8.0-incubating  -m "Release Apache Amoro 0.8.0-incubating"
```

### Publish releases in the Apache Staging repository

- Log in to http://repository.apache.org , log in with your Apache account
- Click Staging repositories on the left
- Select your most recently uploaded warehouse, the warehouse specified in the voting email
- Click the Release button above, this process will perform a series of checks

> It usually takes 24 hours for the warehouse to synchronize to other data sources

### Add release note and update the main web

- Creating an [new release note](https://github.com/apache/amoro/releases) in github
- Update the content for the new version for the web, the source locates in [amoro-site](https://github.com/apache/amoro-site) repo
  - First, submit a pull request containing the new version. You can refer to [this commit](https://github.com/apache/amoro-site/commit/598787c97898bb6431686e9cf6dd37f89ce2a339). Note that the `$version` in the "url = "/docs/$version/" URL in hugo.toml corresponds to the branch name will be created in the following step in amoro-site
  - After merging the pull request from step 1, pull a local branch named `$version` and push it remotely
  - Check that the [GithubAction](https://github.com/apache/amoro-site/actions/workflows/deploy.yml) for branch `$version` is executed successfully

### Send announcement email

Finally, we need to send the announcement email to these mailing lists: `dev@amoro.apache.org`, `general@incubator.apache.org`. Here is an example of an announcement email:  [[ANNOUNCE] Apache Amoro (Incubating) 0.8.0-incubating available](https://lists.apache.org/thread/h3cy8f2mfmp4zms4cs3tq4hdlq64qyw0).

Congratulations! You have successfully completed all steps of the Apache Amoro release process. Thank you for your contributions!
