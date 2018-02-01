#!/bin/bash
sudo docker run -d -p 9094:9094 -v $1:/config alexandersenf/ega_key
