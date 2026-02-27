<!--
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 -->

# HTTP Client Configuration for Local Testing  

This file is designed for local testing of HTTP interfaces using the **HTTP Client** plugin in IntelliJ IDEA. It enables quick testing of REST APIs directly within the IDE without requiring additional external tools or scripts.  

## Prerequisites  
1. Install IntelliJ IDEA (Community or Ultimate Edition).  
2. Install the **HTTP Client** plugin if it is not already installed.  

   For more information on the HTTP Client plugin, refer to the official documentation:  
   [HTTP Client in IntelliJ IDEA](https://www.jetbrains.com/help/idea/http-client-in-product-code-editor.html)  

## How to Use  
1. Open the `.http` or `.rest` file in IntelliJ IDEA.  
2. Use the provided HTTP requests to test your API endpoints.  
3. Select an HTTP request and click the **Run** button that appears next to it in the editor, or press `Ctrl+Enter` (Windows/Linux) or `Command+Enter` (Mac).  
4. View the response in the dedicated response panel.  

### Example  
```http
GET http://localhost:8080/api/example
Content-Type: application/json
Authorization: Bearer <your_token>
```

### Features

-   Supports HTTP methods like GET, POST, PUT, DELETE, etc.
-   Easily add headers, query parameters, and body content.
-   View responses interactively with support for formats like JSON, XML, etc.

## Scope of Use

This file is intended **only for local development and testing purposes**. It is not meant for production or CI/CD pipelines.

## Recommendations for Automated Testing

While the HTTP Client plugin is great for manual testing, we recommend integrating REST API tests into your automated test suite for better coverage and reliability.

### Other Suggested Tools

-   **JUnit** with [RestAssured](https://rest-assured.io/) for Java-based integration tests.
-   **Postman** with [Newman](https://www.npmjs.com/package/newman) for automated API testing.

