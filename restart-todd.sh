#!/bin/bash
kill -9 $(cat /root/todd.pid)
kill -9 $(cat /root/client.pid)
java -Dcom.sun.management.jmxremote.port=10500 -Dcom.sun.management.jmxremote.rmi.port=10500 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=192.168.20.20 -Dcom.sun.management.jmxremote.host=192.168.20.20 -cp /root/cogsiP5-latest.jar net.jnjmx.todd.Server & echo $! >/root/todd.pid
sleep 5
java -cp /root/cogsiP5-latest.jar net.jnjmx.todd.ClientApp4 & echo $! >/root/client.pid
