package nz.ac.squash.windows;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutorService;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
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
import nz.ac.squash.widget.JOverlay;
import nz.ac.squash.widget.JTextFieldPlus;

import org.apache.commons.lang3.StringUtils;

public class CheckInWindow extends JDialog {
	private static final long serialVersionUID = 1L;

	public static CheckInWindow showDialog(Window parent) {
		CheckInWindow window = new CheckInWindow(parent);
		window.setVisible(true);

		return window;
	}

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

	private CheckInWindow(Window parent) {
		super(parent, ModalityType.APPLICATION_MODAL);

		createContents();

		pack();
		setLocationRelativeTo(null);
	}

	private void createContents() {
		setIconImage(Toolkit
				.getDefaultToolkit()
				.getImage(
						CheckInWindow.class
								.getResource("/javax/swing/plaf/metal/icons/ocean/menu.gif")));

		setGlassPane(new JOverlay());

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		getContentPane().setBackground(Color.WHITE);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 128, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, 1.0,
				Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 1.0, 1.0, 0.0, 1.0,
				Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);

		JLabel lblNewLabel = new JLabel("Check-in to club night");
		lblNewLabel.setForeground(Color.WHITE);
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 32));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.ipady = 10;
		gbc_lblNewLabel.gridwidth = 3;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		getContentPane().add(lblNewLabel, gbc_lblNewLabel);

		JLabel lblSelectYour = new JLabel("1: Select your name below");
		lblSelectYour.setForeground(Color.WHITE);
		lblSelectYour.setFont(new Font("Tahoma", Font.PLAIN, 16));
		GridBagConstraints gbc_lblSelectYour = new GridBagConstraints();
		gbc_lblSelectYour.insets = new Insets(0, 20, 5, 20);
		gbc_lblSelectYour.ipady = 5;
		gbc_lblSelectYour.gridx = 0;
		gbc_lblSelectYour.gridy = 1;
		getContentPane().add(lblSelectYour, gbc_lblSelectYour);

		mStep2Label = new JLabel("2: Specify your skill level");
		mStep2Label.setForeground(Color.LIGHT_GRAY);
		mStep2Label.setFont(new Font("Tahoma", Font.PLAIN, 16));
		GridBagConstraints gbc_mStep2Label = new GridBagConstraints();
		gbc_mStep2Label.insets = new Insets(0, 20, 5, 20);
		gbc_mStep2Label.gridx = 1;
		gbc_mStep2Label.gridy = 1;
		getContentPane().add(mStep2Label, gbc_mStep2Label);

		mStep3Label = new JLabel("3: Choose how you want to play tonight");
		mStep3Label.setForeground(Color.LIGHT_GRAY);
		mStep3Label.setFont(new Font("Tahoma", Font.PLAIN, 16));
		GridBagConstraints gbc_mStep3Label = new GridBagConstraints();
		gbc_mStep3Label.insets = new Insets(0, 20, 5, 20);
		gbc_mStep3Label.ipady = 5;
		gbc_mStep3Label.gridx = 2;
		gbc_mStep3Label.gridy = 1;
		getContentPane().add(mStep3Label, gbc_mStep3Label);

		mSearchField = new JTextFieldPlus();
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
		GridBagConstraints gbc_mSearchField = new GridBagConstraints();
		gbc_mSearchField.insets = new Insets(5, 5, 5, 5);
		gbc_mSearchField.anchor = GridBagConstraints.NORTH;
		gbc_mSearchField.fill = GridBagConstraints.HORIZONTAL;
		gbc_mSearchField.gridx = 0;
		gbc_mSearchField.gridy = 2;
		getContentPane().add(mSearchField, gbc_mSearchField);
		mSearchField.setColumns(10);

		mResultsList = new JList<Member>();
		mResultsList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				selectMember(mResultsList.getSelectedValue());
			}
		});
		mResultsList.setEnabled(false);
		mResultsList.setModel(EMPTY_RESULTS);
		mResultsList.setFont(new Font("Tahoma", Font.PLAIN, 16));
		GridBagConstraints gbc_mResultsList = new GridBagConstraints();
		gbc_mResultsList.insets = new Insets(0, 5, 5, 5);
		gbc_mResultsList.fill = GridBagConstraints.BOTH;
		gbc_mResultsList.gridx = 0;
		gbc_mResultsList.gridy = 3;
		getContentPane().add(mResultsList, gbc_mResultsList);

		JButton registerButton = new JButton("Not found? Click to register");
		registerButton.setOpaque(false);
		registerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showRegistration();
			}
		});
		registerButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_registerButton = new GridBagConstraints();
		gbc_registerButton.insets = new Insets(5, 5, 5, 5);
		gbc_registerButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_registerButton.gridx = 0;
		gbc_registerButton.gridy = 4;
		getContentPane().add(registerButton, gbc_registerButton);

		JPanel panel_1 = new JPanel();
		panel_1.setOpaque(false);
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.gridheight = 3;
		gbc_panel_1.insets = new Insets(0, 0, 5, 5);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 1;
		gbc_panel_1.gridy = 2;
		getContentPane().add(panel_1, gbc_panel_1);
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

		mButtonsPanel = new JPanel();
		mButtonsPanel.setOpaque(false);
		GridBagConstraints gbc_mButtonsPanel = new GridBagConstraints();
		gbc_mButtonsPanel.insets = new Insets(5, 0, 5, 5);
		gbc_mButtonsPanel.fill = GridBagConstraints.BOTH;
		gbc_mButtonsPanel.gridheight = 3;
		gbc_mButtonsPanel.gridx = 2;
		gbc_mButtonsPanel.gridy = 2;
		getContentPane().add(mButtonsPanel, gbc_mButtonsPanel);
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

		JPanel panel = new JPanel();
		panel.setBackground(Color.decode("#0065B3"));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridheight = 2;
		gbc_panel.gridwidth = 3;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		getContentPane().add(panel, gbc_panel);

		JButton mCancelButton = new JButton("Close");
		mCancelButton.setOpaque(false);
		mCancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
				dispose();
			}
		});
		GridBagConstraints gbc_mCancelButton = new GridBagConstraints();
		gbc_mCancelButton.insets = new Insets(5, 5, 5, 5);
		gbc_mCancelButton.fill = GridBagConstraints.VERTICAL;
		gbc_mCancelButton.anchor = GridBagConstraints.EAST;
		gbc_mCancelButton.gridx = 2;
		gbc_mCancelButton.gridy = 5;
		getContentPane().add(mCancelButton, gbc_mCancelButton);

		JPanel panel_2 = new JPanel();
		panel_2.setBackground(Color.decode("#F0F0F0"));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridwidth = 3;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 5;
		getContentPane().add(panel_2, gbc_panel_2);

		mSkillRadioGroup = new ButtonGroup();
		mSkillRadioGroup.add(mLevel1Radio);
		mSkillRadioGroup.add(mLevel2Radio);
		mSkillRadioGroup.add(mLevel3Radio);
		mSkillRadioGroup.add(mLevel4Radio);
	}

	@Override
	public void dispose() {
		mSearchTask.shutdown();

		super.dispose();
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
		getGlassPane().setVisible(true);

		RegisterWindow dialog = RegisterWindow.showDialog(getOwner(),
				new RegisterWindow.Callback() {
					@Override
					public void memberRegistered(Member member) {
						CheckInWindow.this.memberRegistered(member);
					}
				});

		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				getGlassPane().setVisible(false);
			}
		});
	}

	private void memberRegistered(Member member) {
		mSearchField.setText(member.getName());
	}
}
