package nz.ac.squash.widget;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;
import nz.ac.squash.db.beans.*;
import nz.ac.squash.db.beans.Member.MemberResults;
import nz.ac.squash.util.LatestExecutor;
import nz.ac.squash.util.SessionHelper;
import nz.ac.squash.util.Utility;
import nz.ac.squash.widget.generic.JTextField;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class MatchPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private JLabel mPlayer1Label;
    private JLabel mPlayer2Label;
    private JLabel mVersusLabel;
    private JPanel mGameInfo;
    private JPanel mSchedulePanel;
    private JLabel mStatusLabel;
    private JPanel mSchedulePanelInner;
    private JPanel mStatusPanel;

    private int mCourt;
    private int mSlot, mOriginalSlot;
    private Match mMatch = null;
    private MatchResult mResult = null;

    private Set<MatchHint> mTempHints = new HashSet<>();
    private boolean mNoMoreMatches = false;

    private final ExecutorService mSearch1Task = new LatestExecutor();
    private final ExecutorService mSearch2Task = new LatestExecutor();

    private Member mPlayer1Hint = null;
    private Member mPlayer2Hint = null;

    private boolean mIsHovering = false;
    private final MouseListener mHoverListener = new MouseAdapter() {
        @Override
        public void mouseExited(MouseEvent e) {
            Point loc = new Point(e.getLocationOnScreen());
            SwingUtilities.convertPointFromScreen(loc, MatchPanel.this);

            if (!new Rectangle(getSize()).contains(loc)) {
                mIsHovering = false;
                resetHints();
                switchPanel(false);
            }
        }

        @Override
        public void mouseEntered(MouseEvent arg0) {
            if (!mIsHovering) {
                mIsHovering = true;
                switchPanel(false);
            }
        }
    };
    private JPanel mReviewPanel;
    private JButton mPlayer1WonButton;
    private JButton mPlayer2WonButton;
    private JButton mDrawButton;
    private JButton mScheduleButton;
    private JButton mCancelButton;
    private JButton mKickPlayer1Button;
    private JButton mKickPlayer2Button;
    private JTextField mPlayer1Field;
    private JTextField mPlayer2Field;

    private final Collection<MatchPanel> mSameCourt;
    private final Collection<MatchPanel> mSameSlot;
    private JLabel mPlayer1Warning;
    private JLabel mPlayer2Warning;

    public MatchPanel(int court, int baseSlot, int slot, Collection<MatchPanel> sameCourt, Collection<MatchPanel> sameSlot) {
        mCourt = court;
        mOriginalSlot = baseSlot;
        mSlot = slot;

        mSameCourt = sameCourt;
        mSameSlot = sameSlot;

        createContents();

        recursivelyAddMouseListener(this, mHoverListener);

        loadMatch();
    }

    private void createContents() {
        setOpaque(false);
        addMouseListener(mHoverListener);

        mGameInfo = new JPanel();
        mGameInfo.setVisible(false);
        setLayout(new CardLayout(0, 0));

        mStatusPanel = new JPanel();
        mStatusPanel.setOpaque(false);
        add(mStatusPanel, "status_panel");
        GridBagLayout gbl_mStatusPanel = new GridBagLayout();
        gbl_mStatusPanel.columnWidths = new int[]{0};
        gbl_mStatusPanel.rowHeights = new int[]{0};
        gbl_mStatusPanel.columnWeights = new double[]{0.0};
        gbl_mStatusPanel.rowWeights = new double[]{0.0};
        mStatusPanel.setLayout(gbl_mStatusPanel);

        mStatusLabel = new JLabel("---");
        mStatusLabel.setForeground(Color.GRAY);
        GridBagConstraints gbc_mStatusLabel = new GridBagConstraints();
        gbc_mStatusLabel.anchor = GridBagConstraints.WEST;
        gbc_mStatusLabel.gridx = 0;
        gbc_mStatusLabel.gridy = 0;
        mStatusPanel.add(mStatusLabel, gbc_mStatusLabel);
        mGameInfo.setOpaque(false);
        add(mGameInfo, "game_info");
        GridBagLayout gbl_mGameInfo = new GridBagLayout();
        gbl_mGameInfo.columnWidths = new int[]{0, 0, 0};
        gbl_mGameInfo.rowHeights = new int[]{0, 0, 0, 0};
        gbl_mGameInfo.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        gbl_mGameInfo.rowWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
        mGameInfo.setLayout(gbl_mGameInfo);

        mPlayer1Label = new JLabel("Player 1 name");
        GridBagConstraints gbc_mPlayer1Label = new GridBagConstraints();
        gbc_mPlayer1Label.anchor = GridBagConstraints.SOUTH;
        gbc_mPlayer1Label.gridx = 0;
        gbc_mPlayer1Label.gridy = 0;
        mGameInfo.add(mPlayer1Label, gbc_mPlayer1Label);
        mPlayer1Label.setFont(new Font("Tahoma", Font.BOLD, 14));

        mPlayer1Warning = new JLabel(" !");
        mPlayer1Warning.setVisible(false);
        mPlayer1Warning.setForeground(Color.RED);
        mPlayer1Warning.setFont(new Font("Tahoma", Font.BOLD, 14));
        GridBagConstraints gbc_mPlayer1Warning = new GridBagConstraints();
        gbc_mPlayer1Warning.anchor = GridBagConstraints.SOUTH;
        gbc_mPlayer1Warning.gridx = 1;
        gbc_mPlayer1Warning.gridy = 0;
        mGameInfo.add(mPlayer1Warning, gbc_mPlayer1Warning);

        mVersusLabel = new JLabel("versus");
        GridBagConstraints gbc_versusLabel = new GridBagConstraints();
        gbc_versusLabel.insets = new Insets(5, 0, 5, 0);
        gbc_versusLabel.gridx = 0;
        gbc_versusLabel.gridy = 1;
        mGameInfo.add(mVersusLabel, gbc_versusLabel);

        mPlayer2Label = new JLabel("Player 2 name");
        GridBagConstraints gbc_mPlayer2Label = new GridBagConstraints();
        gbc_mPlayer2Label.anchor = GridBagConstraints.NORTH;
        gbc_mPlayer2Label.gridx = 0;
        gbc_mPlayer2Label.gridy = 2;
        mGameInfo.add(mPlayer2Label, gbc_mPlayer2Label);
        mPlayer2Label.setFont(new Font("Tahoma", Font.BOLD, 14));

        mPlayer2Warning = new JLabel(" !");
        mPlayer2Warning.setVisible(false);
        mPlayer2Warning.setForeground(Color.RED);
        mPlayer2Warning.setFont(new Font("Tahoma", Font.BOLD, 14));
        GridBagConstraints gbc_mPlayer2Warning = new GridBagConstraints();
        gbc_mPlayer2Warning.anchor = GridBagConstraints.NORTH;
        gbc_mPlayer2Warning.gridx = 1;
        gbc_mPlayer2Warning.gridy = 2;
        mGameInfo.add(mPlayer2Warning, gbc_mPlayer2Warning);

        mSchedulePanel = new JPanel();
        mSchedulePanel.setVisible(false);
        mSchedulePanel.setOpaque(false);
        add(mSchedulePanel, "schedule_panel");
        GridBagLayout gbl_mSchedulePanel = new GridBagLayout();
        gbl_mSchedulePanel.columnWidths = new int[]{0};
        gbl_mSchedulePanel.rowHeights = new int[]{0};
        gbl_mSchedulePanel.columnWeights = new double[]{1.0};
        gbl_mSchedulePanel.rowWeights = new double[]{1.0};
        mSchedulePanel.setLayout(gbl_mSchedulePanel);

        mSchedulePanelInner = new JPanel();
        mSchedulePanelInner.setOpaque(false);
        GridBagConstraints gbc_mSchedulePanelInner = new GridBagConstraints();
        gbc_mSchedulePanelInner.fill = GridBagConstraints.BOTH;
        gbc_mSchedulePanelInner.gridx = 0;
        gbc_mSchedulePanelInner.gridy = 0;
        mSchedulePanel.add(mSchedulePanelInner, gbc_mSchedulePanelInner);
        GridBagLayout gbl_mSchedulePanelInner = new GridBagLayout();
        gbl_mSchedulePanelInner.columnWidths = new int[]{0, 0, 0};
        gbl_mSchedulePanelInner.rowHeights = new int[]{0, 0, 0, 0};
        gbl_mSchedulePanelInner.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        gbl_mSchedulePanelInner.rowWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
        mSchedulePanelInner.setLayout(gbl_mSchedulePanelInner);

        mReviewPanel = new JPanel();
        mReviewPanel.setOpaque(false);
        add(mReviewPanel, "review_panel");
        GridBagLayout gbl_mReviewPanel = new GridBagLayout();
        gbl_mReviewPanel.columnWidths = new int[]{0, 0, 0, 0};
        gbl_mReviewPanel.rowHeights = new int[]{0, 0, 0, 0};
        gbl_mReviewPanel.columnWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_mReviewPanel.rowWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
        mReviewPanel.setLayout(gbl_mReviewPanel);

        mPlayer1WonButton = new JButton("Player 1 won");
        mPlayer1WonButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                indicateWinner(mMatch.getPlayer1());
            }
        });
        mPlayer1WonButton.setOpaque(false);
        GridBagConstraints gbc_mPlayer1WonButton = new GridBagConstraints();
        gbc_mPlayer1WonButton.gridwidth = 3;
        gbc_mPlayer1WonButton.fill = GridBagConstraints.BOTH;
        gbc_mPlayer1WonButton.gridx = 0;
        gbc_mPlayer1WonButton.gridy = 0;
        mReviewPanel.add(mPlayer1WonButton, gbc_mPlayer1WonButton);

        mPlayer2WonButton = new JButton("Player 2 won");
        mPlayer2WonButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                indicateWinner(mMatch.getPlayer2());
            }
        });
        mPlayer2WonButton.setOpaque(false);
        GridBagConstraints gbc_mPlayer2WonButton = new GridBagConstraints();
        gbc_mPlayer2WonButton.gridwidth = 3;
        gbc_mPlayer2WonButton.fill = GridBagConstraints.BOTH;
        gbc_mPlayer2WonButton.gridx = 0;
        gbc_mPlayer2WonButton.gridy = 2;
        mReviewPanel.add(mPlayer2WonButton, gbc_mPlayer2WonButton);

        mDrawButton = new JButton("Unfinished / Draw");
        mDrawButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                indicateWinner(null);
            }
        });
        mDrawButton.setOpaque(false);
        GridBagConstraints gbc_mDrawButton = new GridBagConstraints();
        gbc_mDrawButton.fill = GridBagConstraints.BOTH;
        gbc_mDrawButton.gridx = 1;
        gbc_mDrawButton.gridy = 1;
        mReviewPanel.add(mDrawButton, gbc_mDrawButton);

        mPlayer1Field = new JTextField();
        mPlayer1Field.setPlaceholder("Player 1");
        mPlayer1Field.setColumns(10);
        GridBagConstraints gbc_mPlayer1Field = new GridBagConstraints();
        gbc_mPlayer1Field.anchor = GridBagConstraints.SOUTH;
        gbc_mPlayer1Field.fill = GridBagConstraints.HORIZONTAL;
        gbc_mPlayer1Field.gridx = 0;
        gbc_mPlayer1Field.gridy = 0;
        mSchedulePanelInner.add(mPlayer1Field, gbc_mPlayer1Field);
        mPlayer1Field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                final String query = mPlayer1Field.getText();

                if (!StringUtils.isEmpty(query) && mPlayer1Field.isEditable()) {
                    mSearch1Task.execute(new Runnable() {
                        @Override
                        public void run() {
                            final MemberResults results = Member.searchMembers(query, 1, true);

                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    if (results.hasUniqueMatch() && !results.get(0).equals(mPlayer2Hint)) {
                                        mPlayer1Hint = results.get(0);
                                        mTempHints.add(new TempHintExplicitlyIncludePlayer(mPlayer1Hint));

                                        mPlayer1Field.setText(mPlayer1Hint.getNameFormattedLong());
                                        mPlayer1Field.setEditable(false);
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });

        mKickPlayer1Button = new JButton("\u21ba");
        mKickPlayer1Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                replacePlayer(mMatch.getPlayer1());
            }
        });
        mKickPlayer1Button.setOpaque(false);
        mKickPlayer1Button.setEnabled(false);
        GridBagConstraints gbc_mKickPlayer1Button = new GridBagConstraints();
        gbc_mKickPlayer1Button.anchor = GridBagConstraints.SOUTH;
        gbc_mKickPlayer1Button.gridx = 1;
        gbc_mKickPlayer1Button.gridy = 0;
        mSchedulePanelInner.add(mKickPlayer1Button, gbc_mKickPlayer1Button);

        mScheduleButton = new JButton("Schedule");
        mScheduleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scheduleMatch();
            }
        });
        mScheduleButton.setOpaque(false);
        GridBagConstraints gbc_mScheduleButton = new GridBagConstraints();
        gbc_mScheduleButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_mScheduleButton.anchor = GridBagConstraints.NORTH;
        gbc_mScheduleButton.gridx = 0;
        gbc_mScheduleButton.gridy = 1;
        mSchedulePanelInner.add(mScheduleButton, gbc_mScheduleButton);

        mCancelButton = new JButton("\u2715");
        mCancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelMatch();
            }
        });
        mCancelButton.setOpaque(false);
        mCancelButton.setEnabled(false);
        GridBagConstraints gbc_mCancelButton = new GridBagConstraints();
        gbc_mCancelButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_mCancelButton.gridx = 1;
        gbc_mCancelButton.gridy = 1;
        mSchedulePanelInner.add(mCancelButton, gbc_mCancelButton);

        mPlayer2Field = new JTextField();
        mPlayer2Field.setPlaceholder("Player 2");
        mPlayer2Field.setColumns(10);
        GridBagConstraints gbc_mPlayer2Field = new GridBagConstraints();
        gbc_mPlayer2Field.anchor = GridBagConstraints.NORTH;
        gbc_mPlayer2Field.fill = GridBagConstraints.HORIZONTAL;
        gbc_mPlayer2Field.gridx = 0;
        gbc_mPlayer2Field.gridy = 2;
        mSchedulePanelInner.add(mPlayer2Field, gbc_mPlayer2Field);
        mPlayer2Field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                final String query = mPlayer2Field.getText();

                if (!StringUtils.isEmpty(query) && mPlayer2Field.isEditable()) {
                    mSearch2Task.execute(new Runnable() {
                        @Override
                        public void run() {
                            final MemberResults results = Member.searchMembers(query, 1, true);

                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    if (results.hasUniqueMatch() && !results.get(0).equals(mPlayer1Hint)) {
                                        mPlayer2Hint = results.get(0);
                                        mTempHints.add(new TempHintExplicitlyIncludePlayer(mPlayer2Hint));

                                        mPlayer2Field.setText(mPlayer2Hint.getNameFormattedLong());
                                        mPlayer2Field.setEditable(false);
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });

        mKickPlayer2Button = new JButton("\u21ba");
        mKickPlayer2Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                replacePlayer(mMatch.getPlayer2());
            }
        });
        mKickPlayer2Button.setOpaque(false);
        mKickPlayer2Button.setEnabled(false);
        GridBagConstraints gbc_mKickPlayer2Button = new GridBagConstraints();
        gbc_mKickPlayer2Button.anchor = GridBagConstraints.NORTH;
        gbc_mKickPlayer2Button.gridx = 1;
        gbc_mKickPlayer2Button.gridy = 2;
        mSchedulePanelInner.add(mKickPlayer2Button, gbc_mKickPlayer2Button);
    }

    private void recursivelyAddMouseListener(Container container, MouseListener listener) {
        for (Component child : container.getComponents()) {
            if (child instanceof Container) {
                recursivelyAddMouseListener((Container) child, listener);
            }
            child.addMouseListener(listener);
        }
    }

    public void enableSchedule() {
        // If hovering, show edit controls.
        switchPanel(true);
    }

    public void nextSlot() {
        mMatch = null;
        mResult = null;
        mSlot++;

        loadMatch();
    }

    public void previousSlot() {
        mMatch = null;
        mResult = null;
        mSlot--;

        loadMatch();
    }

    private void loadMatch() {
        DB.executeTransaction(new DB.Transaction<Void>() {
            @Override
            public void run() {
                mMatch = Utility.first(query(Match.class,
                        "m where m.mCourt = ?0 and m.mTimeSlot = ?1 and m.mDate >= ?2 order by m.mDate desc",
                        mCourt, mSlot, Utility.stripTime(new Date())));

                if (mMatch != null) {
                    mResult = Utility.first(query(MatchResult.class, "r where mMatch = ?0", mMatch));
                }
            }
        });

        refreshPanel();
        switchPanel(false);
    }

    public void checkForCollisions() {
        if (mMatch == null) return;

        Member u1 = mMatch.getPlayer1();
        Member u2 = mMatch.getPlayer2();

        int minPlayer1Distance = Integer.MAX_VALUE;
        int minPlayer2Distance = Integer.MAX_VALUE;
        for (MatchPanel otherSlot : mSameSlot) {
            for (MatchPanel otherCourt : otherSlot.mSameCourt) {
                if (otherCourt.mCourt == mCourt) continue;
                if (otherCourt.mMatch == null) continue;
                if (otherCourt.mOriginalSlot < 0 || mOriginalSlot < 0) continue;

                int distance = Math.abs(otherCourt.mOriginalSlot - mOriginalSlot);
                Member t1 = otherCourt.mMatch.getPlayer1();
                Member t2 = otherCourt.mMatch.getPlayer2();

                if (distance < minPlayer1Distance && (u1.equals(t1) || u1.equals(t2))) {
                    minPlayer1Distance = distance;
                }

                if (distance < minPlayer2Distance && (u2.equals(t1) || u2.equals(t2))) {
                    minPlayer2Distance = distance;
                }
            }
        }

        mPlayer1Warning.setForeground(minPlayer1Distance == 0 ? Color.RED : Color.ORANGE);
        mPlayer1Warning.setVisible(minPlayer1Distance <= 1);

        mPlayer2Warning.setForeground(minPlayer2Distance == 0 ? Color.RED : Color.ORANGE);
        mPlayer2Warning.setVisible(minPlayer2Distance <= 1);
    }

    private void refreshPanel() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::refreshPanel);
            return;
        }

        // Update plain panel.
        if (mMatch != null) {
            mPlayer1Label.setText(mMatch.getPlayer1().getNameFormatted());
            mPlayer2Label.setText(mMatch.getPlayer2().getNameFormatted());

            if (mResult != null) {
                boolean player1Won = mResult.getWinner().equals(mMatch.getPlayer1());
                boolean player2Won = mResult.getWinner().equals(mMatch.getPlayer2());

                mPlayer1Label.setForeground(player1Won ? Color.BLACK : Color.GRAY);
                mPlayer2Label.setForeground(player2Won ? Color.BLACK : Color.GRAY);

                if (player1Won) {
                    mVersusLabel.setText("won against");
                } else {
                    mVersusLabel.setText("lost to");
                }
            } else {
                mPlayer1Label.setForeground(Color.BLACK);
                mPlayer2Label.setForeground(Color.BLACK);

                mVersusLabel.setText("versus");
            }
        }

        // Update review panel.
        if (mMatch != null) {
            mPlayer1WonButton.setText(mMatch.getPlayer1().getNameFormatted() + " won");
            mPlayer2WonButton.setText(mMatch.getPlayer2().getNameFormatted() + " won");
        }

        // Update schedule panel.
        mPlayer1Field.setEditable(mMatch == null && !mNoMoreMatches);
        mPlayer2Field.setEditable(mMatch == null && !mNoMoreMatches);

        mScheduleButton.setEnabled(!mNoMoreMatches);
        mScheduleButton.setText(mMatch == null ? "Schedule" : "Reschedule");
        mCancelButton.setEnabled(mMatch != null);

        mKickPlayer1Button.setEnabled(mMatch != null);
        mKickPlayer2Button.setEnabled(mMatch != null);

        if (mMatch != null) {
            mPlayer1Field.setText(mMatch.getPlayer1().getNameFormattedLong());
            mPlayer2Field.setText(mMatch.getPlayer2().getNameFormattedLong());
        } else {
            mPlayer1Field.setText("");
            mPlayer2Field.setText("");
        }
    }

    private void switchPanel(boolean edit) {
        if (mIsHovering && edit) {
            ((CardLayout) getLayout()).show(this, "schedule_panel");
        } else if (mIsHovering && mOriginalSlot <= 0 && mMatch != null) {
            ((CardLayout) getLayout()).show(this, "review_panel");
        } else if (mMatch != null) {
            ((CardLayout) getLayout()).show(this, "game_info");
        } else {
            ((CardLayout) getLayout()).show(this, "status_panel");
        }
    }

    private void scheduleMatch() {
        // Veto the current match if rescheduling.
        if (mMatch != null) {
            mTempHints.add(new TempHintVeto(mMatch.getPlayer1(), mMatch.getPlayer2()));
            cancelMatch();
        }

        // Get a new match.
        if (mPlayer1Hint != null && mPlayer2Hint != null) {
            // Create specific match if both players are forced.
            mMatch = Match.createMatch(mPlayer1Hint, mPlayer2Hint, mCourt, mSlot);
            mNoMoreMatches = true;
        } else {
            final Set<Member> concurrentMembers = new HashSet<>();
            for (MatchPanel panel : mSameSlot) {
                if (panel.mMatch != null) {
                    concurrentMembers.add(panel.mMatch.getPlayer1());
                    concurrentMembers.add(panel.mMatch.getPlayer2());
                }
            }

            // Add anti-collision hints.
            final Set<MatchHint> allHints = new HashSet<>(mTempHints);
            for (Member member : concurrentMembers) {
                allHints.add(new TempHintImplicitlyExcludePlayer(member));
            }

            // Find by internal algorithm.
            mMatch = Match.createMatch(mCourt, mSlot, allHints);
            mNoMoreMatches = mMatch == null;
        }

        // Update UI to show match.
        refreshPanel();

        for (MatchPanel outer : mSameSlot) {
            for (MatchPanel inner : outer.mSameCourt) {
                inner.checkForCollisions();
            }
        }
    }

    private void replacePlayer(final Member memberToReplace) {
        DB.executeTransaction(new DB.Transaction<Void>() {
            @Override
            public void run() {
                // Keep the remaining player for the replacement match.
                final Member remainingMember = memberToReplace.equals(mMatch.getPlayer1())
                        ? mMatch.getPlayer2()
                        : mMatch.getPlayer1();

                mTempHints.add(new TempHintExplicitlyIncludePlayer(remainingMember));
                mTempHints.add(new TempHintImplicitlyExcludePlayer(memberToReplace));

                // Find a new match.
                scheduleMatch();
            }
        });
    }

    private void kickPlayer(final Member memberToKick) {
        DB.executeTransaction(new DB.Transaction<Void>() {
            @Override
            public void run() {
                // Indicate player has left.
                MemberStatus kickedStatus = new MemberStatus(memberToKick);
                kickedStatus.setPresent(false);
                update(kickedStatus);

                // Keep the remaining player for the replacement match.
                final Member remainingMember = memberToKick.equals(mMatch.getPlayer1())
                        ? mMatch.getPlayer2()
                        : mMatch.getPlayer1();

                mTempHints.add(new TempHintExplicitlyIncludePlayer(remainingMember));

                // Find a new match.
                scheduleMatch();
            }
        });
    }

    private void cancelMatch() {
        if (mMatch == null) return;

        mMatch.cancel();
        mMatch = null;
        mResult = null;

        refreshPanel();

        for (MatchPanel outer : mSameSlot) {
            for (MatchPanel inner : outer.mSameCourt) {
                inner.checkForCollisions();
            }
        }
    }

    private void resetHints() {
        mTempHints.clear();
        mNoMoreMatches = false;

        mPlayer1Hint = null;
        mPlayer2Hint = null;

        mPlayer1Field.setText(mMatch != null ? mMatch.getPlayer1().getNameFormattedLong() : "");
        mPlayer2Field.setText(mMatch != null ? mMatch.getPlayer2().getNameFormattedLong() : "");

        mPlayer1Field.setEditable(mMatch == null);
        mPlayer2Field.setEditable(mMatch == null);
        mScheduleButton.setEnabled(true);
    }

    private void indicateWinner(final Member winner) {
        DB.executeTransaction(new Transaction<Void>() {
            @Override
            public void run() {
                // Make sure result is not already set.
                if (mResult != null) {
                    delete(mResult);
                    mResult = null;
                }

                // Persist the new result.
                if (winner != null) {
                    mResult = new MatchResult(winner, mMatch);
                    update(mResult);
                }
            }
        });

        SessionHelper.current().invalidateLadder();
        refreshPanel();

        ((CardLayout) getLayout()).show(this, "game_info");
    }
}
