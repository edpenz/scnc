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
import java.awt.event.KeyEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import nz.ac.squash.panels.LadderPanel;
import nz.ac.squash.panels.SchedulePanel;
import nz.ac.squash.panels.SignInPanel;
import nz.ac.squash.util.Utility;
import nz.ac.squash.widget.JBrandedPanel;
import nz.ac.squash.widget.MatchPanel;
import nz.ac.squash.widget.generic.JOverlay;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.pollerosoftware.log4j.additions.appenders.LazyFileAppender;

public class ClientWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        Utility.seedRestart(ClientWindow.class, args);

        // Logging.
        Layout consoleLayout = new PatternLayout("[%t] %-5p %c %x - %m%n");
        Layout fileLayout = new PatternLayout(
                "%d [%15.15t] %-5p %30.30c %x - %m%n");

        // To console.
        ConsoleAppender consoleAppender = new ConsoleAppender(consoleLayout);
        consoleAppender.setThreshold(Level.TRACE);
        BasicConfigurator.configure(consoleAppender);

        // To file.
        FileAppender fileAppender;
        fileAppender = new LazyFileAppender();
        fileAppender.setLayout(fileLayout);
        fileAppender.setFile("logs/" +
                             Utility.FILE_SAFE_FORMATTER.format(new Date()) +
                             ".log");
        fileAppender.activateOptions();
        fileAppender.setThreshold(Level.INFO);
        BasicConfigurator.configure(fileAppender);

        Logger.getRootLogger().setLevel(Level.ALL);
        Logger.getLogger("org.hibernate").setLevel(Level.WARN);
        Logger.getLogger("com.mchange").setLevel(Level.WARN);

        // Theme GUI.
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // Fallback to default look-and-feel is fine.
        }

        // Start main GUI window.
        new ClientWindow().setVisible(true);
    }

    private JPanel mPanelFrame;
    private SchedulePanel mSchedulePanel;
    private SignInPanel mSignInPanel;
    private LadderPanel mLadderPanel;

    private ClientWindow() {
        createContents();

        setSize(Toolkit.getDefaultToolkit().getScreenSize());

        getRootPane().getActionMap().put("stats", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StatsWindow.showDialog(ClientWindow.this);
            }
        });
        getRootPane().getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "stats");

        getRootPane().getActionMap().put("schedule", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (MatchPanel panel : mSchedulePanel.getMatchPanels()) {
                    panel.enableSchedule();
                }
            }
        });
        getRootPane().getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "schedule");

        getRootPane().getActionMap().put("import", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ImportWindow.showDialog(ClientWindow.this);
            }
        });
        getRootPane().getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "import");
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

        JButton checkinButton = new JButton("Sign in");
        checkinButton.setOpaque(false);
        checkinButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPanel(SignInPanel.class);
                mSignInPanel.clear();
            }
        });
        panel_5.setLayout(new GridLayout(1, 0, 10, 0));

        JButton scheduleButton = new JButton("Match Schedule");
        scheduleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPanel(SchedulePanel.class);
            }
        });
        scheduleButton.setOpaque(false);
        panel_5.add(scheduleButton);
        panel_5.add(checkinButton);
        JButton challengeButton = new JButton("Request a Match");
        challengeButton.setOpaque(false);
        challengeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showChallengeDialog();
            }
        });

        JButton ladderButton = new JButton("Ladder");
        ladderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPanel(LadderPanel.class);
                mLadderPanel.refreshLadder();
            }
        });
        ladderButton.setOpaque(false);
        panel_5.add(ladderButton);
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

        mSignInPanel = new SignInPanel();
        mPanelFrame.add(mSignInPanel, SignInPanel.class.getSimpleName());

        mLadderPanel = new LadderPanel();
        mPanelFrame.add(mLadderPanel, LadderPanel.class.getSimpleName());
    }

    private void showPanel(Class<?> panel) {
        String cardName = panel.getSimpleName();
        ((CardLayout) mPanelFrame.getLayout()).show(mPanelFrame, cardName);
    }

    private void showChallengeDialog() {
        ChallengeWindow.showDialog(this);
    }
}
