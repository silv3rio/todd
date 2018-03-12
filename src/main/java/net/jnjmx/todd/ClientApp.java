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
			Client c=new Client();
			
			System.out.println("The current time of day on todd server is: "+c.timeOfDay());
			
			c.close();
			
			System.out.println("Exiting...");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

}
