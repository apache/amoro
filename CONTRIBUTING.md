<!--
 - Licensed to the Apache Software Foundation (ASF) under one
 - or more contributor license agreements.  See the NOTICE file
 - distributed with this work for additional information
 - regarding copyright ownership.  The ASF licenses this file
 - to you under the Apache License, Version 2.0 (the
 - "License"); you may not use this file except in compliance
 - with the License.  You may obtain a copy of the License at
 - 
 -     http://www.apache.org/licenses/LICENSE-2.0
 - 
 - Unless required by applicable law or agreed to in writing, software
 - distributed under the License is distributed on an "AS IS" BASIS,
 - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 - See the License for the specific language governing permissions and 
 - limitations under the License.
-->

# Contributing

This document provides guidelines for contributing to Amoro.
While these suggestions are not strict rules, they aim to facilitate a smooth contribution experience.

## Issues

Regardless of the type of contribution you plan to make, it is recommended that you create an issue to track it.
* Before creating an issue, please search within the issues to see if a similar one has already been reported.
* Choose the appropriate type:
   * Feature: A new feature to be added.
   * Improvement: Enhancement of an existing feature, including code quality, performance, user experience, etc.
   * Bug: A problem that prevents the project from functioning as intended.
   * Subtask: A subtask of a Feature/Improvement that can be broken down into smaller steps.
   
## Pull requests

Pull requests are the preferred mechanism for contributing to Amoro
* Generally, create a PR only to the master branch.
* PR should be linked to the corresponding issue.
   * The PR title format should be: \[ARCTIC-{issue_number}\]\[{module}\]{pr_description}.
   * Add fix/resolve #{issue_number} in the description to link the PR to the issue.
   * The linked issue should clearly explain the background, objectives, and implementation methods of the PR.
* The change log in the PR should clearly describe the changes made in modules, classes, methods, etc.
* The PR should include corresponding testing methods, and the test results should be visible.
* If the PR involves new features, the user document should include instructions for its usage.

## Code review

Code review is a crucial aspect of contributing to a project, and all contributors are encouraged to actively review and provide feedback on each other's PRs.
* Check whether the PR meet the requirements specified in the previous section on Pull Requests.
* Review each file changed by the PR, and consider the following aspects:
    * Is the java doc complete?
    * Is there new unit or integration test coverage for the code changes?
    * Does the user document explain how to use new features?
    * Are there comments to aid in understanding complex logic?
    * Have any duplicate classes or methods been introduced?
* Track feedback on suggestions and their resolution.
* If a suggestion is resolved, please close it.
* If all suggestions are resolved or there are no suggestions, approve it.

## Design document

Write down your implementation plan and discuss it with other developers in the community before you start coding officially.
If it is just a small changes, describe the implementation steps clearly in the Issue.
If it is a relatively large work, it is recommended to write a design document for this feature.
Here is a [design document template](https://docs.google.com/document/d/1LeTyrlzQJfSs2DkRBsucK_vV5gtHRYLb1KSrpu0hp3g/edit?usp=sharing) for reference.

## Code suggestions

### Code style
If you are using IntelliJ IDEA, you can import code style configurations by following these steps:
1. Open Settings → Editor → Code Style → Show Schema Actions → Import Schema → IntelliJ IDEA Code Style XML
2. Select the `{ARCTIC_PROJECT_HOME}/style/idea/intellij-java-palantir-style.xml` file
3. Click OK to confirm.

### Copyright
All files (including source code, configuration files) in the project are required to declare CopyRight information at the top, and the project uses Apache License 2.
You can cnfigure the copyright information in IntelliJ IDEA with follow steps:
1. Open Settings → Editor → Copyright → Copyright Profiles.
2. Add a new copyright file named Apache.
3. Add the following text as the license text:
```
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and 
limitations under the License.
```
4. Go to Editor → Copyright and select the Apache copyright file as the default copyright file for the project. 
5. Click Apply to save the configuration changes.
6. Right-click on the existing File/Package/Module and select `Update Copyrights…` to update the Copyright of the file.