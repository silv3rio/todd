package net.jnjmx.todd;

import javax.management.Notification;
import javax.management.NotificationListener;

public class JMXNotificationListener implements NotificationListener {
	@Override
	public void handleNotification(Notification notification, Object handback) {
		System.out.println("Receivied Notifications");
		System.out.println("======================================");
		System.out.println("Timestamp: " + notification.getTimeStamp());
		System.out.println("Type: " + notification.getType());
		System.out.println("Sequence Number: " + notification.getSequenceNumber());
		System.out.println("Message: " + notification.getMessage());
		System.out.println("User Data: " + notification.getUserData());
		System.out.println("Message: " + notification.getSource());
		System.out.println("======================================");
	}
}

