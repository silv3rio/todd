#this file is used by the docker file for creating the base image that suports todd+tomcat
# Starting services
/etc/init.d/freeradius start >/dev/null 2>/dev/null
/etc/init.d/tacacs_plus start >/dev/null 2>/dev/null
service ssh start

clear

# Launching shell
cd
exec bash -i
