# Starting services
/etc/init.d/freeradius start >/dev/null 2>/dev/null
/etc/init.d/tacacs_plus start >/dev/null 2>/dev/null
/usr/sbin/sshd

clear

# Launching shell
cd
exec bash -i
