#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
sudo docker run --rm --name build -v $DIR:/EGA_build -it alexandersenf/ega_build sh -c 'exec /EGA_build/build.sh'
sudo docker build -t ega_key -f Dockerfile_Deploy .
sudo rm KeyProviderService-0.0.1-SNAPSHOT.jar
sudo rm Dockerfile_Deploy
sudo rm keyd.sh
sudo docker run -d -p 9094:9094 -v $1:/config ega_key
