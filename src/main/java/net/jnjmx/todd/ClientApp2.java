package net.jnjmx.todd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Scanner;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import javax.management.remote.*;


public class ClientApp2 {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		try {
			// Connect to a remote MBean Server
			JMXConnector c = javax.management.remote.JMXConnectorFactory
					.connect(new JMXServiceURL(
							"service:jmx:rmi:///jndi/rmi://192.168.56.11:10500/jmxrmi"));

			MBeanServerConnection mbs = c.getMBeanServerConnection();

			/*
			 * MBeanServerConnection conn=new MBeanServerConnection();
			 * ManagementFactory.newPlatformMXBeanProxy(MBeanServerConnection
			 * connection, String mxbeanName, Class<T> mxbeanInterface)
			 */

			Set<ObjectInstance> mbeans = mbs.queryMBeans(null, null);

			for (ObjectInstance mbean : mbeans) {
				System.out.println(mbean.getClassName());
			}
			
			// Lets try to access the MBean net.jnjmx.todd.Server:
			ObjectName son = new ObjectName("todd:id=Server");
			ObjectInstance ob=mbs.getObjectInstance(son);
			
			Long value=(Long)mbs.getAttribute(son, "Uptime");
			
			System.out.println("Uptime=" + value);			

			c.close();
		} catch (Exception ex) {
			System.out.println("Error: unable to connect to MBean Server");
		}
	}
}
