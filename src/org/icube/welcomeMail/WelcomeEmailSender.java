package org.icube.welcomeMail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
	private final static String MASTER_URL = UtilHelper
			.getConfigProperty("master_sql_url");
	private final static String MASTER_USER = UtilHelper
			.getConfigProperty("master_sql_user");
	private final static String MASTER_PASSWORD = UtilHelper
			.getConfigProperty("master_sql_password");

	public void connectToDb(int companyId) {
		ResultSet companyDetails = null;
		Statement stmt = null;
		// master sql connection
		try {
			Class.forName("com.mysql.jdbc.Driver");
			masterCon = (masterCon != null && !masterCon.isValid(0)) ? masterCon
					: DriverManager.getConnection(MASTER_URL, MASTER_USER,
							MASTER_PASSWORD);
			org.apache.log4j.Logger.getLogger(WelcomeEmailSender.class).debug(
					"Successfully connected to MySql with master database");
			stmt = masterCon.createStatement();
			companyDetails = stmt
					.executeQuery("SELECT comp_sql_dbname,sql_server,sql_user_id,sql_password FROM company_master where comp_id="
							+ companyId);
			companyDetails.next();
			String sqlUrl = "jdbc:mysql://"
					+ companyDetails.getString("sql_server") + ":3306/"
					+ companyDetails.getString("comp_sql_dbname");
			String sqlUserName = companyDetails.getString("sql_user_id");
			String sqlPassword = companyDetails.getString("sql_password");
			try {
				Class.forName("com.mysql.jdbc.Driver");
				companyCon = (companyCon != null && !companyCon.isValid(0)) ? companyCon
						: DriverManager.getConnection(sqlUrl, sqlUserName,
								sqlPassword);
				org.apache.log4j.Logger
						.getLogger(WelcomeEmailSender.class)
						.debug("Successfully connected to MySql with company database");
				ResultSet employeeDetails = null;
				stmt = companyCon.createStatement();
				employeeDetails = stmt
						.executeQuery("select l.login_id,l.password,e.first_name,e.last_name from login_table as l join employee as e on e.emp_id=l.emp_id");
				while (employeeDetails.next()) {
					sendWelcomeEmail(employeeDetails.getString("login_id"),
							employeeDetails.getString("password"),
							employeeDetails.getString("first_name"));
				}

			} catch (SQLException | ClassNotFoundException e) {
				org.apache.log4j.Logger
						.getLogger(WelcomeEmailSender.class)
						.error("An error occurred while connecting to the company database on : "
								+ sqlUrl + " with user name : " + sqlUserName,
								e);
			}

		} catch (SQLException | ClassNotFoundException e) {
			org.apache.log4j.Logger.getLogger(WelcomeEmailSender.class).error(
					"An error occurred while connecting to the master database on : "
							+ MASTER_URL + " with user name : " + MASTER_USER,
					e);
		}
		try {
			masterCon.close();
			companyCon.close();
		} catch (SQLException e) {
			org.apache.log4j.Logger.getLogger(WelcomeEmailSender.class).error(
					"An error occurred while closing the database connections",
					e);
		}

	}

	public void sendWelcomeEmail(String address, String empPassword,
			String firstName) {
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
				org.apache.log4j.Logger.getLogger(WelcomeEmailSender.class)
						.debug("Unable to get the ip");
			}
			System.out.println(ipAddr.getHostAddress());
			// for (String adr : addresses) {
			msg.setContent(getWelcomeEmailText(address, firstName, empPassword)
					.toString(), "text/html");
			msg.setRecipients(Message.RecipientType.TO, address);
			Transport.send(msg, username, password);
			// }

		} catch (MessagingException | UnsupportedEncodingException e) {
			org.apache.log4j.Logger.getLogger(WelcomeEmailSender.class).error(
					"Error in sending Emails for current questions", e);
		}
	}

	private StringBuilder getWelcomeEmailText(String username,
			String firstName, String password) {
		StringBuilder sb = new StringBuilder();
		File file = new File("html/welcomeOwenMail.html");
		System.out.println(file.getAbsolutePath());
		try (BufferedReader in = new BufferedReader(new FileReader(
				file.getAbsolutePath()))) {
			String str;
			int index = username.indexOf('@');
			String uname = username.substring(0, index + 1);
			int comIndex = username.indexOf('.');
			String compName = username.substring(index + 1, comIndex + 1);
			String dom = username.substring(comIndex + 1);
			while ((str = in.readLine()) != null) {
				if (str.contains("Welcome to my network")) {
					sb.append("<P style=\"MARGIN-BOTTOM: 14px; MIN-HEIGHT: 20px\">Welcome to my network, <B style=\"color:#388E3C;\"> "
							+ firstName
							+ "</B>. I'm so excited you're here...</P>");
				}

				else if (str
						.contains("<B>Username: </B><A href=\"\" style=\"text-decoration: none; cursor: text; pointer-events: none !important; color: #000000;\"><SPAN style=\"text-decoration: none; cursor: text; color: #000000;\">user@</SPAN><SPAN style=\"text-decoration: none; cursor: text; color: #000000;\">name.</SPAN><SPAN style=\"text-decoration: none; cursor: text; color: #000000;\">&#65279;com</SPAN></A></P>")) {
					sb.append("<B>Username: </B><A href=\"\" style=\"text-decoration: none; cursor: text; pointer-events: none !important; color: #000000;\"><SPAN style=\"text-decoration: none; cursor: text; color: #000000;\"> "
							+ uname
							+ "</SPAN><SPAN style=\"text-decoration: none; cursor: text; color: #000000;\">"
							+ compName
							+ "</SPAN><SPAN style=\"text-decoration: none; cursor: text; color: #000000;\">&#65279;"
							+ dom + "</SPAN></A></P>");
				} else if (str
						.contains("<P style=\"MARGIN-BOTTOM: 1em;\"><B>Username: </B>username</P>")) {
					sb.append("<P style=\"MARGIN-BOTTOM: 1em;\"><B>Username: </B> "
							+ username + "</P>");
				} else if (str
						.contains("<P style=\"MARGIN-BOTTOM: 1em;\"><B>Password: </B>password</P>")) {
					sb.append("<P style=\"MARGIN-BOTTOM: 1em;\"><B>Password: </B>"
							+ password + "</P>");
				} else if (str
						.contains("<DIV>You are receiving this email because your email@address.com is registered with OWEN</DIV>")) {
					sb.append("<DIV>You are receiving this email because your "
							+ username + " is registered with OWEN</DIV>");
				} else {
					sb.append(str);
				}
			}
		} catch (IOException e) {
			System.out.println(e);
		}
		System.out.println(sb.toString());
		return sb;
	}
}
