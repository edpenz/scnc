package nz.ac.squash.windows;

import nz.ac.squash.db.beans.Member;
import nz.ac.squash.util.Importer;
import nz.ac.squash.util.SwingUtils;
import nz.ac.squash.util.Utility;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class RegisterWindow extends JDialog {
    private static final long serialVersionUID = 1L;

    public static interface Callback {
        void memberRegistered(Member member);
    }

    public static RegisterWindow showDialog(Component parent,
                                            RegisterWindow.Callback callback) {
        final JFrame frame = parent instanceof JFrame ? (JFrame) parent
                : (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class,
                parent);

        RegisterWindow window = new RegisterWindow(frame.getOwner());
        window.mCallback = callback;

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

    private JTextField mFirstNameField;
    private JTextField mLastNameField;
    private JTextField mEmailField;
    private JTextField mStudentIdField;

    private ButtonGroup mSkillRadioGroup;
    private ButtonGroup mStudentRadioGroup;
    private ButtonGroup mPaymentRadioGroup;

    private ActionListener mStudentRadioHandler = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
        }
    };

    private RegisterWindow.Callback mCallback = null;

    private static File mExportFile = null;
    private JLabel lblLastName;
    private JLabel mStudentIdLabel;
    private JComboBox mStatusCombo;

    private RegisterWindow(Window window) {
        super(window, ModalityType.APPLICATION_MODAL);

        createContents();

        pack();
        setLocationRelativeTo(null);

        SwingUtils.closeOnEscape(this);
    }

    private void createContents() {
        setIconImage(Toolkit
                .getDefaultToolkit()
                .getImage(RegisterWindow.class.getResource("/javax/swing/plaf/metal/icons/ocean/menu.gif")));

        getContentPane().setBackground(Color.WHITE);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
        getContentPane().setLayout(gridBagLayout);

        JLabel lblJoinTheClub = new JLabel("Join the club");
        lblJoinTheClub.setForeground(Color.WHITE);
        lblJoinTheClub.setFont(new Font("Tahoma", Font.PLAIN, 32));
        GridBagConstraints gbc_lblJoinTheClub = new GridBagConstraints();
        gbc_lblJoinTheClub.insets = new Insets(10, 0, 10, 0);
        gbc_lblJoinTheClub.gridwidth = 2;
        gbc_lblJoinTheClub.gridx = 0;
        gbc_lblJoinTheClub.gridy = 0;
        getContentPane().add(lblJoinTheClub, gbc_lblJoinTheClub);

        JPanel panel1 = new JPanel();
        panel1.setBackground(Color.decode("#535353"));
        GridBagConstraints gbc_panel1 = new GridBagConstraints();
        gbc_panel1.gridwidth = 2;
        gbc_panel1.fill = GridBagConstraints.BOTH;
        gbc_panel1.gridx = 0;
        gbc_panel1.gridy = 0;
        getContentPane().add(panel1, gbc_panel1);
        panel1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.insets = new Insets(10, 20, 10, 20);
        gbc_panel.gridwidth = 2;
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.gridx = 0;
        gbc_panel.gridy = 1;
        getContentPane().add(panel, gbc_panel);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[]{71, 160, 125, 160, 0};
        gbl_panel.rowHeights = new int[]{20, 20, 14, 0};
        gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0,Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_panel);

        JLabel lblNewLabel = new JLabel("First name");
        lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
        gbc_lblNewLabel.insets = new Insets(0, 0, 5, 10);
        gbc_lblNewLabel.gridx = 0;
        gbc_lblNewLabel.gridy = 0;
        panel.add(lblNewLabel, gbc_lblNewLabel);

        mFirstNameField = new JTextField();
        mFirstNameField.setFont(new Font("Tahoma", Font.PLAIN, 18));
        mFirstNameField.setOpaque(false);
        GridBagConstraints gbc_mNameField = new GridBagConstraints();
        gbc_mNameField.fill = GridBagConstraints.HORIZONTAL;
        gbc_mNameField.insets = new Insets(0, 0, 5, 5);
        gbc_mNameField.gridx = 1;
        gbc_mNameField.gridy = 0;
        panel.add(mFirstNameField, gbc_mNameField);

        lblLastName = new JLabel("Last name");
        lblLastName.setFont(new Font("Tahoma", Font.PLAIN, 18));
        GridBagConstraints gbc_lblPreferredName = new GridBagConstraints();
        gbc_lblPreferredName.anchor = GridBagConstraints.EAST;
        gbc_lblPreferredName.insets = new Insets(0, 0, 5, 5);
        gbc_lblPreferredName.gridx = 2;
        gbc_lblPreferredName.gridy = 0;
        panel.add(lblLastName, gbc_lblPreferredName);

        mLastNameField = new JTextField();
        mLastNameField.setFont(new Font("Tahoma", Font.PLAIN, 18));
        mLastNameField.setOpaque(false);
        GridBagConstraints gbc_mNicknameField = new GridBagConstraints();
        gbc_mNicknameField.fill = GridBagConstraints.HORIZONTAL;
        gbc_mNicknameField.insets = new Insets(0, 0, 5, 0);
        gbc_mNicknameField.gridx = 3;
        gbc_mNicknameField.gridy = 0;
        panel.add(mLastNameField, gbc_mNicknameField);

        JLabel lblNewLabel_1 = new JLabel("Email address");
        lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 18));
        GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
        gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
        gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 10);
        gbc_lblNewLabel_1.gridx = 0;
        gbc_lblNewLabel_1.gridy = 1;
        panel.add(lblNewLabel_1, gbc_lblNewLabel_1);

        mEmailField = new JTextField();
        mEmailField.setFont(new Font("Tahoma", Font.PLAIN, 18));
        mEmailField.setOpaque(false);
        GridBagConstraints gbc_mEmailField = new GridBagConstraints();
        gbc_mEmailField.fill = GridBagConstraints.HORIZONTAL;
        gbc_mEmailField.insets = new Insets(0, 0, 5, 0);
        gbc_mEmailField.gridwidth = 3;
        gbc_mEmailField.gridx = 1;
        gbc_mEmailField.gridy = 1;
        panel.add(mEmailField, gbc_mEmailField);

        mSkillRadioGroup = new ButtonGroup();

        JLabel lblNewLabel_3 = new JLabel("Student status");
        lblNewLabel_3.setFont(new Font("Tahoma", Font.PLAIN, 18));
        GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
        gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
        gbc_lblNewLabel_3.insets = new Insets(5, 0, 0, 10);
        gbc_lblNewLabel_3.gridx = 0;
        gbc_lblNewLabel_3.gridy = 2;
        panel.add(lblNewLabel_3, gbc_lblNewLabel_3);

        mStudentRadioGroup = new ButtonGroup();

        mStatusCombo = new JComboBox();
        mStatusCombo.setFont(new Font("Tahoma", Font.PLAIN, 14));
        mStatusCombo.setModel(new DefaultComboBoxModel<>(new String[]{"UoA student", "UoA graduate", "UoA staff", "Other student", "Not a student"}));
        mStatusCombo.setOpaque(false);
        GridBagConstraints gbc_mStatusCombo = new GridBagConstraints();
        gbc_mStatusCombo.insets = new Insets(0, 0, 0, 5);
        gbc_mStatusCombo.fill = GridBagConstraints.BOTH;
        gbc_mStatusCombo.gridx = 1;
        gbc_mStatusCombo.gridy = 2;
        panel.add(mStatusCombo, gbc_mStatusCombo);

        mStudentIdLabel = new JLabel("Student ID");
        mStudentIdLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
        GridBagConstraints gbc_mStudentIdLabel = new GridBagConstraints();
        gbc_mStudentIdLabel.anchor = GridBagConstraints.EAST;
        gbc_mStudentIdLabel.insets = new Insets(0, 0, 0, 10);
        gbc_mStudentIdLabel.gridx = 2;
        gbc_mStudentIdLabel.gridy = 2;
        panel.add(mStudentIdLabel, gbc_mStudentIdLabel);

        mStudentIdField = new JTextField();
        mStudentIdField.setFont(new Font("Tahoma", Font.PLAIN, 18));
        mStudentIdField.setOpaque(false);
        GridBagConstraints gbc_mStudentIdField = new GridBagConstraints();
        gbc_mStudentIdField.fill = GridBagConstraints.HORIZONTAL;
        gbc_mStudentIdField.gridx = 3;
        gbc_mStudentIdField.gridy = 2;
        panel.add(mStudentIdField, gbc_mStudentIdField);

        mPaymentRadioGroup = new ButtonGroup();

        JButton registerButton = new JButton("Register");
        registerButton.setFont(new Font("Tahoma", Font.PLAIN, 18));
        registerButton.setOpaque(false);
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doRegistration();
            }
        });
        GridBagConstraints gbc_registerButton = new GridBagConstraints();
        gbc_registerButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_registerButton.insets = new Insets(5, 5, 5, 0);
        gbc_registerButton.gridx = 0;
        gbc_registerButton.gridy = 2;
        getContentPane().add(registerButton, gbc_registerButton);

        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Tahoma", Font.PLAIN, 18));
        closeButton.setOpaque(false);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                doClose();
            }
        });
        GridBagConstraints gbc_closeButton = new GridBagConstraints();
        gbc_closeButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_closeButton.insets = new Insets(5, 5, 5, 5);
        gbc_closeButton.gridx = 1;
        gbc_closeButton.gridy = 2;
        getContentPane().add(closeButton, gbc_closeButton);

        JPanel panel_4 = new JPanel();
        panel_4.setBackground(Color.decode("#F0F0F0"));
        GridBagConstraints gbc_panel_4 = new GridBagConstraints();
        gbc_panel_4.fill = GridBagConstraints.BOTH;
        gbc_panel_4.gridwidth = 2;
        gbc_panel_4.gridx = 0;
        gbc_panel_4.gridy = 2;
        getContentPane().add(panel_4, gbc_panel_4);
    }

    private void validationError(String message) {
        JOptionPane.showMessageDialog(this, message, "",
                JOptionPane.WARNING_MESSAGE);
    }

    private void doRegistration() {
        // Do validation.
        if (StringUtils.isBlank(mFirstNameField.getText()) || StringUtils.isBlank(mLastNameField.getText())) {
            validationError("Please enter your name");
            return;
        }

        if (StringUtils.isBlank(mEmailField.getText())) {
            validationError("Please enter your email address");
            return;
        }

        // Create new member object filled with form data.
        Member newMember = new Member();
        newMember.setSignupTime(Timestamp.from(Instant.now()));

        newMember.setFirstName(mFirstNameField.getText());
        newMember.setLastName(mLastNameField.getText());

        newMember.setEmail(mEmailField.getText());
        newMember.setStudentId(mStudentIdField.getText());

        // Save to database.
        for (Importer.ImportAction importAction : Importer.generateImport(Arrays.asList(newMember))) {
            importAction.apply();
            newMember = importAction.getMember();
        }

        // Export to CSV.
        if (mExportFile == null) {
            mExportFile = new File("logs/Submissions from " + Utility.FILE_SAFE_FORMATTER.format(new Date()) + ".tsv");
        }

        Importer.Config config = Importer.Config.load("db/config.json");

        List<String> columns = new ArrayList<>();
        setColumn(columns, config.TimestampColumn, Utility.SPREADSHEET_FORMATTER.format(newMember.getSignupTime().toLocalDateTime()));

        setColumn(columns, config.FirstNameColumn, newMember.getFirstName());
        setColumn(columns, config.LastNameColumn, newMember.getLastName());

        setColumn(columns, config.EmailColumn, newMember.getEmail());
        setColumn(columns, config.StudentIdColumn, newMember.getStudentId());

        // TODO Don't hard-code.
        setColumn(columns, 5, mStatusCombo.getSelectedItem().toString());
        setColumn(columns, 11, "Club night PC");

        FileWriter fos = null;
        try {
            fos = new FileWriter(mExportFile, true);
            PrintWriter writer = new PrintWriter(new BufferedWriter(fos));

            for (String column : columns) {
                writer.print(column);
                writer.print("\t");
            }
            writer.print("\r\n");
            writer.flush();

            IOUtils.closeQuietly(writer);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            IOUtils.closeQuietly(fos);
        }

        doClose();

        if (mCallback != null) {
            mCallback.memberRegistered(newMember);
        }
    }

    private static void setColumn(List<String> columns, int index, String value) {
        while (columns.size() < index + 1) {
            columns.add("");
        }

        columns.set(index, value != null ? value : "");
    }

    private void doClose() {
        setVisible(false);
        dispose();
    }

}
