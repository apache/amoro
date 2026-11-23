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

This directory contains the documentation site infrastructure for Apache Amoro.
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

## Organization of Documentation Content

The documentation is organized into versioned and non-versioned content:

- **Versioned Content** (`/docs`): All markdown pages specific to an Amoro version are maintained in the main repository's `/docs` directory. These include user guides, admin guides, concepts, and other technical documentation.

- **Non-versioned Content** (`/site`): The website infrastructure and common pages shared across all versions are maintained in the `/site` directory, which includes:
  - `amoro-site`: Contains the landing page and common content
  - `amoro-docs`: Contains the documentation site that renders versioned content (via symlink to `/docs`)
  - `amoro-theme`: Contains the Hugo theme used by both sites

The [site deployment workflow](../.github/workflows/site-deploy.yml) builds the main site and
the [development documentation](https://amoro.apache.org/docs/latest/) from `master`.
Release branches and tags build to `/docs/<version>/`. A branch named `0.9.0` and a tag
named `v0.9.0-incubating` both map to `/docs/0.9.0/`.

# How to Contribute

## Submitting Pull Requests

- **Version-specific content**: Changes to the markdown contents for version-specific pages should be submitted to the `/docs` directory.

- **Non-versioned content**: Changes to common pages, website appearance (HTML, CSS), etc. should be submitted to the `/site` directory.

- **Old version documentation**: Changes to documentation of old Amoro versions should be submitted against the specific version branch.

## Reporting Issues

All issues related to the doc website should be submitted to the [Amoro repository](https://github.com/apache/amoro).

## Running Locally

With Hugo Extended installed, run the following commands from the repository root.
To preview the main site, run:
```shell
(cd site/amoro-site && hugo serve)
```

To start the documentation site locally, run:
```shell
(cd site/amoro-docs && hugo serve)
```

## Testing Both Sites Together

To verify links between the main site and the development documentation, build both
sites into one directory and serve it with Python 3. Run this block from the repository root:

```shell
(cd site/amoro-site && hugo -b http://127.0.0.1:5500/ -d ../../public)
(cd site/amoro-docs && hugo -b http://127.0.0.1:5500/docs/latest/ -d ../../public/docs/latest)
python3 -m http.server 5500 --bind 127.0.0.1 --directory public
```

Open the [main site](http://127.0.0.1:5500/), [Quickstart](http://127.0.0.1:5500/quick-start/),
or [development documentation](http://127.0.0.1:5500/docs/latest/). Press Ctrl+C to stop the server.

## Scanning For Broken Links

To scan for broken links, you can use the linkcheck tool available [here](https://github.com/filiph/linkcheck).
