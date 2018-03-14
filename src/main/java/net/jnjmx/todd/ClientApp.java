package net.jnjmx.todd;

import java.io.IOException;

public class ClientApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Todd Client App...");
		System.out.println("Connecting to todd server");
		
		try {
			Client c=new Client("192.168.56.11");
			
			System.out.println("The current time of day on todd server is: "+c.timeOfDay());
			
			System.out.println("Press enter to exit...");
			System.in.read();
			
			c.close();
			
			System.out.println("Exiting...");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

}
