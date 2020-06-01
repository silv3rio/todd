package net.jnjmx.todd;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import javax.management.*;
import javax.management.monitor.CounterMonitor;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.math.RoundingMode;
import java.text.DecimalFormat;


public class ClientApp4 {
	private static MBeanServerConnection conn;
	private static int poolSizeS=8; //nota: solução requer tb um listener para alteraçoes externas ao pool size
	private static int disponiveis=poolSizeS;
	public static void growPool() throws Exception{

		String server = "192.168.20.20:10500";


		JMXConnector c = javax.management.remote.JMXConnectorFactory
				.connect(new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + server + "/jmxrmi"));

		MBeanServerConnection conn = c.getMBeanServerConnection();
		String[] signatures = new String[] {
				String.class.getName()
		};

		System.out.println("Trying to grow pool by 10...");

		send_nrdp("passive_jmx_check",1,"trying to grow pool size by 10");

		conn.invoke(new ObjectName("todd:id=SessionPool"), "grow",new Object[] {10}, new String[] { int.class.getName() });
		poolSizeS+=10;
		System.out.println("Pool was grown to "+poolSizeS);

	}
	public static void collectGarbage() throws Exception{
		try {
			String tomcatServer = "192.168.20.20:9000";
			JMXConnector tomcatConnector = javax.management.remote.JMXConnectorFactory
					.connect(new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + tomcatServer + "/jmxrmi"));

			MBeanServerConnection mbs = tomcatConnector.getMBeanServerConnection();
			ObjectName spmon = new ObjectName("java.lang:type=Memory");
			mbs.invoke(spmon, "gc", null, null);
			System.out.println("just called the garbage collector. if it's not enought i'll try to restart the JVM with more memory");
			javax.management.openmbean.CompositeDataSupport res= (CompositeDataSupport) mbs.getAttribute(new ObjectName("java.lang:type=Memory"), "HeapMemoryUsage");
			//commited,init,max,used
			double maxMemory=Double.parseDouble(res.get("max").toString());
			double usedMemory=Double.parseDouble(res.get("used").toString());
			double percent=Math.round( usedMemory/maxMemory*100);
			if(percent<10) {
				System.out.println("the mem percentage is " + percent + "%. So far so good! Let's tell nagios it's all ok");
				send_nrdp("catalina_info",0,"Catalina tá OK com a memoria a "+percent+"%");
			}
			else{ //i'll consider it critical so let's restart this thing with more memory!
				//send_nrdp("catalina_info",1,"Catalina tá fixe com a memoria a "+percent+"%. garbage collection worked");
				System.out.println("the mem percentage is high at "+percent);
				mbs.invoke(spmon, "gc", null, null);
				System.out.println("just called the garbage collector. if it's not enought i'll try to restart the JVM with more memory");
				res= (CompositeDataSupport) mbs.getAttribute(new ObjectName("java.lang:type=Memory"), "HeapMemoryUsage");
				maxMemory=Double.parseDouble(res.get("max").toString());
				usedMemory=Double.parseDouble(res.get("used").toString());
				percent=Math.round( usedMemory/maxMemory*100);
				if(percent<10) {
					System.out.println("the mem percentage is now " + percent + "%. So far so good! Let's tell nagios it's all ok");
					send_nrdp("catalina_info",0,"Catalina está ok com a memoria a "+percent+"%. garbage collection worked");
				}
				else{
					System.out.println("things are baaad! i'm restarting JVM with more memory!");
					send_nrdp("catalina_info",1,"things are baaad! i'm restarting JVM with more memory!");
					//NOTA: esta soluçao so funciona com esta app a correr na maquina do tomcat. n é esta a ideia!
					//comandos linux para aumentar memoria
					//export _JAVA_OPTIONS="-Xmx50M"
					//comando que mostra a mem
					//java -XX:+PrintFlagsFinal -version | grep HeapSize
					String comando="export _JAVA_OPTIONS=\"-Xmx2G\"";//aumenta heapsize pra 2GB
					comando+="\n";
					comando+="sudo systemctl restart tomcat7";
					comando+="\n";
					comando+="sudo echo \"just a testttt\" >> /tmp/mytest.txt";
					try {
						ProcessBuilder processBuilder = new ProcessBuilder();
						processBuilder.command("bash", "-c", comando);
						Process process = processBuilder.start();
						process.waitFor();
					}
					catch (Exception exc){}//n faco nada aqui pq isto é so pra testes. comando n funciona em windows!
				}
			}
		}
		catch(Exception exc){
			System.out.println(exc.toString());
		}
	}
	/**
	 * The first two lines here create and register a GaugeMonitor MBean named
	 * todd:id=SessionPoolMonitor. The next seven lines set attributes that tell
	 * GaugeMonitor which attribute of which MBean should be monitored
	 * (ObservedAttribute or ObservedObject), how often (GranularityPeriod, in
	 * milliseconds), and whether or not to send a notification on high-threshold
	 * and low-threshold violations. Then we invoke the setThresholds() method, via
	 * the MBeanServer, to set the actual high and low threshold values. Finally, we
	 * make the server listen for session pool monitor notifications and start the
	 * gauge monitor.
	 */
	public static void configureMonitor1(MBeanServerConnection mbs) throws Exception {


		ObjectName spmon = new ObjectName("todd:id=SessionPoolMonitor");

		Set<ObjectInstance> mbeans = mbs.queryMBeans(spmon, null);

		if (mbeans.isEmpty()) {
			mbs.createMBean("javax.management.monitor.GaugeMonitor", spmon);
		} else {
			// noting to do...
		}

		AttributeList spmal = new AttributeList();
		spmal.add(new Attribute("ObservedObject", new ObjectName("todd:id=SessionPool")));
		//spmal.add(new Attribute("ObservedAttribute", "Size"));
		spmal.add(new Attribute("ObservedAttribute", "AvailableSessions"));
		spmal.add(new Attribute("GranularityPeriod", new Long(10000)));
		spmal.add(new Attribute("NotifyHigh", new Boolean(true)));
		spmal.add(new Attribute("NotifyLow", new Boolean(true)));
		mbs.setAttributes(spmon, spmal);


		int  poolSize= (int)mbs.getAttribute(new ObjectName("todd:id=SessionPool"),"Size");
		System.out.println("Current pool size is: "+poolSize);
		poolSizeS=poolSize;
		int lowThreshold=(int)(poolSizeS*0.2);//alterar este valor para valor calculado com base em 20% do Size
		int highThreshold=200;
		//lowThreshold=poolSize-1;
		System.out.println("Low Threshold is: "+lowThreshold+" (20% of initial size!)");
		mbs.invoke(spmon, "setThresholds", new Object[] { highThreshold, lowThreshold },
				new String[] { "java.lang.Number", "java.lang.Number" });
		System.out.println("Now i'm just sitting still, watching praca da alegria and waiting for the available sessions to decrease...");
		mbs.addNotificationListener(spmon, new JMXNotificationListener2(), null, null);
		//o todd tem um "bug", se baixar manualmente o size da sessions pool os disponiveis ficam acima do size
		mbs.invoke(spmon, "start", new Object[] {}, new String[] {});
	}

	public static void configureMonitorTomcat(MBeanServerConnection mbs) throws Exception {

		try {
			System.out.println("starting config for tomcat");
			ObjectName spmon = new ObjectName("java.lang:type=Memory");

			Set<ObjectInstance> mbeans = mbs.queryMBeans(spmon, null);

			if (mbeans.isEmpty()) {
				mbs.createMBean("javax.management.monitor.GaugeMonitor", spmon);
			} else {
				// noting to do...
			}
			/*
			AttributeList spmal = new AttributeList();
			spmal.add(new Attribute("ObservedObject", new ObjectName("java.lang:type=Memory")));
			spmal.add(new Attribute("ObservedAttribute", "HeapMemoryUsage"));
			spmal.add(new Attribute("GranularityPeriod", new Long(10000)));
			spmal.add(new Attribute("Notify", new Boolean(true)));
			spmal.add(new Attribute("InitThreshold", new Integer(10)));
			spmal.add(new Attribute("Offset", new Integer(2)));
			spmal.add(new Attribute("DifferenceMode", new Boolean(false)));
			mbs.setAttributes(spmon, spmal);
			*/
			javax.management.openmbean.CompositeDataSupport res= (CompositeDataSupport) mbs.getAttribute(new ObjectName("java.lang:type=Memory"), "HeapMemoryUsage");
			//commited,init,max,used
			double maxMemory=Double.parseDouble(res.get("max").toString());
			double usedMemory=Double.parseDouble(res.get("used").toString());
			double percent=Math.round( usedMemory/maxMemory*100);
			System.out.println("memoria usada="+usedMemory+" total="+maxMemory+" percentagem="+percent+"%");


			int highThreshold=(int)(maxMemory*0.1);//meti 10% para testes, alterar para 80%

			mbs.addNotificationListener(spmon, new JMXNotificationListener3(), null, null);
			//o todd tem um "bug", se baixar manualmente o size da sessions pool os disponiveis ficam acima do size



			System.out.println("Invoked the initial garbage collector and now i'm just watching the memory of the JVM");

			//just testing the garbage collector function now
			collectGarbage();
		}
		catch(Exception exc){
			System.out.println(exc.toString());
		}
	}

	private static void send_nrdp(String servico,int codigo,String mensagem) throws IOException {
		try {

			String comando="/tmp/send_nrdp.py -u http://192.168.10.5/nrdp/ -t tokendosilverio -H tomcat -s "+servico+" -S "+codigo+" -o \""+mensagem+"\"";

			//Process process = Runtime.getRuntime().exec(comando);

			ProcessBuilder processBuilder = new ProcessBuilder();
			// Windows
			processBuilder.command("bash", "-c", comando);
			Process process = processBuilder.start();
			process.waitFor();
			//System.out.println("executed: ");
		}
		catch (Exception ex){
			//System.out.println(ex.toString());
		}
	}
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		System.out.println("Welcome to Silverio's monitoring app. We're monitoring TODD and TOMCAT in the same app!");

		try {

			String server = "127.0.0.1:10500";
			String tomcatServer = "192.168.20.20:9000";
			if (args.length >= 1) {
				server = args[0];
			}

			System.out.println("Connecting to TODD server at "+server+" ...");
			System.out.println("Connecting to tomcat server at "+tomcatServer+" ...");

			// Connect to a remote MBean Server
			JMXConnector c = javax.management.remote.JMXConnectorFactory
					.connect(new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + server + "/jmxrmi"));
			JMXConnector tomcatConnector = javax.management.remote.JMXConnectorFactory
					.connect(new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + tomcatServer + "/jmxrmi"));

			MBeanServerConnection mbs = c.getMBeanServerConnection();
			MBeanServerConnection tomcatMBS = tomcatConnector.getMBeanServerConnection();

			System.out.println("Setting up notification handlers...");
			
			// Set a Notification Handler
			configureMonitor1(mbs);
			configureMonitorTomcat(tomcatMBS);
			

			//em vez de correr este cliente 60 segundos fica sempre a correr
			//este metodo faria mais sentido executar enquanto o server todd ta a correr
			//e depois fechar a conexao mas para efeito de testes deixei um while true
			while(true)
			{
				send_nrdp("passive_jmx_check",0,"todd ta ok com "+disponiveis+" sessoes disponiveis");
				Thread.sleep(60000);
				collectGarbage();
			}
			//c.close();
		} catch (Exception ex) {
			System.out.println("Error: unable to connect to MBean Server");
		}
	}
}
