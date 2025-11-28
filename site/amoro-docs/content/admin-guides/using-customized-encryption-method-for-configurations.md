---
title: "Using Customized Encryption Method for Configurations"
url: using-customized-encryption-method
aliases:
    - "admin-guides/using-customized-encryption-method"
menu:
    main:
        parent: Admin Guides
        weight: 400
---
<!--
 - Licensed to the Apache Software Foundation (ASF) under one or more
 - contributor license agreements.  See the NOTICE file distributed with
 - this work for additional information regarding copyright ownership.
 - The ASF licenses this file to You under the Apache License, Version 2.0
 - (the "License"); you may not use this file except in compliance with
 - the License.  You may obtain a copy of the License at
 -
 -   http://www.apache.org/licenses/LICENSE-2.0
 -
 - Unless required by applicable law or agreed to in writing, software
 - distributed under the License is distributed on an "AS IS" BASIS,
 - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 - See the License for the specific language governing permissions and
 - limitations under the License.
 -->
# Using Customized Encryption Method for Configurations
To enhance security, AMS allows encrypted sensitive configuration items such as passwords. Currently, AMS only supports the built-in base64 encryption algorithm (see [Configure encrypted configuration items](../deployment/#configure-encrypted-sensitive-configuration-items) for details). If you require a stronger or customized encryption method, AMS also provides the flexibility to implement your own encryption algorithm.
## Develop the Custom Implementation
To integrate a custom encryption algorithm, you need to create a Java class that implements the `ConfigShade` interface and package it as a service.
### Add Maven Dependency
   If using a Maven project, add the following dependency to your `pom.xml`:
```xml
<dependency>
    <groupId>org.apache.amoro</groupId>
    <artifactId>amoro-common</artifactId>
    <version>${amoro.version}</version>
    <scope>provided</scope>
</dependency>
```

### Implement the `ConfigShade` Interface
Create a Java class that implements the `ConfigShade` interface. This class will handle decryption for sensitive configuration values.

```java
/**
 * The interface that provides the ability to decrypt {@link
 * org.apache.amoro.config.Configurations}.
 */
public interface ConfigShade {
  /**
   * Initializes the custom instance using the service configurations.
   *
   * This method can be useful when decryption requires an external file (e.g. a key file)
   * defined in the service configs.
   */
  default void initialize(Configurations serviceConfig) throws Exception {}

  /**
   * The unique identifier of the current interface, used it to select the correct {@link
   * ConfigShade}.
   */
  String getIdentifier();

  /**
   * Decrypt the content.
   *
   * @param content The content to decrypt
   */
  String decrypt(String content);
}
```
In this interface:

- `getIdentifier()`: Returns a **unique** identifier for your encryption algorithm, which is used to configure the `ams.shade.identifier`. Avoid using "default" (which disables encryption) or "base64" (which refers to AMS’s built-in Base64 support).
- `decrypt(String content)`: Implements the decryption logic for converting encrypted values back to plaintext.

Here is an example implementation:
```java
package com.example.shade;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.config.shade.ConfigShade;
import java.util.Base64;

/**
 * Custom Base64 decryption implementation for AMS.
 */
public class Base64CustomConfigShade implements ConfigShade {
    @Override
    public String getIdentifier() {
        return "base64-custom"; // Use this identifier in shade.identifier
    }

    @Override
    public String decrypt(String content) {
        return new String(Base64.getDecoder().decode(content));
    }
}
```

### Register the Custom Implementation
Create a file named `org.apache.amoro.config.shade.ConfigShade` under `resources/META-INF/services/` and add the fully qualified class name of your implementation:
```j
com.example.shade.Base64CustomConfigShade
```
### Build the JAR
Package your implementation into a JAR file using Maven:
```shell
mvn clean package
```

## Deploy the Custom Implementation
Once you've developed and packaged the custom encryption algorithm, you can deploy it to AMS by following these steps.

### Copy the JAR to AMS
Move the generated JAR file to the `${AMORO_HOME}/lib/` directory.

### Configure AMS to Use the Custom Encryption
Modify `${AMORO_HOME}/conf/config.yaml` to specify the custom encryption algorithm by setting the `ams.shade.identifier` to match the value returned by `getIdentifier()` in your Java class.
Then, replace sensitive configuration values with their encrypted versions.
```yaml
ams:
  shade:
    identifier: base64-custom  # Use the custom encryption algorithm
    sensitive-keywords: admin-password;database.password
```

### Restart AMS
Restart the AMS service to apply the new encryption settings.
```shell
bin/ams restart
```

By following these steps, you can successfully integrate and deploy a custom encryption algorithm in AMS, ensuring that sensitive information is securely stored in the configuration file.