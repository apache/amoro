<!--
  - Licensed to the Apache Software Foundation (ASF) under one
  - or more contributor license agreements.  See the NOTICE file
  - distributed with this work for additional information
  - regarding copyright ownership.  The ASF licenses this file
  - to you under the Apache License, Version 2.0 (the
  - "License"); you may not use this file except in compliance
  - with the License.  You may obtain a copy of the License at
  -
  -   http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing,
  - software distributed under the License is distributed on an
  - "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  - KIND, either express or implied.  See the License for the
  - specific language governing permissions and limitations
  - under the License.
  -->

# Apache Amoro Documentation Site

This repository contains the documentation for [Apache Amoro](https://github.com/apache/amoro).
It's built with [Hugo](https://gohugo.io/) and hosted at https://amoro.apache.org.

# Structure

The Amoro documentation site is actually constructed from two hugo sites. The first, is the site page. The second site, 
is the documentation site which contains the full Amoro documentation. The site page and
documentation sites are completely self-contained in the `./amoro-site` and `./amoro-docs` directories, respectively.

## Relationship to the Apache Amoro Repository

All markdown pages that are specific to an Amoro version are maintained in the apache/amoro repository. All pages common across all version
releases are kept here in the apache/amoro-site repo.

`apache/amoro`
- The `docs` folder in the [Amoro repository](https://github.com/apache/amoro) contains all the markdown docs used by the **versioned** docs site.

`apache/amoro-site`
- The `amoro-docs/content` folder is the target folder when copying the docs over during a version release
- The `amoro-site/content` folder is where you can find the common markdown files shared across all versions

During each new release, the release manager will:
1. Create a branch in this repo from master named for the release version
2. Copy the contents under `docs` in the amoro repo to `./amoro-docs/content` in the **release** branch
3. Update the latest branch HEAD to point to the release branch HEAD

# How to Contribute

## Submitting Pull Requests

Changes to the markdown contents for **version** specific pages should be submitted directly in the Amoro repository.

Changes to the markdown contents for common pages should be submitted to this repository against the `master` branch.

Changes to the website appearance (e.g. HTML, CSS changes) should be submitted to this repository against the `master` branch.

Changes to the documentation of old Amoro versions should be submitted to this repository against the specific version branch.


## Reporting Issues

All issues related to the doc website should still be submitted to the [Amoro repository](https://github.com/apache/amoro).
The GitHub Issues feature of this repository is disabled.

## Running Locally

Clone this repository to run the website locally:
```shell
git clone git@github.com:apache/amoro-site.git
cd amoro-site
```

To start the site page site locally, run:
```shell
(cd amoro-site && hugo serve)
```

To start the documentation site locally, run:
```shell
(cd amoro-docs && hugo serve)
```

If you would like to see how the latest website looks based on the documentation in the Amoro repository, you can copy docs to this repository by:
```shell
rm -rf amoro-docs/content
cp -r <path to amoro repo>/docs docs/content
```

## Scanning For Broken Links

If you'd like to scan for broken links, one available tool is linkcheck that can be found [here](https://github.com/filiph/linkcheck).

# How the Website is Deployed

## Testing Both Sites Locally

In some cases, it's useful to test both the amoro site and the docs site locally. Especially in situations
where you need to test relative links between the two sites. This can be achieved by building both sites with custom
`baseURL` and `publishDir` values passed to the CLI. You can then run the site with any local live server, such as the
[Live Server](https://marketplace.visualstudio.com/items?itemName=ritwickdey.LiveServer) extension for VSCode.

First, change into the `amoro-site` directory and build the site. Use `-b` and `-d` to set `baseURL` and `publishDir`, respectively.
```
cd amoro-site
hugo -b http://localhost:5500/ -d ../public
```

Next, change into the `amoro-docs` directory and do the same thing. Remember that the docs-site is deployed to a `docs/<VERSION>` url, relative to the landing-page site. Since the landing-page was deployed to `../publish` in the example
above, the example below usees `../public/docs/latest` to deploy a `latest` version docs-site.
```
cd ../amoro-docs
hugo -b http://localhost:5500/docs/latest/ -d ../public/docs/latest
```

You should then have both sites deployed to the `public` directory which you can launch using your live server.

**Note:** The examples above use port `5500`. Be sure to change the port number if your local live server uses a different port.
