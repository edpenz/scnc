package panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutorService;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
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
import nz.ac.squash.db.beans.Member;
import nz.ac.squash.db.beans.Member.MemberResults;
import nz.ac.squash.db.beans.MemberStatus;
import nz.ac.squash.util.LatestExecutor;
import nz.ac.squash.widget.JTextFieldPlus;
import nz.ac.squash.windows.RegisterWindow;

import org.apache.commons.lang3.StringUtils;
import java.awt.Dimension;

public class CheckInPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private JTextFieldPlus mSearchField;
	private JList<Member> mResultsList;
	private JPanel mButtonsPanel;
	private JLabel mStep3Label;
	private JLabel mStep2Label;
	private JRadioButton mLevel1Radio;
	private JRadioButton mLevel2Radio;
	private JRadioButton mLevel3Radio;
	private JRadioButton mLevel4Radio;
	private ButtonGroup mSkillRadioGroup;

	private static final ListModel<Member> EMPTY_RESULTS = new DefaultListModel<Member>();

	private ExecutorService mSearchTask = new LatestExecutor();
	private ActionListener mSkillButtonHandler = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			enableMode();
		}
	};

	private Member mSelectedMember = null;
	private JButton btnManuallyPickYour;
	private JPanel panel;
	private JPanel panel_2;
	private JPanel panel_3;
	private JPanel panel_4;
	private JPanel panel_5;
	private JPanel panel_6;

	public CheckInPanel() {
		createContents();
	}

	private void createContents() {
		setOpaque(false);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0 };
		setLayout(gridBagLayout);

		panel = new JPanel();
		panel.setOpaque(false);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 50, 0, 0);
		gbc_panel.anchor = GridBagConstraints.WEST;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] {0};
		gbl_panel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[] { 1.0 };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0,
				0.0, 0.0, 0.0 };
		panel.setLayout(gbl_panel);

		JLabel lblNewLabel = new JLabel("Check-in to club night");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		lblNewLabel.setForeground(Color.WHITE);
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 32));

		JLabel lblSelectYour = new JLabel("1: Select your name below");
		GridBagConstraints gbc_lblSelectYour = new GridBagConstraints();
		gbc_lblSelectYour.insets = new Insets(5, 5, 5, 0);
		gbc_lblSelectYour.anchor = GridBagConstraints.WEST;
		gbc_lblSelectYour.gridx = 0;
		gbc_lblSelectYour.gridy = 1;
		panel.add(lblSelectYour, gbc_lblSelectYour);
		lblSelectYour.setForeground(Color.WHITE);
		lblSelectYour.setFont(new Font("Tahoma", Font.PLAIN, 16));

		mSearchField = new JTextFieldPlus();
		GridBagConstraints gbc_mSearchField = new GridBagConstraints();
		gbc_mSearchField.insets = new Insets(5, 5, 0, 5);
		gbc_mSearchField.anchor = GridBagConstraints.NORTH;
		gbc_mSearchField.fill = GridBagConstraints.HORIZONTAL;
		gbc_mSearchField.gridx = 0;
		gbc_mSearchField.gridy = 2;
		panel.add(mSearchField, gbc_mSearchField);
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
									query, 5, Integer.MAX_VALUE, false);
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									mResultsList.setListData(results
											.toArray(new Member[results.size()]));
									mResultsList.setEnabled(true);

									if (results.hasUniqueMatch())
										mResultsList.setSelectedIndex(0);
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
		gbc_mStep2Label.gridy = 5;
		panel.add(mStep2Label, gbc_mStep2Label);
		mStep2Label.setForeground(Color.LIGHT_GRAY);
		mStep2Label.setFont(new Font("Tahoma", Font.PLAIN, 16));

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(5, 5, 5, 5);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 6;
		panel.add(panel_1, gbc_panel_1);
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
		gbc_mResultsList.gridy = 3;
		panel.add(mResultsList, gbc_mResultsList);
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
		gbc_registerButton.gridy = 4;
		panel.add(registerButton, gbc_registerButton);
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
		gbc_mStep3Label.gridy = 7;
		panel.add(mStep3Label, gbc_mStep3Label);
		mStep3Label.setForeground(Color.LIGHT_GRAY);
		mStep3Label.setFont(new Font("Tahoma", Font.PLAIN, 16));

		mButtonsPanel = new JPanel();
		mButtonsPanel.setPreferredSize(new Dimension(0, 200));
		GridBagConstraints gbc_mButtonsPanel = new GridBagConstraints();
		gbc_mButtonsPanel.insets = new Insets(5, 5, 5, 5);
		gbc_mButtonsPanel.fill = GridBagConstraints.BOTH;
		gbc_mButtonsPanel.gridx = 0;
		gbc_mButtonsPanel.gridy = 8;
		panel.add(mButtonsPanel, gbc_mButtonsPanel);
		mButtonsPanel.setOpaque(false);
		mButtonsPanel.setLayout(new GridLayout(0, 1, 0, 5));

		JButton btnNewButton_1 = new JButton(
				"Get matched against similar members");
		btnNewButton_1.setOpaque(false);
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setMemberStatus(true, true);
			}
		});
		btnNewButton_1.setEnabled(false);
		mButtonsPanel.add(btnNewButton_1);

		btnManuallyPickYour = new JButton(
				"Just training and/or King of the Court");
		btnManuallyPickYour.setOpaque(false);
		btnManuallyPickYour.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setMemberStatus(false, true);
			}
		});
		btnManuallyPickYour.setEnabled(false);
		mButtonsPanel.add(btnManuallyPickYour);

		JButton btnNewButton_3 = new JButton("I'm finished for today");
		btnNewButton_3.setOpaque(false);
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setMemberStatus(false, false);
			}
		});
		btnNewButton_3.setEnabled(false);
		mButtonsPanel.add(btnNewButton_3);

		JPanel panel2 = new JPanel();
		GridBagConstraints gbc_panel2 = new GridBagConstraints();
		gbc_panel2.fill = GridBagConstraints.BOTH;
		gbc_panel2.gridheight = 3;
		gbc_panel2.gridx = 0;
		gbc_panel2.gridy = 2;
		panel.add(panel2, gbc_panel2);
		panel2.setBackground(Color.WHITE);
		
		panel_2 = new JPanel();
		panel_2.setBackground(Color.WHITE);
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 6;
		panel.add(panel_2, gbc_panel_2);
		
		panel_3 = new JPanel();
		panel_3.setBackground(Color.WHITE);
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 8;
		panel.add(panel_3, gbc_panel_3);
		
		panel_4 = new JPanel();
		panel_4.setBackground(Color.decode("#0065B3"));
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 1;
		panel.add(panel_4, gbc_panel_4);
		
		panel_5 = new JPanel();
		panel_5.setBackground(Color.decode("#0065B3"));
		GridBagConstraints gbc_panel_5 = new GridBagConstraints();
		gbc_panel_5.fill = GridBagConstraints.BOTH;
		gbc_panel_5.gridx = 0;
		gbc_panel_5.gridy = 5;
		panel.add(panel_5, gbc_panel_5);
		
		panel_6 = new JPanel();
		panel_6.setBackground(Color.decode("#0065B3"));
		GridBagConstraints gbc_panel_6 = new GridBagConstraints();
		gbc_panel_6.fill = GridBagConstraints.BOTH;
		gbc_panel_6.gridx = 0;
		gbc_panel_6.gridy = 7;
		panel.add(panel_6, gbc_panel_6);
	}

	private String getSkillLevel() {
		if (mLevel1Radio.isSelected())
			return "1";
		else if (mLevel2Radio.isSelected())
			return "2";
		else if (mLevel3Radio.isSelected())
			return "3";
		else if (mLevel4Radio.isSelected())
			return "4";
		else
			return "";
	}

	private void clearSearch() {
		mSearchField.setText(null);
		mResultsList.setEnabled(false);
		mResultsList.setModel(EMPTY_RESULTS);
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
		DB.executeTransaction(new Transaction() {
			@Override
			public void run() {
				MemberStatus newStatus = new MemberStatus(mSelectedMember,
						getSkillLevel(), present, playGames);
				update(newStatus);
			}
		});

		clearSearch();
		disableSkill();
		disableMode();

		mSearchField.grabFocus();

		JOptionPane.showMessageDialog(this, "Your status has been changed");
	}

	private void showRegistration() {
		RegisterWindow dialog = RegisterWindow.showDialog(null,
				new RegisterWindow.Callback() {
					@Override
					public void memberRegistered(Member member) {
						CheckInPanel.this.memberRegistered(member);
					}
				});
	}

	private void memberRegistered(Member member) {
		mSearchField.setText(member.getName());
	}
}
