package org.icube.owen.ui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.PropertyConfigurator;
import org.icube.owen.dao.CompanyDAO;

public class CompanyConnectionApp extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	static {
		PropertyConfigurator.configure("resources/log4j.properties");

	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CompanyConnectionApp frame = new CompanyConnectionApp();
					frame.setVisible(true);
				} catch (Exception e) {
					org.apache.log4j.Logger.getLogger(CompanyConnectionApp.class).error("Unable to load the Job Scheduler App");
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public CompanyConnectionApp() {
		Timer timer = new Timer();
		setTitle("Job Scheduler");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JButton btnStart = new JButton("START");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Calendar today = Calendar.getInstance();
				// set the start date to be 12:01 AM
				//today.add(Calendar.DAY_OF_MONTH, 1);
				today.set(Calendar.HOUR_OF_DAY, 10);
				today.set(Calendar.MINUTE, 45);
				today.set(Calendar.SECOND, 0);

				try {
					System.out.println(today.getTime());
					CompanyDAO cdao = new CompanyDAO();
					timer.scheduleAtFixedRate(cdao, today.getTime(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
					// timer.scheduleAtFixedRate(cdao, today.getTime(), 300000);

				} catch (Exception e) {
					org.apache.log4j.Logger.getLogger(CompanyConnectionApp.class).error("Unable to execute the scheduled task");
					e.printStackTrace();
				}

			}
		});
		btnStart.setBounds(170, 151, 97, 25);
		contentPane.add(btnStart);
	}
}
