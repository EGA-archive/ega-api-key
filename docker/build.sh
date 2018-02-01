#!/bin/bash
git clone https://github.com/elixir-europe/ega-data-api-v3-key.git
mvn -f /ega-data-api-v3-key/pom.xml install
mv /ega-data-api-v3-key/target/KeyProviderService-0.0.1-SNAPSHOT.jar /EGA_build
mv /ega-data-api-v3-key/docker/keyd.sh /EGA_build
mv /ega-data-api-v3-key/docker/Dockerfile_Deploy /EGA_build
