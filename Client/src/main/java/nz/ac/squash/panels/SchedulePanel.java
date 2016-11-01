package nz.ac.squash.panels;

import nz.ac.squash.widget.MatchPanel;
import nz.ac.squash.widget.MatchTimer;
import nz.ac.squash.widget.generic.VTextIcon;
import nz.ac.squash.widget.generic.VerticalGridLayout;

import javax.swing.*;
import java.awt.*;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SchedulePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final int[] COURTS = new int[]{5, 6, 7, 8, 1, 2, 3, 4};
    private static final int SLOT_MIN = -2;
    private static final int SLOT_MAX = 3;

    private MatchPanel[][] mMatchPanels;
    private final List<List<MatchPanel>> mSlotGroups;
    private final List<List<MatchPanel>> mCourtGroups;
    private JPanel mMatchGrid;

    public SchedulePanel() {
        mSlotGroups = createSlotGroups();
        mCourtGroups = createCourtGroups();

        createContents();

        mMatchPanels = createMatchPanels(getSlotCount(), getCourtCount());

        checkForCollisions();
    }

    private List<List<MatchPanel>> createSlotGroups() {
        return Stream
                .generate(ArrayList<MatchPanel>::new)
                .limit(getSlotCount())
                .collect(Collectors.toList());
    }

    private List<List<MatchPanel>> createCourtGroups() {
        return Stream
                .generate(ArrayList<MatchPanel>::new)
                .limit(getCourtCount())
                .collect(Collectors.toList());
    }

    private MatchPanel[][] createMatchPanels(int slotCount, int courtCount) {
        MatchPanel[][] panels = new MatchPanel[courtCount][slotCount];

        for (int courtIndex = 0; courtIndex < courtCount; ++courtIndex) {
            final int courtNumber = COURTS[courtIndex];

            for (int slotIndex = 0; slotIndex < slotCount; ++slotIndex) {
                final int slotOffset = SLOT_MIN + slotIndex;

                List<MatchPanel> courtGroup = mCourtGroups.get(courtIndex);
                List<MatchPanel> slotGroup = mSlotGroups.get(slotIndex);

                MatchPanel matchPanel = new MatchPanel(courtNumber, slotOffset, slotOffset, courtGroup, slotGroup);

                panels[courtIndex][slotIndex] = matchPanel;
                courtGroup.add(matchPanel);
                slotGroup.add(matchPanel);

                mMatchGrid.add(matchPanel);
            }
        }

        return panels;
    }

    private int getCourtCount() {
        return COURTS.length;
    }

    private int getSlotCount() {
        return SLOT_MAX - SLOT_MIN + 1;
    }

    public Collection<MatchPanel> getMatchPanels() {
        return new AbstractList<MatchPanel>() {
            @Override
            public MatchPanel get(int index) {
                int slotIndex = index % getSlotCount();
                int courtIndex = index / getSlotCount();

                return mMatchPanels[courtIndex][slotIndex];
            }

            @Override
            public int size() {
                return getCourtCount() * getSlotCount();
            }
        };
    }

    private void checkForCollisions() {
        getMatchPanels().forEach(MatchPanel::checkForCollisions);
    }

    private void createContents() {
        setOpaque(false);
        GridBagLayout gbl_schedulePanel = new GridBagLayout();
        gbl_schedulePanel.columnWidths = new int[]{96, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_schedulePanel.rowHeights = new int[]{0, 0, 116, 116, 116, 116, 116, 116, 0, 0};
        gbl_schedulePanel.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
        gbl_schedulePanel.rowWeights = new double[]{0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
        setLayout(gbl_schedulePanel);

        JLabel lblNewLabel_6 = new JLabel("Upstairs");
        lblNewLabel_6.setForeground(Color.WHITE);
        lblNewLabel_6.setFont(new Font("Tahoma", Font.PLAIN, 24));
        GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
        gbc_lblNewLabel_6.gridwidth = 4;
        gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_6.gridx = 1;
        gbc_lblNewLabel_6.gridy = 0;
        add(lblNewLabel_6, gbc_lblNewLabel_6);

        JLabel lblNewLabel_7 = new JLabel("Downstairs");
        lblNewLabel_7.setForeground(Color.WHITE);
        lblNewLabel_7.setFont(new Font("Tahoma", Font.PLAIN, 24));
        GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
        gbc_lblNewLabel_7.gridwidth = 4;
        gbc_lblNewLabel_7.insets = new Insets(0, 0, 5, 0);
        gbc_lblNewLabel_7.gridx = 5;
        gbc_lblNewLabel_7.gridy = 0;
        add(lblNewLabel_7, gbc_lblNewLabel_7);

        JLabel lblNewLabel = new JLabel("Court 5");
        lblNewLabel.setForeground(Color.WHITE);
        lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 24));
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel.gridx = 1;
        gbc_lblNewLabel.gridy = 1;
        add(lblNewLabel, gbc_lblNewLabel);

        JLabel lblNewLabel_1 = new JLabel("Court 6");
        lblNewLabel_1.setForeground(Color.WHITE);
        lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 24));
        GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
        gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_1.gridx = 2;
        gbc_lblNewLabel_1.gridy = 1;
        add(lblNewLabel_1, gbc_lblNewLabel_1);

        JLabel lblNewLabel_2 = new JLabel("Court 7");
        lblNewLabel_2.setForeground(Color.WHITE);
        lblNewLabel_2.setFont(new Font("Tahoma", Font.PLAIN, 24));
        GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
        gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_2.gridx = 3;
        gbc_lblNewLabel_2.gridy = 1;
        add(lblNewLabel_2, gbc_lblNewLabel_2);

        JLabel lblNewLabel_3 = new JLabel("Court 8");
        lblNewLabel_3.setForeground(Color.WHITE);
        lblNewLabel_3.setFont(new Font("Tahoma", Font.PLAIN, 24));
        GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
        gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_3.gridx = 4;
        gbc_lblNewLabel_3.gridy = 1;
        add(lblNewLabel_3, gbc_lblNewLabel_3);

        JLabel lblNewLabel_4 = new JLabel("Court 1");
        lblNewLabel_4.setForeground(Color.WHITE);
        lblNewLabel_4.setFont(new Font("Tahoma", Font.PLAIN, 24));
        GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
        gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_4.gridx = 5;
        gbc_lblNewLabel_4.gridy = 1;
        add(lblNewLabel_4, gbc_lblNewLabel_4);

        JLabel lblNewLabel_5 = new JLabel("Court 2");
        lblNewLabel_5.setForeground(Color.WHITE);
        lblNewLabel_5.setFont(new Font("Tahoma", Font.PLAIN, 24));
        GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
        gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_5.gridx = 6;
        gbc_lblNewLabel_5.gridy = 1;
        add(lblNewLabel_5, gbc_lblNewLabel_5);

        JLabel lblCourt = new JLabel("Court 3");
        lblCourt.setForeground(Color.WHITE);
        lblCourt.setFont(new Font("Tahoma", Font.PLAIN, 24));
        GridBagConstraints gbc_lblCourt = new GridBagConstraints();
        gbc_lblCourt.insets = new Insets(0, 0, 5, 5);
        gbc_lblCourt.gridx = 7;
        gbc_lblCourt.gridy = 1;
        add(lblCourt, gbc_lblCourt);

        JLabel lblCourt_1 = new JLabel("Court 4");
        lblCourt_1.setForeground(Color.WHITE);
        lblCourt_1.setFont(new Font("Tahoma", Font.PLAIN, 24));
        GridBagConstraints gbc_lblCourt_1 = new GridBagConstraints();
        gbc_lblCourt_1.insets = new Insets(0, 0, 5, 0);
        gbc_lblCourt_1.gridx = 8;
        gbc_lblCourt_1.gridy = 1;
        add(lblCourt_1, gbc_lblCourt_1);

        JLabel rtdlblPastGames = new JLabel();
        rtdlblPastGames.setFont(new Font("Tahoma", Font.PLAIN, 18));
        rtdlblPastGames.setIcon(new VTextIcon(rtdlblPastGames, "Previous", VTextIcon.ROTATE_LEFT));
        GridBagConstraints gbc_rtdlblPastGames = new GridBagConstraints();
        gbc_rtdlblPastGames.gridheight = 2;
        gbc_rtdlblPastGames.gridx = 0;
        gbc_rtdlblPastGames.gridy = 2;
        add(rtdlblPastGames, gbc_rtdlblPastGames);

        JLabel rtdlblNowPlaying = new JLabel();
        rtdlblNowPlaying.setForeground(Color.WHITE);
        rtdlblNowPlaying.setFont(new Font("Tahoma", Font.PLAIN, 18));
        rtdlblNowPlaying.setIcon(new VTextIcon(rtdlblPastGames, "Playing", VTextIcon.ROTATE_LEFT));
        GridBagConstraints gbc_rtdlblNowPlaying = new GridBagConstraints();
        gbc_rtdlblNowPlaying.gridx = 0;
        gbc_rtdlblNowPlaying.gridy = 4;
        add(rtdlblNowPlaying, gbc_rtdlblNowPlaying);

        JLabel rtdlblUpcomingGames = new JLabel();
        rtdlblUpcomingGames.setFont(new Font("Tahoma", Font.PLAIN, 18));
        rtdlblUpcomingGames.setIcon(new VTextIcon(rtdlblPastGames, "Upcoming", VTextIcon.ROTATE_LEFT));
        GridBagConstraints gbc_rtdlblUpcomingGames = new GridBagConstraints();
        gbc_rtdlblUpcomingGames.gridheight = 3;
        gbc_rtdlblUpcomingGames.gridx = 0;
        gbc_rtdlblUpcomingGames.gridy = 5;
        add(rtdlblUpcomingGames, gbc_rtdlblUpcomingGames);

        mMatchGrid = new JPanel();
        mMatchGrid.setOpaque(false);
        GridBagConstraints gbc_mMatchGrid = new GridBagConstraints();
        gbc_mMatchGrid.gridheight = 6;
        gbc_mMatchGrid.gridwidth = 8;
        gbc_mMatchGrid.fill = GridBagConstraints.BOTH;
        gbc_mMatchGrid.gridx = 1;
        gbc_mMatchGrid.gridy = 2;
        add(mMatchGrid, gbc_mMatchGrid);
        mMatchGrid.setLayout(new VerticalGridLayout(0, 8, 0, 0));

        MatchTimer panel_7 = new MatchTimer(getMatchPanels());
        GridBagConstraints gbc_panel_7 = new GridBagConstraints();
        gbc_panel_7.insets = new Insets(5, 0, 0, 0);
        gbc_panel_7.fill = GridBagConstraints.BOTH;
        gbc_panel_7.gridx = 0;
        gbc_panel_7.gridy = 8;
        add(panel_7.getPanel(), gbc_panel_7);

        for (int courtIndex = 0; courtIndex < getCourtCount(); courtIndex++) {
            List<MatchPanel> courtPanels = mCourtGroups.get(courtIndex);

            MatchTimer panel_8 = new MatchTimer(courtPanels);
            GridBagConstraints gbc_panel_8 = new GridBagConstraints();
            gbc_panel_8.insets = new Insets(5, 0, 0, 0);
            gbc_panel_8.fill = GridBagConstraints.BOTH;
            gbc_panel_8.anchor = GridBagConstraints.SOUTH;
            gbc_panel_8.gridx = 1 + courtIndex;
            gbc_panel_8.gridy = 8;
            add(panel_8.getPanel(), gbc_panel_8);
        }

        JPanel panel_1 = new JPanel();
        panel_1.setBackground(Color.LIGHT_GRAY);
        GridBagConstraints gbc_panel_1 = new GridBagConstraints();
        gbc_panel_1.gridheight = 2;
        gbc_panel_1.gridwidth = 9;
        gbc_panel_1.fill = GridBagConstraints.BOTH;
        gbc_panel_1.gridx = 0;
        gbc_panel_1.gridy = 2;
        add(panel_1, gbc_panel_1);

        JPanel panel = new JPanel();
        panel.setBackground(Color.decode("#74B2E3"));
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.gridwidth = 9;
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.gridx = 0;
        gbc_panel.gridy = 4;
        add(panel, gbc_panel);

        JPanel panel_3 = new JPanel();
        panel_3.setBackground(Color.WHITE);
        GridBagConstraints gbc_panel_3 = new GridBagConstraints();
        gbc_panel_3.gridwidth = 9;
        gbc_panel_3.gridheight = 3;
        gbc_panel_3.fill = GridBagConstraints.BOTH;
        gbc_panel_3.gridx = 0;
        gbc_panel_3.gridy = 5;
        add(panel_3, gbc_panel_3);
    }
}
