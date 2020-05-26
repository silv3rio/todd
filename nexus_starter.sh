#this file is used by the docker image file for the nexus machine
# Starting services
/etc/init.d/freeradius start >/dev/null 2>/dev/null
/etc/init.d/tacacs_plus start >/dev/null 2>/dev/null
service ssh start
/nexus-3.23.0-03/bin/nexus start

clear

# Launching shell
cd
exec bash -i
