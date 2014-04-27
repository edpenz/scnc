package nz.ac.squash.windows;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import nz.ac.squash.util.Importer;
import nz.ac.squash.util.Utility;
import nz.ac.squash.widget.JBrandedPanel;
import nz.ac.squash.widget.JOverlay;
import nz.ac.squash.widget.MatchPanel;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import panels.CheckInPanel;
import panels.SchedulePanel;

public class ClientWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		Utility.seedRestart(ClientWindow.class, args);

		// Logging.
		Layout logLayout = new PatternLayout("[%t] %-5p %c %x - %m%n");

		BasicConfigurator.configure(new ConsoleAppender(logLayout));
		try {
			BasicConfigurator.configure(new FileAppender(logLayout, "logs/"
					+ Utility.FILE_SAFE_FORMATTER.format(new Date()) + ".log"));
		} catch (IOException e1) {
		}

		Logger.getLogger("org.hibernate").setLevel(Level.INFO);

		// Theme GUI.
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			// Fallback to default look-and-feel is fine.
		}

		// Import data.
		Importer.importFromCsv("db");

		// Start main GUI window.
		new ClientWindow().setVisible(true);
	}

	private MatchPanel[][] mMatchPanels;
	private JPanel mPanelFrame;
	private SchedulePanel mSchedulePanel;
	private CheckInPanel mCheckinPanel;

	private ClientWindow() {
		// setSize(Toolkit.getDefaultToolkit().getScreenSize());
		createContents();
	}

	private void createContents() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(
				ClientWindow.class.getResource("/images/Icon.png")));
		JPanel panel_6 = new JBrandedPanel();
		setContentPane(panel_6);
		setGlassPane(new JOverlay());

		setSize(new Dimension(1280, 800));
		setLocation(0, 0);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("AUSC Scheduler");
		setResizable(false);
		setUndecorated(true);
		GridBagLayout gridBagLayout_5 = new GridBagLayout();
		gridBagLayout_5.columnWidths = new int[] { 0, 0 };
		gridBagLayout_5.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout_5.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout_5.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout_5);

		JPanel panel_2 = new JPanel();
		panel_2.setOpaque(false);
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 0;
		getContentPane().add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 0, 0, 0 };
		gbl_panel_2.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_panel_2.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 0.0, 0.0, 1.0, 1.0,
				Double.MIN_VALUE };
		panel_2.setLayout(gbl_panel_2);

		JLabel lblNewLabel_8 = new JLabel("ausc");
		lblNewLabel_8.setFont(new Font("Tahoma", Font.PLAIN, 64));
		lblNewLabel_8.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblNewLabel_8 = new GridBagConstraints();
		gbc_lblNewLabel_8.insets = new Insets(-28, 0, 0, 0);
		gbc_lblNewLabel_8.gridwidth = 2;
		gbc_lblNewLabel_8.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblNewLabel_8.gridx = 0;
		gbc_lblNewLabel_8.gridy = 0;
		panel_2.add(lblNewLabel_8, gbc_lblNewLabel_8);

		JLabel lblNewLabel_9 = new JLabel("auckland university squash club");
		lblNewLabel_9.setForeground(Color.decode("#0153A5"));
		lblNewLabel_9.setFont(new Font("Tahoma", Font.PLAIN, 28));
		GridBagConstraints gbc_lblNewLabel_9 = new GridBagConstraints();
		gbc_lblNewLabel_9.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_9.gridx = 0;
		gbc_lblNewLabel_9.gridy = 1;
		panel_2.add(lblNewLabel_9, gbc_lblNewLabel_9);

		JPanel panel_5 = new JPanel();
		panel_5.setOpaque(false);
		panel_5.setBackground(Color.WHITE);
		GridBagConstraints gbc_panel_5 = new GridBagConstraints();
		gbc_panel_5.insets = new Insets(10, 10, 10, 10);
		gbc_panel_5.gridheight = 2;
		gbc_panel_5.fill = GridBagConstraints.BOTH;
		gbc_panel_5.gridx = 1;
		gbc_panel_5.gridy = 0;
		panel_2.add(panel_5, gbc_panel_5);

		JButton checkinButton = new JButton("Check-in / Ladder");
		checkinButton.setOpaque(false);
		checkinButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showPanel(CheckInPanel.class);
			}
		});
		panel_5.setLayout(new GridLayout(1, 0, 10, 0));

		JButton scheduleButton = new JButton("Schedule");
		scheduleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showPanel(SchedulePanel.class);
			}
		});
		scheduleButton.setOpaque(false);
		panel_5.add(scheduleButton);
		panel_5.add(checkinButton);
		JButton challengeButton = new JButton("Custom match");
		challengeButton.setOpaque(false);
		challengeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showChallengeDialog();
			}
		});
		panel_5.add(challengeButton);

		JPanel panel_4 = new JPanel();
		panel_4.setBackground(Color.WHITE);
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.gridwidth = 2;
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 1;
		panel_2.add(panel_4, gbc_panel_4);

		mPanelFrame = new JPanel();
		mPanelFrame.setOpaque(false);
		GridBagConstraints gbc_mPanelFrame = new GridBagConstraints();
		gbc_mPanelFrame.insets = new Insets(10, 10, 10, 10);
		gbc_mPanelFrame.fill = GridBagConstraints.BOTH;
		gbc_mPanelFrame.gridx = 0;
		gbc_mPanelFrame.gridy = 1;
		getContentPane().add(mPanelFrame, gbc_mPanelFrame);
		mPanelFrame.setLayout(new CardLayout(0, 0));

		mSchedulePanel = new SchedulePanel();
		mPanelFrame.add(mSchedulePanel, SchedulePanel.class.getSimpleName());

		mCheckinPanel = new CheckInPanel();
		mPanelFrame.add(mCheckinPanel, CheckInPanel.class.getSimpleName());
	}

	private void nextSlot(int court) {
		int start = court == -1 ? 0 : court;
		int end = court == -1 ? mMatchPanels.length : court + 1;

		for (int c = start; c < end; ++c) {
			for (int slot = 0; slot < mMatchPanels[c].length; slot++) {
				final MatchPanel matchPanel = mMatchPanels[c][slot];
				matchPanel.nextSlot();
			}
		}
	}

	private void previousSlot(int court) {
		int start = court == -1 ? 0 : court;
		int end = court == -1 ? mMatchPanels.length : court + 1;

		for (int c = start; c < end; ++c) {
			for (int slot = 0; slot < mMatchPanels[c].length; slot++) {
				final MatchPanel matchPanel = mMatchPanels[c][slot];
				matchPanel.previousSlot();
			}
		}
	}

	private void showPanel(Class<?> panel) {
		String cardName = panel.getSimpleName();
		((CardLayout) mPanelFrame.getLayout()).show(mPanelFrame, cardName);
	}

	private void showChallengeDialog() {
		getGlassPane().setVisible(true);

		ChallengeWindow dialog = ChallengeWindow.showDialog(getOwner());

		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				getGlassPane().setVisible(false);
			}
		});
	}
}
