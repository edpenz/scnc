package nz.ac.squash.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;
import nz.ac.squash.db.beans.MatchResult;
import nz.ac.squash.db.beans.Member;
import nz.ac.squash.db.beans.Member.MemberResults;
import nz.ac.squash.db.beans.MemberStatus;
import nz.ac.squash.util.LatestExecutor;
import nz.ac.squash.util.Utility;
import nz.ac.squash.widget.LadderEntry;
import nz.ac.squash.widget.generic.JTextField;
import nz.ac.squash.widget.generic.VerticalGridLayout;
import nz.ac.squash.windows.RegisterWindow;

import org.apache.commons.lang3.StringUtils;

public class CheckInPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private JTextField mSearchField;
    private JList<Member> mResultsList;
    private JPanel mButtonsPanel;
    private JLabel mStep3Label;
    private JLabel mStep2Label;
    private JRadioButton mLevel1Radio;
    private JRadioButton mLevel2Radio;
    private JRadioButton mLevel3Radio;
    private JRadioButton mLevel4Radio;
    private ButtonGroup mSkillRadioGroup;
    private JLabel label;
    private JLabel lblLadder;
    private JPanel mLadderGrid;
    private JButton playManualButton;

    private static final ListModel<Member> EMPTY_RESULTS = new DefaultListModel<Member>();

    private ExecutorService mSearchTask = new LatestExecutor();
    private ActionListener mSkillButtonHandler = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            enableMode();
        }
    };

    private Member mSelectedMember = null;

    private final Map<Member, LadderEntry> mLadderMapping = new HashMap<Member, LadderEntry>();

    public CheckInPanel() {
        createContents();

        refreshLadder();
    }

    private void createContents() {
        setOpaque(false);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] { 40, 0, 40, 0, 40, 0 };
        gridBagLayout.rowHeights = new int[] { 20, 0, 0, 40 };
        gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0,
                Double.MIN_VALUE };
        gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0 };
        setLayout(gridBagLayout);

        label = new JLabel("Check-in to club night");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Tahoma", Font.PLAIN, 32));
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.insets = new Insets(0, 20, 10, 20);
        gbc_label.gridx = 1;
        gbc_label.gridy = 1;
        add(label, gbc_label);

        lblLadder = new JLabel("Ladder");
        lblLadder.setForeground(Color.WHITE);
        lblLadder.setFont(new Font("Tahoma", Font.PLAIN, 32));
        GridBagConstraints gbc_lblLadder = new GridBagConstraints();
        gbc_lblLadder.insets = new Insets(0, 20, 10, 20);
        gbc_lblLadder.gridx = 3;
        gbc_lblLadder.gridy = 1;
        add(lblLadder, gbc_lblLadder);

        JPanel checkinPanel = new JPanel();
        checkinPanel.setOpaque(false);
        GridBagConstraints gbc_checkinPanel = new GridBagConstraints();
        gbc_checkinPanel.fill = GridBagConstraints.BOTH;
        gbc_checkinPanel.gridx = 1;
        gbc_checkinPanel.gridy = 2;
        add(checkinPanel, gbc_checkinPanel);
        GridBagLayout gbl_checkinPanel = new GridBagLayout();
        gbl_checkinPanel.columnWidths = new int[] { 0 };
        gbl_checkinPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
        gbl_checkinPanel.columnWeights = new double[] { 1.0 };
        gbl_checkinPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 1.0 };
        checkinPanel.setLayout(gbl_checkinPanel);

        JLabel lblSelectYour = new JLabel("1: Select your name below");
        GridBagConstraints gbc_lblSelectYour = new GridBagConstraints();
        gbc_lblSelectYour.insets = new Insets(5, 5, 5, 0);
        gbc_lblSelectYour.anchor = GridBagConstraints.WEST;
        gbc_lblSelectYour.gridx = 0;
        gbc_lblSelectYour.gridy = 0;
        checkinPanel.add(lblSelectYour, gbc_lblSelectYour);
        lblSelectYour.setForeground(Color.WHITE);
        lblSelectYour.setFont(new Font("Tahoma", Font.PLAIN, 16));

        mSearchField = new JTextField();
        mSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER &&
                    mSelectedMember != null) {
                    setMemberStatus(true, true);
                }
            }
        });
        GridBagConstraints gbc_mSearchField = new GridBagConstraints();
        gbc_mSearchField.insets = new Insets(5, 5, 0, 5);
        gbc_mSearchField.anchor = GridBagConstraints.NORTH;
        gbc_mSearchField.fill = GridBagConstraints.HORIZONTAL;
        gbc_mSearchField.gridx = 0;
        gbc_mSearchField.gridy = 1;
        checkinPanel.add(mSearchField, gbc_mSearchField);
        mSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent event) {
                changedUpdate(event);
            }

            @Override
            public void insertUpdate(DocumentEvent event) {
                changedUpdate(event);
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                final String query = mSearchField.getText();

                if (!StringUtils.isEmpty(query)) {
                    mSearchTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            final MemberResults results = Member.searchMembers(
                                    query, 5, 2, false);
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    mResultsList.setListData(results
                                            .toArray(new Member[results.size()]));
                                    mResultsList.setEnabled(true);

                                    if (results.hasUniqueMatch()) mResultsList
                                            .setSelectedIndex(0);
                                }
                            });
                        }
                    });
                } else {
                    mResultsList.setEnabled(false);
                    mResultsList.setModel(EMPTY_RESULTS);
                }
            }
        });
        mSearchField.setPlaceholder("type your name to search");
        mSearchField.setFont(new Font("Tahoma", Font.PLAIN, 16));
        mSearchField.setColumns(10);

        mStep2Label = new JLabel("2: Specify your skill level");
        GridBagConstraints gbc_mStep2Label = new GridBagConstraints();
        gbc_mStep2Label.anchor = GridBagConstraints.WEST;
        gbc_mStep2Label.insets = new Insets(5, 5, 5, 0);
        gbc_mStep2Label.gridx = 0;
        gbc_mStep2Label.gridy = 4;
        checkinPanel.add(mStep2Label, gbc_mStep2Label);
        mStep2Label.setForeground(Color.LIGHT_GRAY);
        mStep2Label.setFont(new Font("Tahoma", Font.PLAIN, 16));

        JPanel panel_1 = new JPanel();
        GridBagConstraints gbc_panel_1 = new GridBagConstraints();
        gbc_panel_1.insets = new Insets(5, 5, 5, 5);
        gbc_panel_1.fill = GridBagConstraints.BOTH;
        gbc_panel_1.gridx = 0;
        gbc_panel_1.gridy = 5;
        checkinPanel.add(panel_1, gbc_panel_1);
        panel_1.setOpaque(false);
        panel_1.setLayout(new GridLayout(0, 1, 0, 0));

        mLevel1Radio = new JRadioButton("Graded/advanced");
        mLevel1Radio.addActionListener(mSkillButtonHandler);
        mLevel1Radio.setEnabled(false);
        mLevel1Radio.setOpaque(false);
        panel_1.add(mLevel1Radio);

        mLevel2Radio = new JRadioButton("Intermediate");
        mLevel2Radio.addActionListener(mSkillButtonHandler);
        mLevel2Radio.setEnabled(false);
        mLevel2Radio.setOpaque(false);
        panel_1.add(mLevel2Radio);

        mLevel3Radio = new JRadioButton("Beginner");
        mLevel3Radio.addActionListener(mSkillButtonHandler);
        mLevel3Radio.setEnabled(false);
        mLevel3Radio.setOpaque(false);
        panel_1.add(mLevel3Radio);

        mLevel4Radio = new JRadioButton("Never played");
        mLevel4Radio.addActionListener(mSkillButtonHandler);
        mLevel4Radio.setEnabled(false);
        mLevel4Radio.setOpaque(false);
        panel_1.add(mLevel4Radio);

        mSkillRadioGroup = new ButtonGroup();
        mSkillRadioGroup.add(mLevel1Radio);
        mSkillRadioGroup.add(mLevel2Radio);
        mSkillRadioGroup.add(mLevel3Radio);
        mSkillRadioGroup.add(mLevel4Radio);

        mResultsList = new JList<Member>();
        mResultsList.setPreferredSize(new Dimension(0, 120));
        GridBagConstraints gbc_mResultsList = new GridBagConstraints();
        gbc_mResultsList.insets = new Insets(0, 5, 0, 5);
        gbc_mResultsList.fill = GridBagConstraints.BOTH;
        gbc_mResultsList.gridx = 0;
        gbc_mResultsList.gridy = 2;
        checkinPanel.add(mResultsList, gbc_mResultsList);
        mResultsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                selectMember(mResultsList.getSelectedValue());
            }
        });
        mResultsList.setEnabled(false);
        mResultsList.setModel(EMPTY_RESULTS);
        mResultsList.setFont(new Font("Tahoma", Font.PLAIN, 16));

        JButton registerButton = new JButton("Not found? Click to register");
        GridBagConstraints gbc_registerButton = new GridBagConstraints();
        gbc_registerButton.insets = new Insets(5, 5, 5, 5);
        gbc_registerButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_registerButton.gridx = 0;
        gbc_registerButton.gridy = 3;
        checkinPanel.add(registerButton, gbc_registerButton);
        registerButton.setOpaque(false);
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showRegistration();
            }
        });
        registerButton.setFont(new Font("Tahoma", Font.PLAIN, 14));

        mStep3Label = new JLabel("3: Choose how you want to play tonight");
        GridBagConstraints gbc_mStep3Label = new GridBagConstraints();
        gbc_mStep3Label.anchor = GridBagConstraints.WEST;
        gbc_mStep3Label.insets = new Insets(5, 5, 5, 0);
        gbc_mStep3Label.gridx = 0;
        gbc_mStep3Label.gridy = 6;
        checkinPanel.add(mStep3Label, gbc_mStep3Label);
        mStep3Label.setForeground(Color.LIGHT_GRAY);
        mStep3Label.setFont(new Font("Tahoma", Font.PLAIN, 16));

        mButtonsPanel = new JPanel();
        GridBagConstraints gbc_mButtonsPanel = new GridBagConstraints();
        gbc_mButtonsPanel.insets = new Insets(5, 5, 5, 5);
        gbc_mButtonsPanel.fill = GridBagConstraints.BOTH;
        gbc_mButtonsPanel.gridx = 0;
        gbc_mButtonsPanel.gridy = 7;
        checkinPanel.add(mButtonsPanel, gbc_mButtonsPanel);
        mButtonsPanel.setOpaque(false);
        mButtonsPanel.setLayout(new GridLayout(0, 1, 0, 5));

        JButton playFullButton = new JButton("Participate in the ladder");
        playFullButton.setOpaque(false);
        playFullButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                setMemberStatus(true, true);
            }
        });
        playFullButton.setEnabled(false);
        mButtonsPanel.add(playFullButton);

        playManualButton = new JButton("Just training and/or King of the Court");
        playManualButton.setOpaque(false);
        playManualButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setMemberStatus(false, true);
            }
        });
        playManualButton.setEnabled(false);
        mButtonsPanel.add(playManualButton);

        JButton playNoneButton = new JButton("I'm finished for today");
        playNoneButton.setOpaque(false);
        playNoneButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setMemberStatus(false, false);
            }
        });
        playNoneButton.setEnabled(false);
        mButtonsPanel.add(playNoneButton);

        JPanel panel2 = new JPanel();
        GridBagConstraints gbc_panel2 = new GridBagConstraints();
        gbc_panel2.fill = GridBagConstraints.BOTH;
        gbc_panel2.gridheight = 3;
        gbc_panel2.gridx = 0;
        gbc_panel2.gridy = 1;
        checkinPanel.add(panel2, gbc_panel2);
        panel2.setBackground(Color.WHITE);

        JPanel panel_2 = new JPanel();
        panel_2.setBackground(Color.WHITE);
        GridBagConstraints gbc_panel_2 = new GridBagConstraints();
        gbc_panel_2.fill = GridBagConstraints.BOTH;
        gbc_panel_2.gridx = 0;
        gbc_panel_2.gridy = 5;
        checkinPanel.add(panel_2, gbc_panel_2);

        JPanel panel_3 = new JPanel();
        panel_3.setBackground(Color.WHITE);
        GridBagConstraints gbc_panel_3 = new GridBagConstraints();
        gbc_panel_3.fill = GridBagConstraints.BOTH;
        gbc_panel_3.gridx = 0;
        gbc_panel_3.gridy = 7;
        checkinPanel.add(panel_3, gbc_panel_3);

        JPanel panel_4 = new JPanel();
        panel_4.setBackground(Color.decode("#0065B3"));
        GridBagConstraints gbc_panel_4 = new GridBagConstraints();
        gbc_panel_4.fill = GridBagConstraints.BOTH;
        gbc_panel_4.gridx = 0;
        gbc_panel_4.gridy = 0;
        checkinPanel.add(panel_4, gbc_panel_4);

        JPanel panel_5 = new JPanel();
        panel_5.setBackground(Color.decode("#0065B3"));
        GridBagConstraints gbc_panel_5 = new GridBagConstraints();
        gbc_panel_5.fill = GridBagConstraints.BOTH;
        gbc_panel_5.gridx = 0;
        gbc_panel_5.gridy = 4;
        checkinPanel.add(panel_5, gbc_panel_5);

        JPanel panel_6 = new JPanel();
        panel_6.setBackground(Color.decode("#0065B3"));
        GridBagConstraints gbc_panel_6 = new GridBagConstraints();
        gbc_panel_6.fill = GridBagConstraints.BOTH;
        gbc_panel_6.gridx = 0;
        gbc_panel_6.gridy = 6;
        checkinPanel.add(panel_6, gbc_panel_6);

        GridBagConstraints gbc_mLadderPanel = new GridBagConstraints();
        gbc_mLadderPanel.fill = GridBagConstraints.BOTH;
        gbc_mLadderPanel.gridx = 3;
        gbc_mLadderPanel.gridy = 2;

        mLadderGrid = new JPanel();
        mLadderGrid.setBackground(Color.decode("#0065B3"));
        GridBagConstraints gbc_mLadderGrid = new GridBagConstraints();
        gbc_mLadderGrid.fill = GridBagConstraints.BOTH;
        gbc_mLadderGrid.gridx = 0;
        gbc_mLadderGrid.gridy = 0;
        add(mLadderGrid, gbc_mLadderPanel);
        mLadderGrid.setLayout(new VerticalGridLayout(15, 0, 5, 0));
    }

    private String getSkillLevel() {
        if (mLevel1Radio.isSelected()) return "1";
        else if (mLevel2Radio.isSelected()) return "2";
        else if (mLevel3Radio.isSelected()) return "3";
        else if (mLevel4Radio.isSelected()) return "4";
        else return "";
    }

    public void resetSearch() {
        mSearchField.setText(null);
        mResultsList.setEnabled(false);
        mResultsList.setModel(EMPTY_RESULTS);

        disableSkill();
        disableMode();

        mSearchField.grabFocus();
    }

    private void selectMember(Member member) {
        mSelectedMember = member;

        if (mSelectedMember != null) {
            enableSkill();
        } else {
            disableSkill();
            disableMode();
        }
    }

    private void enableSkill() {
        // Enable UI widgets.
        mStep2Label.setForeground(Color.white);

        mLevel1Radio.setEnabled(true);
        mLevel2Radio.setEnabled(true);
        mLevel3Radio.setEnabled(true);
        mLevel4Radio.setEnabled(true);

        // Find their latest skill level.
        String skillLevel = new MemberStatus(mSelectedMember).getSkillLevel();

        // Preselect most recent skill level.
        if (StringUtils.isEmpty(skillLevel)) {
            mSkillRadioGroup.clearSelection();
            disableMode();
        } else if (StringUtils.equals(skillLevel, "4")) {
            mLevel4Radio.setSelected(true);
            enableMode();
        } else if (StringUtils.equals(skillLevel, "3")) {
            mLevel3Radio.setSelected(true);
            enableMode();
        } else if (StringUtils.equals(skillLevel, "2")) {
            mLevel2Radio.setSelected(true);
            enableMode();
        } else {
            mLevel1Radio.setSelected(true);
            enableMode();
        }
    }

    private void disableSkill() {
        mStep2Label.setForeground(Color.lightGray);

        mLevel1Radio.setEnabled(false);
        mLevel2Radio.setEnabled(false);
        mLevel3Radio.setEnabled(false);
        mLevel4Radio.setEnabled(false);

        mSkillRadioGroup.clearSelection();
    }

    private void enableMode() {
        mStep3Label.setForeground(Color.white);

        for (Component child : mButtonsPanel.getComponents()) {
            child.setEnabled(true);
        }
    }

    private void disableMode() {
        mStep3Label.setForeground(Color.lightGray);

        for (Component child : mButtonsPanel.getComponents()) {
            child.setEnabled(false);
        }
    }

    private void setMemberStatus(final boolean playGames, final boolean present) {
        DB.executeTransaction(new Transaction<Void>() {
            @Override
            public void run() {
                MemberStatus newStatus = new MemberStatus(mSelectedMember,
                        getSkillLevel(), present, playGames);
                update(newStatus);

                MatchResult.addToLadder(mSelectedMember);
            }
        });

        // Update ladder.
        if (mLadderMapping.containsKey(mSelectedMember)) {
            LadderEntry entry = mLadderMapping.get(mSelectedMember);
            entry.setPresent(present);
            entry.flash();
        } else {
            refreshLadder();
        }

        // Clear check-in input widgets.
        resetSearch();
    }

    private void showRegistration() {
        RegisterWindow.showDialog(this, new RegisterWindow.Callback() {
            @Override
            public void memberRegistered(Member member) {
                CheckInPanel.this.memberRegistered(member);
            }
        });
    }

    private void memberRegistered(Member member) {
        mSearchField.setText(member.getName());
    }

    public void refreshLadder() {
        mLadderMapping.clear();

        DB.queueTransaction(new Transaction<Void>() {
            @Override
            public void run() {
                final Date today = Utility.today();
                final Date mustHaveAttendedSince = new Date(System
                        .currentTimeMillis() - 1000l * 60 * 60 * 24 * 7 * 4);

                final Map<Member, MemberStatus> memberStatuses = new HashMap<>();
                for (MemberStatus status : MemberStatus.getLatestStatus()) {
                    memberStatuses.put(status.getMember(), status);
                }

                final Collection<Member> ladder = MatchResult.getLadder();

                // Make a static copy of the ladder widget list.
                final List<LadderEntry> entries = new ArrayList<>();

                mLadderMapping.clear();

                int i = 0;
                for (final Member member : ladder) {
                    // Get status of member.
                    MemberStatus status = memberStatuses.get(member);
                    boolean present = status != null && status.isPresent() &&
                                      status.getDate().compareTo(today) > 0;

                    boolean recent = status != null &&
                                     status.getDate().compareTo(
                                             mustHaveAttendedSince) > 0;
                    if (!recent) continue;

                    // Create UI entry.
                    LadderEntry entry = new LadderEntry(i + 1, member, present);
                    entry.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            mSearchField.setText(member.getNameFormatted());
                            selectMember(member);
                        }
                    });

                    // Add to internal lists.
                    mLadderMapping.put(member, entry);
                    entries.add(entry);

                    i++;
                }

                // Add entries to UI on swing thread.
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        mLadderGrid.removeAll();

                        for (LadderEntry entry : entries) {
                            mLadderGrid.add(entry);
                        }

                        mLadderGrid.revalidate();
                        mLadderGrid.repaint();
                    }
                });
            }
        });
    }
}
