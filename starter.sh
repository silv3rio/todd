#this file is used by the docker file for creating the base image that suports todd+tomcat
# Starting services
/etc/init.d/freeradius start >/dev/null 2>/dev/null
/etc/init.d/tacacs_plus start >/dev/null 2>/dev/null
service ssh start
#java -cp /root/cogsiP5-latest.jar net.jnjmx.todd.Server &
java -Dcom.sun.management.jmxremote.port=10500 -Dcom.sun.management.jmxremote.rmi.port=10500 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=192.168.20.20 -Dcom.sun.management.jmxremote.host=192.168.20.20 -cp /root/cogsiP5-latest.jar net.jnjmx.todd.Server & echo $! >/root/todd.pid
sleep 5
java -cp /root/cogsiP5-latest.jar net.jnjmx.todd.ClientApp4 & echo $! >/root/client.pid
clear

# Launching shell
cd
exec bash -i
