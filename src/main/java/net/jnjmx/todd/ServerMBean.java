package net.jnjmx.todd;

public interface ServerMBean {
	void shutdown();
	void start();
	void stop();
	
	Integer getConnections();
	Integer getSessions();
	Long getUptime();
}
