package nz.ac.squash.reports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;
import nz.ac.squash.db.beans.MatchResult;
import nz.ac.squash.db.beans.Member;
import nz.ac.squash.db.beans.MemberStatus;

import org.apache.log4j.Logger;

public class SeedLadder {
    public static void main(String[] args) {
        DB.executeTransaction(new Transaction<Void>() {
            @Override
            public void run() {
                if (!listAll(MatchResult.class).isEmpty()) {
                    Logger.getLogger(SeedLadder.class).error(
                            "Ladder already populated");
                    System.exit(1);
                }
            }
        });

        Collection<MemberStatus> statuses = MemberStatus.getLatestStatus();

        final List<MemberStatus> ladderOrder = new ArrayList<MemberStatus>(
                statuses);
        Collections.sort(ladderOrder, new Comparator<MemberStatus>() {
            @Override
            public int compare(MemberStatus a, MemberStatus b) {
                float deltaSkill = a.getSkillLevel() - b.getSkillLevel();
                if (deltaSkill < 0) return +1;
                else if (deltaSkill > 0) return -1;
                else return 0;
            }
        });

        DB.executeTransaction(new Transaction<Void>() {
            @Override
            public void run() {
                for (int i = 0; i < ladderOrder.size() - 1; ++i) {
                    Member low = ladderOrder.get(i).getMember();
                    Member high = ladderOrder.get(i + 1).getMember();

                    MatchResult result = new MatchResult(high, low);
                    update(result);
                }

            }
        });

        System.exit(0);
    }
}
