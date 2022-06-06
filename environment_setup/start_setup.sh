#!/bin/bash

echo Starting up everything. This will take quite some time on teh first start. Asterisk and Ari-proxy are beeing downloded and build..
echo ... please be patient!

which docker
if [ $? -gt 0 ]; then
    echo please install docker first
    echo more info here: https://github.com/docker/docker-ce
    exit 1
fi
which docker-compose
if [ $? -gt 0 ]; then
    echo please install docker-compose first
    echo more info here: https://docs.docker.com/compose/
    exit 1
fi

docker network rm aricontroller_default

docker-compose up -d
echo
echo
echo "YAY \o/ ...you made it"
echo
echo Started up successfully. Connect your ariController to Kafka now!
echo also you need a SIP Phone connecting to the Asterisk, more information see https://wiki.asterisk.org/wiki
echo Please be aware, docker-compose starts Asterisk in a Natted environment. This may cause problems with SIP and RTP. Check your pjsip.conf!
echo
echo "to stop, please enter -> docker-compose stop"
echo "for logs, please enter -> docker-compose logs -f"
