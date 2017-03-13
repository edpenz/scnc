package nz.ac.squash.windows;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;
import nz.ac.squash.db.beans.Match;
import nz.ac.squash.db.beans.Member;
import nz.ac.squash.db.beans.MemberStatus;
import nz.ac.squash.util.SessionHelper;
import nz.ac.squash.util.SwingUtils;
import nz.ac.squash.util.Utility;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

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
        setIconImage(Toolkit.getDefaultToolkit().getImage(StatsWindow.class
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
        lblStatistics.setBackground(Color.decode("#535353"));
        lblStatistics.setForeground(Color.WHITE);
        lblStatistics.setFont(new Font("Tahoma", Font.PLAIN, 32));
        GridBagConstraints gbc_lblStatistics = new GridBagConstraints();
        gbc_lblStatistics.ipady = 5;
        gbc_lblStatistics.fill = GridBagConstraints.BOTH;
        gbc_lblStatistics.gridx = 0;
        gbc_lblStatistics.gridy = 0;
        getContentPane().add(lblStatistics, gbc_lblStatistics);

        scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 1;
        getContentPane().add(scrollPane, gbc_scrollPane);

        mTable = new JTable();
        mTable.setPreferredScrollableViewportSize(new Dimension(325, 300));
        scrollPane.setViewportView(mTable);
        mTable.setModel(new DefaultTableModel(new Object[][] {},
                new String[] { "Name", "Fee status", "# Matches", "Playing" }) {
            Class[] columnTypes = new Class[] { String.class, String.class,
                    Integer.class, Boolean.class };

            public Class getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }
        });
        mTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        mTable.getColumnModel().getColumn(3).setPreferredWidth(80);

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
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JButton raffleButton = new JButton("Raffle");
        raffleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doRaffle();
            }
        });
        panel.add(raffleButton);
        panel.add(mCancelButton);
    }

    protected void doRaffle() {
        Set<Integer> participatingRows = new HashSet<>();

        final TableModel tableData = mTable.getModel();
        final int rows = tableData.getRowCount();
        for (int row = 0; row < rows; ++row) {
            boolean hasPaid = "".equals(tableData.getValueAt(row, 1));
            if (hasPaid) {
                participatingRows.add(row);
            }
        }

        if (participatingRows.size() > 0) {
            int winner = new Random().nextInt(participatingRows.size());
            int winnerModelRow = new ArrayList<>(participatingRows).get(winner);
            int winnerViewRow = mTable.convertRowIndexToView(winnerModelRow);
            mTable.setRowSelectionInterval(winnerViewRow, winnerViewRow);
        } else {
            mTable.clearSelection();
        }
    }

    private void loadTable() {
        final Collection<MemberStatus> members = MemberStatus
                .getPresentMembers();

        final Map<Member, MemberStatus> statusMapping = new HashMap<>();

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
            statusMapping.put(status.getMember(), status);
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
            final Member member = entry.getKey();
            final int matchCount = entry.getValue();

            String name = member.getNameFormatted();
            MemberStatus status = statusMapping.get(member);

            int nightsAttended = SessionHelper.current()
                    .getPriorNightsAttended(member) + 1;
            boolean hasPaid = member.hasPaid();
            String paymentStatus = "";
            if (!hasPaid) {
                switch (nightsAttended) {
                case 0:
                case 1:
                    paymentStatus = "Trial";
                    break;

                case 2:
                    paymentStatus = "Due";
                    break;

                case 3:
                    paymentStatus = "Overdue";
                    break;

                default:
                    paymentStatus = "Overdue +" + (nightsAttended - 1);
                    break;
                }
            }

            model.addRow(new Object[] { name, paymentStatus, matchCount,
                    status != null ? status.wantsGames() : false });
        }

        final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(
                model);
        mTable.setRowSorter(sorter);

        sorter.toggleSortOrder(2);
    }
}
