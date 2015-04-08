package nz.ac.squash.widget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import nz.ac.squash.db.beans.Member;

public class LadderEntry extends JPanel {
    private static final long serialVersionUID = 1L;

    private JLabel mNameLabel;
    private JLabel mRankLabel;
    private JLabel mPresentLabel;

    private LadderEntry() {
        createContents();
    }

    public LadderEntry(int rank, Member member, boolean present) {
        this();

        mRankLabel.setText("#" + rank);
        mNameLabel.setText(member.getNameFormatted());
        setBackground(rank % 2 == 1 ? Color.WHITE : Color.decode("#eeeeee"));

        setPresent(present);
    }

    private void createContents() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(0, 0));

        mRankLabel = new JLabel("#1");
        mRankLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mRankLabel.setPreferredSize(new Dimension(40, 14));
        add(mRankLabel, BorderLayout.WEST);

        mNameLabel = new JLabel("Player name");
        mNameLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
        add(mNameLabel);

        mPresentLabel = new JLabel("\u2713");
        mPresentLabel.setPreferredSize(new Dimension(20, 14));
        mPresentLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(mPresentLabel, BorderLayout.EAST);
    }

    public void setPresent(boolean present) {
        setOpaque(present);

        mRankLabel.setForeground(present ? Color.BLACK : Color.LIGHT_GRAY);
        mNameLabel.setForeground(present ? Color.BLACK : Color.LIGHT_GRAY);
        mPresentLabel.setVisible(present);
    }
}
