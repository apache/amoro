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

This directory contains the documentation site for [Apache Amoro](https://github.com/apache/amoro).
It's built with [Hugo](https://gohugo.io/) and hosted at https://amoro.apache.org.

# Structure

The Amoro documentation site consists of both versioned and non-versioned content, organized as follows:

```
.
├── docs (versioned)
│   ├── admin-guides
│   ├── concepts
│   ├── engines
│   ├── formats
│   ├── images
│   └── user-guides
└── site (non-versioned)
    ├── amoro-docs
    │   ├── archetypes
    │   ├── content (symlink to ../../docs)
    │   ├── hugo.toml
    │   └── themes
    ├── amoro-site
    │   ├── archetypes
    │   ├── content
    │   ├── hugo.toml
    │   └── themes
    ├── amoro-theme
    └── README.md
```

## Relationship to the Apache Amoro Repository

The documentation is organized into versioned and non-versioned content:

- **Versioned Content** (`/docs`): All markdown pages specific to an Amoro version are maintained in the main repository. These include user guides, admin guides, concepts, and other technical documentation.

- **Non-versioned Content** (`/site`): The website infrastructure and common pages shared across all versions are maintained in the site directory, which includes:
  - `amoro-site`: Contains the landing page and common content
  - `amoro-docs`: Contains the documentation site that renders versioned content
  - `amoro-theme`: Contains the Hugo theme used by both sites

During each new release, the release manager will:
1. Create a branch in this repo from master named for the release version
2. Update the latest branch HEAD to point to the release branch HEAD

# How to Contribute

## Submitting Pull Requests

- **Version-specific content**: Changes to the markdown contents for version-specific pages should be submitted to the `/docs` directory in the main repository.

- **Non-versioned content**: Changes to common pages, website appearance (HTML, CSS), etc. should be submitted to the `/site` directory.

- **Old version documentation**: Changes to documentation of old Amoro versions should be submitted against the specific version branch.

## Reporting Issues

All issues related to the doc website should be submitted to the [Amoro repository](https://github.com/apache/amoro).

## Running Locally

To run the website locally:

```shell
# Clone the repository if you haven't already
git clone git@github.com:apache/amoro.git
cd amoro
```

To start the site page locally, run:
```shell
(cd site/amoro-site && hugo serve)
```

To start the documentation site locally, run:
```shell
(cd site/amoro-docs && hugo serve)
```

## Testing Both Sites Together

In some cases, it's useful to test both the amoro site and the docs site together, especially for testing relative links between the two. This can be achieved by building both sites with custom `baseURL` and `publishDir` values:

First, build the main site:
```
cd site/amoro-site
hugo -b http://localhost:5500/ -d ../../public
```

Next, build the docs site:
```
cd ../amoro-docs
hugo -b http://localhost:5500/docs/latest/ -d ../../public/docs/latest
```

You can then serve the combined site from the `public` directory using any local server.

## Scanning For Broken Links

To scan for broken links, you can use the linkcheck tool available [here](https://github.com/filiph/linkcheck).