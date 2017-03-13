package nz.ac.squash.windows;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;
import nz.ac.squash.db.beans.MatchHintRequest;
import nz.ac.squash.db.beans.Member;
import nz.ac.squash.db.beans.Member.MemberResults;
import nz.ac.squash.db.beans.MemberStatus;
import nz.ac.squash.util.LatestExecutor;
import nz.ac.squash.util.SwingUtils;
import nz.ac.squash.util.Utility;
import nz.ac.squash.widget.generic.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ChallengeWindow extends JDialog {
    private static final long serialVersionUID = 1L;

    private static final Logger sLogger = Logger
            .getLogger(ChallengeWindow.class);

    public static ChallengeWindow showDialog(Component parent) {
        final JFrame frame = parent instanceof JFrame ? (JFrame) parent
                : (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class,
                        parent);

        ChallengeWindow window = new ChallengeWindow(frame.getOwner());

        frame.getGlassPane().setVisible(true);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                frame.getGlassPane().setVisible(false);
            }
        });

        window.setVisible(true);
        return window;
    }

    private static final ListModel<Member> EMPTY_RESULTS = new DefaultListModel<Member>();

    private ExecutorService mSearchTaskA = new LatestExecutor();
    private ExecutorService mSearchTaskB = new LatestExecutor();
    private JList<Member> mPlayer1List;
    private JList<Member> mPlayer2List;
    private JTextField mPlayer1Field;
    private JTextField mPlayer2Field;
    private JButton mChallengeButton;

    private Member mPlayer1 = null;
    private Member mPlayer2 = null;
    private JScrollPane scrollPane;
    private JScrollPane scrollPane_1;

    private ChallengeWindow(Window parent) {
        super(parent, ModalityType.APPLICATION_MODAL);
        createContents();

        pack();
        setLocationRelativeTo(null);

        getRootPane().setDefaultButton(mChallengeButton);
        SwingUtils.closeOnEscape(this);
    }

    @Override
    public void dispose() {
        super.dispose();

        mSearchTaskA.shutdownNow();
        mSearchTaskB.shutdownNow();
    }

    private void createContents() {

        setIconImage(Toolkit
                .getDefaultToolkit()
                .getImage(
                        ChallengeWindow.class
                                .getResource("/javax/swing/plaf/metal/icons/ocean/menu.gif")));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] { 200, 0, 200, 0 };
        gridBagLayout.rowHeights = new int[] { 0, 0, 128, 0, 0 };
        gridBagLayout.columnWeights = new double[] { 1.0, 1.0, 1.0,
                Double.MIN_VALUE };
        gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 1.0, 1.0,
                Double.MIN_VALUE };
        getContentPane().setLayout(gridBagLayout);

        JLabel lblCustomMatch = new JLabel("Request match");
        lblCustomMatch.setForeground(Color.WHITE);
        lblCustomMatch.setFont(new Font("Tahoma", Font.PLAIN, 32));
        GridBagConstraints gbc_lblCustomMatch = new GridBagConstraints();
        gbc_lblCustomMatch.ipady = 10;
        gbc_lblCustomMatch.gridwidth = 3;
        gbc_lblCustomMatch.gridx = 0;
        gbc_lblCustomMatch.gridy = 0;
        getContentPane().add(lblCustomMatch, gbc_lblCustomMatch);

        mPlayer1Field = new JTextField();
        mPlayer1Field.getDocument().addDocumentListener(new DocumentListener() {
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
                final String query = mPlayer1Field.getText();

                if (!StringUtils.isEmpty(query)) {
                    mSearchTaskA.execute(new Runnable() {
                        @Override
                        public void run() {
                            final MemberResults results = Member.searchMembers(
                                    query, 5, true);

                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    mPlayer1List.setListData(results
                                            .toArray(new Member[results.size()]));

                                    if (results.hasUniqueMatch()) mPlayer1List
                                            .setSelectedIndex(0);
                                }
                            });
                        }
                    });
                } else if (mPlayer2 != null) {
                    mPlayer1List.setModel(getPeers(mPlayer2));
                } else {
                    mPlayer1List.setModel(getPeers(null));
                }
            }
        });
        mPlayer1Field.setPlaceholder("your name");
        mPlayer1Field.setFont(new Font("Tahoma", Font.PLAIN, 16));
        mPlayer1Field.setColumns(10);
        GridBagConstraints gbc_mPlayer1Field = new GridBagConstraints();
        gbc_mPlayer1Field.insets = new Insets(5, 5, 5, 0);
        gbc_mPlayer1Field.fill = GridBagConstraints.HORIZONTAL;
        gbc_mPlayer1Field.gridx = 0;
        gbc_mPlayer1Field.gridy = 1;
        getContentPane().add(mPlayer1Field, gbc_mPlayer1Field);

        JPanel panel = new JPanel();
        panel.setBackground(Color.decode("#535353"));
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.gridwidth = 3;
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.gridx = 0;
        gbc_panel.gridy = 0;
        getContentPane().add(panel, gbc_panel);

        mPlayer2Field = new JTextField();
        mPlayer2Field.getDocument().addDocumentListener(new DocumentListener() {
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
                final String query = mPlayer2Field.getText();

                if (!StringUtils.isEmpty(query)) {
                    mSearchTaskB.execute(new Runnable() {
                        @Override
                        public void run() {
                            final MemberResults results = Member.searchMembers(
                                    query, 5, true);

                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    mPlayer2List.setListData(results
                                            .toArray(new Member[results.size()]));

                                    if (results.hasUniqueMatch()) mPlayer2List
                                            .setSelectedIndex(0);
                                }
                            });
                        }
                    });
                } else if (mPlayer1 != null) {
                    mPlayer2List.setModel(getPeers(mPlayer1));
                } else {
                    mPlayer2List.setModel(getPeers(null));
                }
            }
        });
        mPlayer2Field.setPlaceholder("opponent's name");
        mPlayer2Field.setFont(new Font("Tahoma", Font.PLAIN, 16));
        mPlayer2Field.setColumns(10);
        GridBagConstraints gbc_mPlayer2Field = new GridBagConstraints();
        gbc_mPlayer2Field.insets = new Insets(5, 0, 5, 5);
        gbc_mPlayer2Field.fill = GridBagConstraints.HORIZONTAL;
        gbc_mPlayer2Field.gridx = 2;
        gbc_mPlayer2Field.gridy = 1;
        getContentPane().add(mPlayer2Field, gbc_mPlayer2Field);

        scrollPane = new JScrollPane();
        scrollPane.setOpaque(false);
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.insets = new Insets(0, 5, 5, 0);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 2;
        getContentPane().add(scrollPane, gbc_scrollPane);

        mPlayer1List = new JList<Member>();
        scrollPane.setViewportView(mPlayer1List);
        mPlayer1List.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent arg0) {
                mPlayer1 = (Member) mPlayer1List.getSelectedValue();
                if (StringUtils.isEmpty(mPlayer2Field.getText())) {
                    mPlayer2List.setModel(getPeers(mPlayer1));
                }
                playerSelected();
            }
        });
        mPlayer1List.setFont(new Font("Tahoma", Font.PLAIN, 16));

        JLabel lblNewLabel = new JLabel("versus");
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.insets = new Insets(0, 10, 0, 10);
        gbc_lblNewLabel.gridx = 1;
        gbc_lblNewLabel.gridy = 2;
        getContentPane().add(lblNewLabel, gbc_lblNewLabel);

        scrollPane_1 = new JScrollPane();
        scrollPane_1.setOpaque(false);
        GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
        gbc_scrollPane_1.insets = new Insets(0, 0, 5, 5);
        gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
        gbc_scrollPane_1.gridx = 2;
        gbc_scrollPane_1.gridy = 2;
        getContentPane().add(scrollPane_1, gbc_scrollPane_1);

        mPlayer2List = new JList<Member>();
        scrollPane_1.setViewportView(mPlayer2List);
        mPlayer2List.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent arg0) {
                mPlayer2 = (Member) mPlayer2List.getSelectedValue();
                if (StringUtils.isEmpty(mPlayer1Field.getText())) {
                    mPlayer1List.setModel(getPeers(mPlayer2));
                }
                playerSelected();
            }
        });
        mPlayer2List.setFont(new Font("Tahoma", Font.PLAIN, 16));

        JPanel panel_1 = new JPanel();
        FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
        flowLayout.setAlignment(FlowLayout.RIGHT);
        GridBagConstraints gbc_panel_1 = new GridBagConstraints();
        gbc_panel_1.gridwidth = 3;
        gbc_panel_1.fill = GridBagConstraints.BOTH;
        gbc_panel_1.gridx = 0;
        gbc_panel_1.gridy = 3;
        getContentPane().add(panel_1, gbc_panel_1);

        mChallengeButton = new JButton("Challenge");
        mChallengeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                makeChallenge();
            }
        });
        mChallengeButton.setEnabled(false);
        mChallengeButton.setOpaque(false);
        panel_1.add(mChallengeButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        cancelButton.setOpaque(false);
        panel_1.add(cancelButton);
    }

    private ListModel<Member> getPeers(final Member member) {
        if (member == null) return EMPTY_RESULTS;

        final DefaultListModel<Member> newList = new DefaultListModel<Member>();

        DB.queueTransaction(new Transaction<Void>() {
            @Override
            public void run() {
                MemberStatus latestStatus = new MemberStatus(member);

                final List<MemberStatus> players = typedQuery(
                        MemberStatus.class,
                        "select s from " +
                                MemberStatus.class.getName() +
                                " as s where s.mDate >= ?0 and s.mMember != ?1 and s.mSkillLevel = ?2 and s.mPresent = true and s.mDate = (select max(mDate) from " +
                                MemberStatus.class.getName() +
                                " as m where s.mMember = m.mMember)",
                        Utility.stripTime(new Date()), member,
                        latestStatus.getSkillLevel());

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        for (MemberStatus status : players) {
                            newList.addElement(status.getMember());
                        }
                    }
                });
            }
        });

        return newList;
    }

    private void playerSelected() {
        mChallengeButton.setEnabled(mPlayer1 != null && mPlayer2 != null &&
                                    !mPlayer1.equals(mPlayer2));
    }

    private void makeChallenge() {
        DB.executeTransaction(new DB.Transaction<Void>() {
            @Override
            public void run() {
                // Ignore challenge if identical one is already pending.
                boolean nonePending = query(
                        MatchHintRequest.class,
                        "h where h.mDate >= ?0 and h.mSatisfiedBy = null and ((h.mPlayer1 = ?1 and h.mPlayer2 = ?2) or (h.mPlayer1 = ?2 and h.mPlayer2 = ?1))",
                        Utility.today(), mPlayer1, mPlayer2).isEmpty();

                // Make the request.
                if (nonePending) {
                    MatchHintRequest.createRequest(mPlayer1, mPlayer2);
                } else {
                    sLogger.info("Ignoring request between " +
                                 mPlayer1.getNameFormatted() + " and " +
                                 mPlayer2.getNameFormatted() +
                                 " because a similar request is already pending");
                }
            }
        });

        dispose();
    }
}
