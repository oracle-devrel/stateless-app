# sample-stateless-app

[![License: UPL](https://img.shields.io/badge/license-UPL-green)](https://img.shields.io/badge/license-UPL-green) [![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=oracle-devrel_stateless-app)](https://sonarcloud.io/dashboard?id=oracle-devrel_stateless-app)

## Introduction
This is the eclipse project for some simple stateless apps

## Getting Started
There are three Eclipse projects in this repo

StatelessYaml - contains the YAML files and also example configs to use for deployments. Please look at the README.md file in that project for instructions on how to install and run these stateless applications in Kubrnetes

StatelessFront - a **very** simple application based on the front ent part of the Helidon example "Greeting" application (`/greet`). This only does the front end processing, it communiucates to the StatelessBack application to get the prefix used byt the greeting. 

StatelessBack - a **very** simple application based on the back end (Greeting provider code) of the Helidon example "Greeting" application (`/prefix`). This only supports the retrieval and setting of the prefix. 

Using two separate microservices is of course a completely artificial split but is intended to enable the exploration of in Kubernetes communications.

Both of the "Front" and "Back" applications also provide `/openapi`, `/metrics`, and `/health` endpoints. They also provide mechanisms to simulate fault conditions.

### Prerequisites
You will need Maven (3.8.5 works, earlier may not) and Java 17

## Notes/Issues
None known

## URLs
* Nothing at this time

## Contributing
This project is open source.  Please submit your contributions by forking this repository and submitting a pull request!  Oracle appreciates any contributions that are made by the open source community.

## License
Copyright (c) 2022 Oracle and/or its affiliates.

Licensed under the Universal Permissive License (UPL), Version 1.0.

See [LICENSE](LICENSE) for more details.
