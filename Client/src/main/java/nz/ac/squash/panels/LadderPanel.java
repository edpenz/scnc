package nz.ac.squash.panels;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;
import nz.ac.squash.db.beans.MatchResult;
import nz.ac.squash.db.beans.Member;
import nz.ac.squash.db.beans.MemberStatus;
import nz.ac.squash.util.LatestExecutor;
import nz.ac.squash.util.Utility;
import nz.ac.squash.widget.LadderEntry;
import nz.ac.squash.widget.generic.VerticalGridLayout;

public class LadderPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private ButtonGroup mSkillRadioGroup;
    private JLabel lblLadder;
    private JPanel mLadderGrid;

    private static final ListModel<Member> EMPTY_RESULTS = new DefaultListModel<Member>();

    private ExecutorService mSearchTask = new LatestExecutor();
    private ActionListener mSkillButtonHandler = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    };

    private Member mSelectedMember = null;

    private final Map<Member, LadderEntry> mLadderMapping = new HashMap<Member, LadderEntry>();

    public LadderPanel() {
        createContents();

        refreshLadder();
    }

    private void createContents() {
        setOpaque(false);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] { 40, 0, 40, 0 };
        gridBagLayout.rowHeights = new int[] { 20, 0, 0, 40 };
        gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0,
                Double.MIN_VALUE };
        gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0 };
        setLayout(gridBagLayout);

        lblLadder = new JLabel("Ladder");
        lblLadder.setForeground(Color.WHITE);
        lblLadder.setFont(new Font("Tahoma", Font.PLAIN, 32));
        GridBagConstraints gbc_lblLadder = new GridBagConstraints();
        gbc_lblLadder.insets = new Insets(0, 20, 10, 20);
        gbc_lblLadder.gridx = 1;
        gbc_lblLadder.gridy = 1;
        add(lblLadder, gbc_lblLadder);

        mSkillRadioGroup = new ButtonGroup();

        GridBagConstraints gbc_mLadderPanel = new GridBagConstraints();
        gbc_mLadderPanel.insets = new Insets(0, 0, 5, 5);
        gbc_mLadderPanel.fill = GridBagConstraints.BOTH;
        gbc_mLadderPanel.gridx = 1;
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
