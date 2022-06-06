
# ari controller

the ari controller is intended to represent a showcase how to use [ARI Proxy](https://github.com/retel-io/ari-proxy)
This is not a production ready controller. 

Currently, the controller will start a playback but nothing else.

## Setup
in order to bring up the showcase there is a docker-compose file starting the entire needed environment.  
The setup is quite complex. There is a Kafka setup as well as a persistent backend in pace to transport messages from ARI Proxy to ARI-Controller.
How this works is presented in a [Asterisk Astricon Talk](https://www.youtube.com/watch?v=vMCyuItMYxE).

### Startup
to startup testsetup change to folder `environment_setup` and exec the script `start_setup.sh`

If you start the environment the first time, it will take quite some time. A Asterisk will be pulled and compiled on startup.

after that you need to run the `Main.java` Class located in `src/main/java/com/aricontroller/Main`.
Please respect, I will not help with the Java project itself. 

### Testing
You can register a SIP Soft-phone to asterisk running in the docker Setup. The Asterisk config including the asterisk pjsip config is located in the project under `environment_setip/asterisk/asterisk-config`. Please configure your client accordingly.
After register the client you can call `1234` Which playback the `tt-monkeys` right from dialplan. if you call the `4321` it will trigger the Stasis application.

### Shutdown
to shut down the Environment just change to folder `environment-seup` and type `docker-compose down`.

## Call logic
so far the ARI-controller is only able to answer a channel and playback a soundfile. 
all the relevant logic is implemented at package com.aricontroller.control.controller
This is plain java without any Akka specifics.
 
The ARI-controller is an [akka based actor system](https://doc.akka.io/docs/akka/current/typed/actors.html). There is an Actor for each call. There is no possibility to leak information between those calls. The Calls are represented by the "Routing Key". The routing key is dictated by the ARI-proxy and is used as the routing Key for kafka as well. If there are more than one ARI-Controller instances running, all messaged for a routing key is routed to the same controller instance.

In case the ARI-controller created new resources within the asterisk (e.g. a Playback) the ARI-Proxy will store the Playback ID - Routing key pair and all future ARI Events are routed accordingly.

If the ARI-controller should be High Available, you need to replace the persistent backend with something else than "in-Mem". For production cassandra could be an Option. Please refer to the Akka Documentation for it.

### Useful commands

Entering Asterisk console
```shell
docker-compose exec asterisk asterisk -vvvr
```

Logs for ARI-proxy
```shell
docker-compose logs -f ari-proxy
```

Stopping Environment
```shell
docker-compose stop
```

# Contributing
I will not accept PR for this project. If you find an issue, please tell me, so I can fix the issue. Since this is a showcase only, no production enhancing features will be implemented here.
You can use this code as a basis, but I highly recommend not to do so, without mayor changes for. 

# Thanks
Special thanks are going to [sipgate](https://www.sipgate.de). sipgate is using a similar project in production and a lot of code is used in here.
As well as the entire Setup to connect Asterisk with Kafka is done by sipgate.
