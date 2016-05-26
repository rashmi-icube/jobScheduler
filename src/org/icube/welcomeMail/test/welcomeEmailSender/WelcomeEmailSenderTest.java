package org.icube.welcomeMail.test.welcomeEmailSender;

import org.icube.welcomeMail.WelcomeEmailSender;
import org.junit.Test;



public class WelcomeEmailSenderTest {
	WelcomeEmailSender wes = new WelcomeEmailSender();
	
	@Test
	public void testConnectToDb(){
		wes.connectToDb(2);
	}

}
