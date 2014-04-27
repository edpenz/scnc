package nz.ac.squash.windows;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
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
import nz.ac.squash.widget.VTextIcon;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import java.awt.Toolkit;

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
	private JPanel mMatchGrid;

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
		gbc_panel_5.fill = GridBagConstraints.BOTH;
		gbc_panel_5.gridx = 1;
		gbc_panel_5.gridy = 1;
		panel_2.add(panel_5, gbc_panel_5);

		JButton checkinButton = new JButton("Club night check-in/out");
		checkinButton.setOpaque(false);
		checkinButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showCheckinDialog();
			}
		});
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

		JPanel schedulePanel = new JPanel();
		schedulePanel.setOpaque(false);
		GridBagConstraints gbc_schedulePanel = new GridBagConstraints();
		gbc_schedulePanel.insets = new Insets(20, 10, 10, 10);
		gbc_schedulePanel.fill = GridBagConstraints.BOTH;
		gbc_schedulePanel.gridx = 0;
		gbc_schedulePanel.gridy = 1;
		getContentPane().add(schedulePanel, gbc_schedulePanel);
		GridBagLayout gbl_schedulePanel = new GridBagLayout();
		gbl_schedulePanel.columnWidths = new int[] { 96, 0, 0, 0, 0, 0, 0, 0,
				0, 0 };
		gbl_schedulePanel.rowHeights = new int[] { 0, 0, 116, 116, 116, 116,
				116, 116, 0, 0 };
		gbl_schedulePanel.columnWeights = new double[] { 0.0, 1.0, 1.0, 1.0,
				1.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_schedulePanel.rowWeights = new double[] { 0.0, 0.0, 1.0, 1.0, 1.0,
				1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE };
		schedulePanel.setLayout(gbl_schedulePanel);

		JLabel lblNewLabel_6 = new JLabel("Upstairs");
		lblNewLabel_6.setForeground(Color.WHITE);
		lblNewLabel_6.setFont(new Font("Tahoma", Font.PLAIN, 24));
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.gridwidth = 4;
		gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_6.gridx = 1;
		gbc_lblNewLabel_6.gridy = 0;
		schedulePanel.add(lblNewLabel_6, gbc_lblNewLabel_6);

		JLabel lblNewLabel_7 = new JLabel("Downstairs");
		lblNewLabel_7.setForeground(Color.WHITE);
		lblNewLabel_7.setFont(new Font("Tahoma", Font.PLAIN, 24));
		GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
		gbc_lblNewLabel_7.gridwidth = 4;
		gbc_lblNewLabel_7.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_7.gridx = 5;
		gbc_lblNewLabel_7.gridy = 0;
		schedulePanel.add(lblNewLabel_7, gbc_lblNewLabel_7);

		JLabel lblNewLabel = new JLabel("Court 5");
		lblNewLabel.setForeground(Color.WHITE);
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 24));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 1;
		schedulePanel.add(lblNewLabel, gbc_lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Court 6");
		lblNewLabel_1.setForeground(Color.WHITE);
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 24));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 1;
		schedulePanel.add(lblNewLabel_1, gbc_lblNewLabel_1);

		JLabel lblNewLabel_2 = new JLabel("Court 7");
		lblNewLabel_2.setForeground(Color.WHITE);
		lblNewLabel_2.setFont(new Font("Tahoma", Font.PLAIN, 24));
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 3;
		gbc_lblNewLabel_2.gridy = 1;
		schedulePanel.add(lblNewLabel_2, gbc_lblNewLabel_2);

		JLabel lblNewLabel_3 = new JLabel("Court 8");
		lblNewLabel_3.setForeground(Color.WHITE);
		lblNewLabel_3.setFont(new Font("Tahoma", Font.PLAIN, 24));
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 4;
		gbc_lblNewLabel_3.gridy = 1;
		schedulePanel.add(lblNewLabel_3, gbc_lblNewLabel_3);

		JLabel lblNewLabel_4 = new JLabel("Court 1");
		lblNewLabel_4.setForeground(Color.WHITE);
		lblNewLabel_4.setFont(new Font("Tahoma", Font.PLAIN, 24));
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 5;
		gbc_lblNewLabel_4.gridy = 1;
		schedulePanel.add(lblNewLabel_4, gbc_lblNewLabel_4);

		JLabel lblNewLabel_5 = new JLabel("Court 2");
		lblNewLabel_5.setForeground(Color.WHITE);
		lblNewLabel_5.setFont(new Font("Tahoma", Font.PLAIN, 24));
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_5.gridx = 6;
		gbc_lblNewLabel_5.gridy = 1;
		schedulePanel.add(lblNewLabel_5, gbc_lblNewLabel_5);

		JLabel lblCourt = new JLabel("Court 3");
		lblCourt.setForeground(Color.WHITE);
		lblCourt.setFont(new Font("Tahoma", Font.PLAIN, 24));
		GridBagConstraints gbc_lblCourt = new GridBagConstraints();
		gbc_lblCourt.insets = new Insets(0, 0, 5, 5);
		gbc_lblCourt.gridx = 7;
		gbc_lblCourt.gridy = 1;
		schedulePanel.add(lblCourt, gbc_lblCourt);

		JLabel lblCourt_1 = new JLabel("Court 4");
		lblCourt_1.setForeground(Color.WHITE);
		lblCourt_1.setFont(new Font("Tahoma", Font.PLAIN, 24));
		GridBagConstraints gbc_lblCourt_1 = new GridBagConstraints();
		gbc_lblCourt_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblCourt_1.gridx = 8;
		gbc_lblCourt_1.gridy = 1;
		schedulePanel.add(lblCourt_1, gbc_lblCourt_1);

		JLabel rtdlblPastGames = new JLabel();
		rtdlblPastGames.setFont(new Font("Tahoma", Font.PLAIN, 18));
		rtdlblPastGames.setIcon(new VTextIcon(rtdlblPastGames, "Previous",
				VTextIcon.ROTATE_LEFT));
		GridBagConstraints gbc_rtdlblPastGames = new GridBagConstraints();
		gbc_rtdlblPastGames.gridx = 0;
		gbc_rtdlblPastGames.gridy = 2;
		schedulePanel.add(rtdlblPastGames, gbc_rtdlblPastGames);

		JLabel rtdlblNowPlaying = new JLabel();
		rtdlblNowPlaying.setForeground(Color.WHITE);
		rtdlblNowPlaying.setFont(new Font("Tahoma", Font.PLAIN, 18));
		rtdlblNowPlaying.setIcon(new VTextIcon(rtdlblPastGames, "Playing",
				VTextIcon.ROTATE_LEFT));
		GridBagConstraints gbc_rtdlblNowPlaying = new GridBagConstraints();
		gbc_rtdlblNowPlaying.gridx = 0;
		gbc_rtdlblNowPlaying.gridy = 3;
		schedulePanel.add(rtdlblNowPlaying, gbc_rtdlblNowPlaying);

		JLabel rtdlblUpcomingGames = new JLabel();
		rtdlblUpcomingGames.setFont(new Font("Tahoma", Font.PLAIN, 18));
		rtdlblUpcomingGames.setIcon(new VTextIcon(rtdlblPastGames, "Upcoming",
				VTextIcon.ROTATE_LEFT));
		GridBagConstraints gbc_rtdlblUpcomingGames = new GridBagConstraints();
		gbc_rtdlblUpcomingGames.gridheight = 4;
		gbc_rtdlblUpcomingGames.gridx = 0;
		gbc_rtdlblUpcomingGames.gridy = 4;
		schedulePanel.add(rtdlblUpcomingGames, gbc_rtdlblUpcomingGames);

		mMatchGrid = new JPanel();
		mMatchGrid.setOpaque(false);
		GridBagConstraints gbc_mMatchGrid = new GridBagConstraints();
		gbc_mMatchGrid.gridheight = 6;
		gbc_mMatchGrid.gridwidth = 8;
		gbc_mMatchGrid.fill = GridBagConstraints.BOTH;
		gbc_mMatchGrid.gridx = 1;
		gbc_mMatchGrid.gridy = 2;
		schedulePanel.add(mMatchGrid, gbc_mMatchGrid);
		mMatchGrid.setLayout(new GridLayout(0, 8, 0, 0));

		int[] COURTS = new int[] { 5, 6, 7, 8, 1, 2, 3, 4 };
		mMatchPanels = new MatchPanel[8][6];
		for (int time = 0; time < 6; time++) {
			for (int court = 0; court < 8; court++) {
				MatchPanel matchPanel = mMatchPanels[court][time] = new MatchPanel(
						COURTS[court], time - 1);

				mMatchGrid.add(matchPanel);
			}
		}

		JPanel panel_7 = new JPanel();
		panel_7.setOpaque(false);
		GridBagConstraints gbc_panel_7 = new GridBagConstraints();
		gbc_panel_7.insets = new Insets(5, 0, 0, 0);
		gbc_panel_7.fill = GridBagConstraints.BOTH;
		gbc_panel_7.gridx = 0;
		gbc_panel_7.gridy = 8;
		schedulePanel.add(panel_7, gbc_panel_7);
		panel_7.setLayout(new GridLayout(1, 0, 0, 0));

		JButton button = new JButton("\u25BC");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				previousSlot(-1);
			}
		});
		button.setOpaque(false);
		panel_7.add(button);

		JButton btnVvv = new JButton("\u25B2");
		btnVvv.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nextSlot(-1);
			}
		});
		btnVvv.setOpaque(false);
		panel_7.add(btnVvv);

		for (int court = 0; court < 8; court++) {
			final int courtF = court;

			JPanel panel_8 = new JPanel();
			panel_8.setOpaque(false);
			GridBagConstraints gbc_panel_8 = new GridBagConstraints();
			gbc_panel_8.anchor = GridBagConstraints.SOUTH;
			gbc_panel_8.gridx = 1 + courtF;
			gbc_panel_8.gridy = 8;
			schedulePanel.add(panel_8, gbc_panel_8);
			panel_8.setLayout(new GridLayout(1, 0, 0, 0));

			JButton button_1 = new JButton("\u25BE");
			button_1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					previousSlot(courtF);
				}
			});
			button_1.setOpaque(false);
			panel_8.add(button_1);

			JButton button_2 = new JButton("\u25B4");
			button_2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					nextSlot(courtF);
				}
			});
			button_2.setOpaque(false);
			panel_8.add(button_2);
		}

		JPanel panel_1 = new JPanel();
		panel_1.setBackground(Color.LIGHT_GRAY);
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.gridwidth = 9;
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 2;
		schedulePanel.add(panel_1, gbc_panel_1);

		JPanel panel = new JPanel();
		panel.setBackground(Color.decode("#74B2E3"));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 9;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 3;
		schedulePanel.add(panel, gbc_panel);

		JPanel panel_3 = new JPanel();
		panel_3.setBackground(Color.WHITE);
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.gridwidth = 9;
		gbc_panel_3.gridheight = 4;
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 4;
		schedulePanel.add(panel_3, gbc_panel_3);
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

	private void showCheckinDialog() {
		getGlassPane().setVisible(true);

		CheckInWindow dialog = CheckInWindow.showDialog(getOwner());

		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				getGlassPane().setVisible(false);
			}
		});
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
