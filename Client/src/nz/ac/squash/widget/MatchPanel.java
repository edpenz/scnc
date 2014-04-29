package nz.ac.squash.widget;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;
import nz.ac.squash.db.beans.Match;
import nz.ac.squash.db.beans.MatchHint;
import nz.ac.squash.db.beans.MatchHintTempVeto;
import nz.ac.squash.db.beans.MatchResult;
import nz.ac.squash.db.beans.Member;
import nz.ac.squash.db.beans.Member.MemberResults;
import nz.ac.squash.db.beans.MemberStatus;
import nz.ac.squash.util.LatestExecutor;
import nz.ac.squash.util.Utility;

import org.apache.commons.lang3.StringUtils;

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
	private JButton mScheduleButton;
	private JButton mKickPlayer1Button;
	private JButton mKickPlayer2Button;

	private final boolean mPastSlot;

	private int mCourt;
	private int mSlot;
	private Match mMatch = null;
	private MatchResult mResult = null;

	private Set<MatchHint> mTempVetoed = new HashSet<>();

	private final ExecutorService mSearch1Task = new LatestExecutor();
	private final ExecutorService mSearch2Task = new LatestExecutor();

	private Member mPlayer1Hint = null;
	private Member mPlayer2Hint = null;

	private MouseListener mHoverListener = new MouseAdapter() {
		private boolean mIsHovering = false;

		@Override
		public void mouseExited(MouseEvent e) {
			Point loc = new Point(e.getLocationOnScreen());
			SwingUtilities.convertPointFromScreen(loc, MatchPanel.this);

			if (!new Rectangle(getSize()).contains(loc)) {
				mIsHovering = false;
				onHoverEnd();
			}
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			if (!mIsHovering) {
				mIsHovering = true;
				onHover();
			}
		}
	};
	private JPanel panel_1;
	private JLabel mDescLabel;
	private JButton mCancelButton;
	private JPanel mReviewPanel;
	private JButton mPlayer1WonButton;
	private JButton mPlayer2WonButton;
	private JButton mDrawButton;
	private JPanel mHintKickPanel;
	private JPanel mKickPanel;
	private JPanel mHintPanel;
	private JTextField mPlayer1Field;
	private JTextField mPlayer2Field;

	public MatchPanel(int court, int slot) {
		mCourt = court;
		mSlot = slot;
		mPastSlot = slot < 0;

		createContents();

		recursivelyAddMouseListener(mSchedulePanelInner, mHoverListener);
		recursivelyAddMouseListener(mReviewPanel, mHoverListener);

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
		gbl_mStatusPanel.columnWidths = new int[] { 0 };
		gbl_mStatusPanel.rowHeights = new int[] { 0 };
		gbl_mStatusPanel.columnWeights = new double[] { 0.0 };
		gbl_mStatusPanel.rowWeights = new double[] { 0.0 };
		mStatusPanel.setLayout(gbl_mStatusPanel);

		mStatusLabel = new JLabel("Not in use");
		mStatusLabel.setForeground(Color.GRAY);
		GridBagConstraints gbc_mStatusLabel = new GridBagConstraints();
		gbc_mStatusLabel.anchor = GridBagConstraints.WEST;
		gbc_mStatusLabel.gridx = 0;
		gbc_mStatusLabel.gridy = 0;
		mStatusPanel.add(mStatusLabel, gbc_mStatusLabel);
		mGameInfo.setOpaque(false);
		add(mGameInfo, "game_info");
		GridBagLayout gbl_mGameInfo = new GridBagLayout();
		gbl_mGameInfo.columnWidths = new int[] { 0, 0 };
		gbl_mGameInfo.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_mGameInfo.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_mGameInfo.rowWeights = new double[] { 1.0, 0.0, 1.0,
				Double.MIN_VALUE };
		mGameInfo.setLayout(gbl_mGameInfo);

		mPlayer1Label = new JLabel("Player 1 name");
		GridBagConstraints gbc_mPlayer1Label = new GridBagConstraints();
		gbc_mPlayer1Label.anchor = GridBagConstraints.SOUTH;
		gbc_mPlayer1Label.gridx = 0;
		gbc_mPlayer1Label.gridy = 0;
		mGameInfo.add(mPlayer1Label, gbc_mPlayer1Label);
		mPlayer1Label.setFont(new Font("Tahoma", Font.BOLD, 14));

		mVersusLabel = new JLabel("versus");
		GridBagConstraints gbc_versusLabel = new GridBagConstraints();
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

		mSchedulePanel = new JPanel();
		mSchedulePanel.setVisible(false);
		mSchedulePanel.setOpaque(false);
		add(mSchedulePanel, "schedule_panel");
		GridBagLayout gbl_mSchedulePanel = new GridBagLayout();
		gbl_mSchedulePanel.columnWidths = new int[] { 0 };
		gbl_mSchedulePanel.rowHeights = new int[] { 0 };
		gbl_mSchedulePanel.columnWeights = new double[] { 1.0 };
		gbl_mSchedulePanel.rowWeights = new double[] { 1.0 };
		mSchedulePanel.setLayout(gbl_mSchedulePanel);

		mSchedulePanelInner = new JPanel();
		mSchedulePanelInner.setOpaque(false);
		GridBagConstraints gbc_mSchedulePanelInner = new GridBagConstraints();
		gbc_mSchedulePanelInner.fill = GridBagConstraints.BOTH;
		gbc_mSchedulePanelInner.gridx = 0;
		gbc_mSchedulePanelInner.gridy = 0;
		mSchedulePanel.add(mSchedulePanelInner, gbc_mSchedulePanelInner);
		GridBagLayout gbl_mSchedulePanelInner = new GridBagLayout();
		gbl_mSchedulePanelInner.columnWidths = new int[] { 0, 0 };
		gbl_mSchedulePanelInner.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_mSchedulePanelInner.columnWeights = new double[] { 1.0,
				Double.MIN_VALUE };
		gbl_mSchedulePanelInner.rowWeights = new double[] { 1.0, 0.0, 2.0,
				Double.MIN_VALUE };
		mSchedulePanelInner.setLayout(gbl_mSchedulePanelInner);

		panel_1 = new JPanel();
		panel_1.setOpaque(false);
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 0;
		mSchedulePanelInner.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		mScheduleButton = new JButton("Schedule");
		GridBagConstraints gbc_mScheduleButton = new GridBagConstraints();
		gbc_mScheduleButton.fill = GridBagConstraints.BOTH;
		gbc_mScheduleButton.gridx = 0;
		gbc_mScheduleButton.gridy = 0;
		panel_1.add(mScheduleButton, gbc_mScheduleButton);
		mScheduleButton.setEnabled(false);
		mScheduleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				scheduleMatch();
			}
		});
		mScheduleButton.setOpaque(false);

		mCancelButton = new JButton("X");
		mCancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelMatch();
			}
		});
		mCancelButton.setOpaque(false);
		mCancelButton.setEnabled(false);
		GridBagConstraints gbc_mCancelButton = new GridBagConstraints();
		gbc_mCancelButton.fill = GridBagConstraints.VERTICAL;
		gbc_mCancelButton.gridx = 1;
		gbc_mCancelButton.gridy = 0;
		panel_1.add(mCancelButton, gbc_mCancelButton);

		mDescLabel = new JLabel(" ");
		mDescLabel.setOpaque(false);
		mDescLabel.setEnabled(false);
		GridBagConstraints gbc_mDescLabel = new GridBagConstraints();
		gbc_mDescLabel.insets = new Insets(0, 0, 5, 0);
		gbc_mDescLabel.gridx = 0;
		gbc_mDescLabel.gridy = 1;
		mSchedulePanelInner.add(mDescLabel, gbc_mDescLabel);

		mReviewPanel = new JPanel();
		mReviewPanel.setOpaque(false);
		add(mReviewPanel, "review_panel");
		mReviewPanel.setLayout(new GridLayout(0, 1, 0, 0));

		mPlayer1WonButton = new JButton("Player 1 won");
		mPlayer1WonButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				indicateWinner(mMatch.getPlayer1());
			}
		});
		mPlayer1WonButton.setOpaque(false);
		mReviewPanel.add(mPlayer1WonButton);

		mPlayer2WonButton = new JButton("Player 2 won");
		mPlayer2WonButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				indicateWinner(mMatch.getPlayer2());
			}
		});

		mDrawButton = new JButton("Clear");
		mDrawButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				indicateWinner(null);
			}
		});
		mDrawButton.setOpaque(false);
		mReviewPanel.add(mDrawButton);
		mPlayer2WonButton.setOpaque(false);
		mReviewPanel.add(mPlayer2WonButton);

		mHintKickPanel = new JPanel();
		mHintKickPanel.setOpaque(false);
		GridBagConstraints gbc_mHintKickPanel = new GridBagConstraints();
		gbc_mHintKickPanel.fill = GridBagConstraints.BOTH;
		gbc_mHintKickPanel.gridx = 0;
		gbc_mHintKickPanel.gridy = 2;
		mSchedulePanelInner.add(mHintKickPanel, gbc_mHintKickPanel);
		mHintKickPanel.setLayout(new CardLayout(0, 0));

		mKickPanel = new JPanel();
		mKickPanel.setOpaque(false);
		mHintKickPanel.add(mKickPanel, "kick_panel");
		mKickPanel.setLayout(new GridLayout(2, 2, 0, 0));

		mKickPlayer1Button = new JButton("Kick player 1");
		mKickPanel.add(mKickPlayer1Button);
		mKickPlayer1Button.setEnabled(false);
		mKickPlayer1Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				kickPlayer(mMatch.getPlayer1());
			}
		});
		mKickPlayer1Button.setOpaque(false);

		mKickPlayer2Button = new JButton("Kick player 2");
		mKickPanel.add(mKickPlayer2Button);
		mKickPlayer2Button.setEnabled(false);
		mKickPlayer2Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				kickPlayer(mMatch.getPlayer2());
			}
		});
		mKickPlayer2Button.setOpaque(false);

		mHintPanel = new JPanel();
		mHintPanel.setOpaque(false);
		mHintKickPanel.add(mHintPanel, "hint_panel");
		mHintPanel.setLayout(new GridLayout(2, 0, 0, 0));

		mPlayer1Field = new JTextField();
		mHintPanel.add(mPlayer1Field);
		mPlayer1Field.setColumns(10);
		
		mPlayer2Field = new JTextField();
		mHintPanel.add(mPlayer2Field);
		mPlayer2Field.setColumns(10);
	}

	private void recursivelyAddMouseListener(Container container,
			MouseListener listener) {
		for (Component child : container.getComponents()) {
			if (child instanceof Container) {
				recursivelyAddMouseListener((Container) child, listener);
			}
			child.addMouseListener(mHoverListener);
		}
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
		DB.executeTransaction(new Transaction() {
			@Override
			public void run() {
				mMatch = Utility
						.first(query(
								Match.class,
								"m where m.mCourt = ?0 and m.mTimeSlot = ?1 and m.mDate >= ?2 order by m.mDate desc",
								mCourt, mSlot, Utility.stripTime(new Date())));

				if (mMatch != null) {
					mResult = Utility.first(query(MatchResult.class,
							"r where mMatch = ?0", mMatch));
				}
			}
		});

		refreshPanel();
	}

	private void refreshPanel() {
		if (mMatch != null) {
			mPlayer1Label.setText(mMatch.getPlayer1().getNameFormatted());
			mPlayer2Label.setText(mMatch.getPlayer2().getNameFormatted());

			// TODO Show skill level.
			mVersusLabel.setText("versus");

			mScheduleButton.setText("Reschedule");

			mKickPlayer1Button.setText("Kick "
					+ mMatch.getPlayer1().getNameFormatted());
			mKickPlayer2Button.setText("Kick "
					+ mMatch.getPlayer2().getNameFormatted());

			mPlayer1WonButton.setText(mMatch.getPlayer1().getNameFormatted()
					+ " won");
			mPlayer2WonButton.setText(mMatch.getPlayer2().getNameFormatted()
					+ " won");

			//((CardLayout) mHintKickPanel.getLayout()).show(mHintKickPanel, "kick_panel");
		} else {
			mScheduleButton.setText("Schedule");

			mKickPlayer1Button.setText("Kick player 1");
			mKickPlayer2Button.setText("Kick player 2");

			mPlayer1WonButton.setText("Player 1 won");
			mPlayer2WonButton.setText("Player 2 won");

			//((CardLayout) mHintKickPanel.getLayout()).show(mHintKickPanel, "hint_panel");
		}

		if (mResult != null) {
			mPlayer1Label.setForeground(mMatch.getPlayer1().equals(
					mResult.getWinner()) ? Color.BLACK : Color.GRAY);
			mPlayer2Label.setForeground(mMatch.getPlayer2().equals(
					mResult.getWinner()) ? Color.BLACK : Color.GRAY);
		} else {
			mPlayer1Label.setForeground(Color.BLACK);
			mPlayer2Label.setForeground(Color.BLACK);
		}

		mScheduleButton.setEnabled(true);
		mCancelButton.setEnabled(mMatch != null);

		mKickPlayer1Button.setEnabled(mMatch != null);
		mKickPlayer2Button.setEnabled(mMatch != null);

		if (!mSchedulePanel.isVisible() && !mReviewPanel.isVisible()) {
			if (mMatch != null) {
				((CardLayout) getLayout()).show(this, "game_info");
			} else {
				((CardLayout) getLayout()).show(this, "status_panel");
			}
		}
	}

	private void onHover() {
		if (!mPastSlot) {
			((CardLayout) getLayout()).show(this, "schedule_panel");
		} else if (mMatch != null) {
			((CardLayout) getLayout()).show(this, "review_panel");
		}
	}

	private void onHoverEnd() {
		if (mMatch != null) {
			((CardLayout) getLayout()).show(this, "game_info");
		} else {
			((CardLayout) getLayout()).show(this, "status_panel");
		}

		mTempVetoed.clear();
	}

	private void scheduleMatch() {
		if (mMatch != null) {
			mTempVetoed.add(new MatchHintTempVeto(mMatch.getPlayer1(), mMatch
					.getPlayer2()));
			cancelMatch();
		}

		mMatch = Match.createMatch(mCourt, mSlot, mTempVetoed);
		refreshPanel();
	}

	private void kickPlayer(final Member memberToKick) {
		DB.executeTransaction(new DB.Transaction() {
			@Override
			public void run() {
				// Indicate player has left.
				MemberStatus kickedStatus = new MemberStatus(memberToKick);
				kickedStatus.setPresent(false);
				update(kickedStatus);

				// Get the other player in the match.
				final Member remainingMember;
				if (memberToKick == mMatch.getPlayer1())
					remainingMember = mMatch.getPlayer2();
				else
					remainingMember = mMatch.getPlayer1();

				// Cancel the old match.
				cancelMatch();

				// Find a new match.
				mMatch = Match
						.createMatch(remainingMember, mCourt, mSlot, null);
			}
		});

		refreshPanel();
	}

	private void cancelMatch() {
		if (mMatch == null)
			return;

		mMatch.cancel();
		mMatch = null;
		mResult = null;

		refreshPanel();
	}

	private void indicateWinner(final Member winner) {
		DB.executeTransaction(new Transaction() {
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

		refreshPanel();
	}
}
