package nz.ac.squash.windows;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;
import nz.ac.squash.db.beans.Match;
import nz.ac.squash.db.beans.Member;
import nz.ac.squash.db.beans.MemberStatus;
import nz.ac.squash.util.SwingUtils;
import nz.ac.squash.util.Utility;

public class StatsWindow extends JDialog {
    private static final long serialVersionUID = 1L;
    private JButton mCancelButton;
    private JTable mTable;
    private JScrollPane scrollPane;

    public static StatsWindow showDialog(Component parent) {
        final JFrame frame = parent instanceof JFrame ? (JFrame) parent
                : (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class,
                        parent);

        StatsWindow window = new StatsWindow(frame.getOwner());
        window.loadTable();

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

    private StatsWindow(Window parent) {
        super(parent, ModalityType.APPLICATION_MODAL);
        createContents();

        pack();
        setLocationRelativeTo(null);

        getRootPane().setDefaultButton(mCancelButton);
        SwingUtils.closeOnEscape(this);
    }

    private void createContents() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setIconImage(Toolkit
                .getDefaultToolkit()
                .getImage(
                        StatsWindow.class
                                .getResource("/javax/swing/plaf/metal/icons/ocean/menu.gif")));
        getContentPane().setBackground(Color.WHITE);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] { 0, 0 };
        gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
        gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
        gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0,
                Double.MIN_VALUE };
        getContentPane().setLayout(gridBagLayout);

        JLabel lblStatistics = new JLabel("Statistics");
        lblStatistics.setHorizontalAlignment(SwingConstants.CENTER);
        lblStatistics.setOpaque(true);
        lblStatistics.setBackground(new Color(0, 101, 179));
        lblStatistics.setForeground(Color.WHITE);
        lblStatistics.setFont(new Font("Tahoma", Font.PLAIN, 32));
        GridBagConstraints gbc_lblStatistics = new GridBagConstraints();
        gbc_lblStatistics.ipady = 5;
        gbc_lblStatistics.fill = GridBagConstraints.BOTH;
        gbc_lblStatistics.gridx = 0;
        gbc_lblStatistics.gridy = 0;
        getContentPane().add(lblStatistics, gbc_lblStatistics);

        scrollPane = new JScrollPane();
        scrollPane
                .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane
                .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 1;
        getContentPane().add(scrollPane, gbc_scrollPane);

        mTable = new JTable();
        scrollPane.setViewportView(mTable);
        mTable.setModel(new DefaultTableModel(new Object[][] {}, new String[] {
                "Member", "Match Count" }) {
            Class[] columnTypes = new Class[] { String.class, Integer.class };

            public Class getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }

            boolean[] columnEditables = new boolean[] { false, false };

            public boolean isCellEditable(int row, int column) {
                return columnEditables[column];
            }
        });
        mTable.getColumnModel().getColumn(0).setPreferredWidth(125);

        JPanel panel = new JPanel();
        FlowLayout flowLayout = (FlowLayout) panel.getLayout();
        flowLayout.setAlignment(FlowLayout.RIGHT);
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.gridx = 0;
        gbc_panel.gridy = 2;
        getContentPane().add(panel, gbc_panel);

        mCancelButton = new JButton("Cancel");
        mCancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                dispose();
            }
        });
        panel.add(mCancelButton);
    }

    private void loadTable() {
        final Collection<MemberStatus> members = MemberStatus
                .getPresentMembers();

        final List<Match> matches = //
        DB.executeTransaction(new Transaction<List<Match>>() {
            @Override
            public void run() {
                setResult(query(Match.class, "m where m.mDate >= ?0",
                        Utility.today()));
            }
        });

        final Map<Member, Integer> matchCounts = new HashMap<>();
        for (MemberStatus status : members) {
            matchCounts.put(status.getMember(), 0);
        }
        for (Match match : matches) {
            Integer count1 = matchCounts.get(match.getPlayer1());
            Integer count2 = matchCounts.get(match.getPlayer2());

            if (count1 == null) count1 = 0;
            if (count2 == null) count2 = 0;

            ++count1;
            ++count2;

            matchCounts.put(match.getPlayer1(), count1);
            matchCounts.put(match.getPlayer2(), count2);
        }

        final DefaultTableModel model = (DefaultTableModel) mTable.getModel();
        for (Entry<Member, Integer> entry : matchCounts.entrySet()) {
            model.addRow(new Object[] { entry.getKey().getNameFormatted(),
                    entry.getValue() });
        }

        final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(
                model);
        mTable.setRowSorter(sorter);

        sorter.toggleSortOrder(1);
    }
}
