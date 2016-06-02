package org.icube.welcomeMail;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class WelcomeEmailSender {

	public Connection masterCon;
	public Connection companyCon;
	private final static String MASTER_URL = UtilHelper.getConfigProperty("master_sql_url");
	private final static String MASTER_USER = UtilHelper.getConfigProperty("master_sql_user");
	private final static String MASTER_PASSWORD = UtilHelper.getConfigProperty("master_sql_password");

	public void connectToDb(int companyId) {
		ResultSet companyDetails = null;
		Statement stmt = null;
		// master sql connection
		try {
			Class.forName("com.mysql.jdbc.Driver");
			masterCon = (masterCon != null && !masterCon.isValid(0)) ? masterCon : DriverManager.getConnection(MASTER_URL, MASTER_USER,
					MASTER_PASSWORD);
			org.apache.log4j.Logger.getLogger(WelcomeEmailSender.class).debug("Successfully connected to MySql with master database");
			stmt = masterCon.createStatement();
			companyDetails = stmt.executeQuery("SELECT comp_sql_dbname,sql_server,sql_user_id,sql_password FROM company_master where comp_id="
					+ companyId);
			companyDetails.next();
			String sqlUrl = "jdbc:mysql://" + companyDetails.getString("sql_server") + ":3306/" + companyDetails.getString("comp_sql_dbname");
			String sqlUserName = companyDetails.getString("sql_user_id");
			String sqlPassword = companyDetails.getString("sql_password");
			try {
				Class.forName("com.mysql.jdbc.Driver");
				companyCon = (companyCon != null && !companyCon.isValid(0)) ? companyCon : DriverManager.getConnection(sqlUrl, sqlUserName,
						sqlPassword);
				org.apache.log4j.Logger.getLogger(WelcomeEmailSender.class).debug("Successfully connected to MySql with company database");
				ResultSet employeeDetails = null;
				stmt = companyCon.createStatement();
				employeeDetails = stmt
						.executeQuery("select l.login_id,l.password,e.first_name,e.last_name from login_table as l join employee as e on e.emp_id=l.emp_id");
				while (employeeDetails.next()) {
					sendWelcomeEmail(employeeDetails.getString("login_id"), employeeDetails.getString("password"), employeeDetails
							.getString("first_name"));
				}

			} catch (SQLException | ClassNotFoundException e) {
				org.apache.log4j.Logger.getLogger(WelcomeEmailSender.class).error(
						"An error occurred while connecting to the company database on : " + sqlUrl + " with user name : " + sqlUserName, e);
			}

		} catch (SQLException | ClassNotFoundException e) {
			org.apache.log4j.Logger.getLogger(WelcomeEmailSender.class).error(
					"An error occurred while connecting to the master database on : " + MASTER_URL + " with user name : " + MASTER_USER, e);
		}
		try {
			masterCon.close();
			companyCon.close();
		} catch (SQLException e) {
			org.apache.log4j.Logger.getLogger(WelcomeEmailSender.class).error("An error occurred while closing the database connections", e);
		}

	}

	public void sendWelcomeEmail(String address, String empPassword, String firstName) {
		String host = "smtp.zoho.com";
		String username = "owen@owenanalytics.com";
		String password = "Abcd@654321";
		String from = "owen@owenanalytics.com";
		Properties props = new Properties();
		props.put("mail.debug", "true");
		props.put("mail.smtp.ssl.enable", "true");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", 465);
		Session session = Session.getInstance(props);
		MimeMessage msg = new MimeMessage(session);
		try {
			msg.setFrom(new InternetAddress(from, "OWEN"));
			msg.setSubject("Welcome to the OWEN network");

			InetAddress ipAddr = null;
			try {
				ipAddr = InetAddress.getLocalHost();
			} catch (Exception e) {
				org.apache.log4j.Logger.getLogger(WelcomeEmailSender.class).debug("Unable to get the ip");
			}
			System.out.println(ipAddr.getHostAddress());
			// for (String adr : addresses) {
			msg.setContent(getWelcomeEmailText(address, firstName, empPassword).toString(), "text/html");
			msg.setRecipients(Message.RecipientType.TO, address);
			Transport.send(msg, username, password);
			// }

		} catch (MessagingException | UnsupportedEncodingException e) {
			org.apache.log4j.Logger.getLogger(WelcomeEmailSender.class).error("Error in sending Emails for current questions", e);
		}
	}

	private StringBuilder getWelcomeEmailText(String username, String firstName, String password) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<body>");

		sb.append("<p><b><i>WELCOME TO MY NETWORK, " + firstName + ". I'M SO EXCITED YOU'RE HERE...</i></b></p>");
		sb.append("<p>I am on a mission to help you engage better at your workplace, by answering small surveys. This should be easy.</p>");
		sb.append("<p>Get started right away with just a few simple steps. And don't worry, I'll send you little reminders to answer my surveys. I'm nice like that.");
		sb.append("<p>Your Login Credentials : </p>");
		sb.append("<p><b>Username : " + username + "</b><br><b>Password : " + password + " </b></p>");
		sb.append("<p>When I created your profile, I didn't ask you to setup a password. It's time to update your password now.</p>");
		sb.append("<p>Step 1: Update your User Profile<br><i>Go to the Settings page and setup a profile picture, update your work experience, education and more</i></p>");
		sb.append("<p>Step 2: Answer a survey<br><i>Give stars to people you like, to show them your appreciation</i></p>");
		sb.append("<p>Step 3: Access your dashboard<br><i>Measure your expertise, mentorship and influence within and outside of your team</i></p>");

		sb.append("<a href=http://ec2-52-35-113-15.us-west-2.compute.amazonaws.com:8080/login.jsp><i><b>Log in to My account now</b></i></a>");
		sb.append("<p>If you have feedback or need any help, I am standing by. Just send me an email at support@owenanalytics.com</p> <p>Follow me on Twitter @owen_analytics </p>");

		sb.append("</div>");

		sb.append("</body>");
		sb.append("</html>");
		return sb;
	}
}
