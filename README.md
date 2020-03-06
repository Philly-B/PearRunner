# PearRunner

Small project, which allows to run one [UIMA](https://github.com/apache/uima-uimaj) pear inside a docker container. This docker container provides a webservice endpoint. Documents, which are sent to this endpoint are processed by the pear and the result is provided as JSON. The container is stateless and can therefore be scaled as needed.

# Getting started

## Building this project
The maven project compiles the code and builds a docker image based on the contained docker file. It requires a pear in the root directory of this project. The Container can then be started by using the docker-compose file, which is provided on root level of this project as well.

## Building a pear
An example pear is provided in the folder **examplePear** on root level of this project. To build a pear yourself I recommend to read the [UIMA documentation](https://uima.apache.org/doc-uima-pears.html). Afterwards good example and archetype projects can be found in the git repository of [Averbis](https://github.com/averbis). The provided example pear is a copy of the [Hello-World-Pear](https://github.com/averbis/hello-world-pear).
