package nz.ac.squash.reports;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;
import nz.ac.squash.db.beans.MatchResult;
import nz.ac.squash.db.beans.Member;
import nz.ac.squash.db.beans.MemberStatus;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SeedLadder {
    public static void main(String[] args) {
        recreateSeeding();

        System.exit(0);
    }

    private static void recreateSeeding() {
        List<MemberStatus> statuses = MemberStatus.getLatestStatus();
        List<MatchResult> results = MatchResult.listResults();

        List<Object> events = new ArrayList<>();
        events.addAll(statuses);
        events.addAll(results);

        events.sort(Comparator.comparingLong(event -> {
            if (event instanceof MemberStatus) {
                return ((MemberStatus) event).getMember().getSignupTime().getTime();
            } else if (event instanceof MatchResult) {
                return ((MatchResult) event).getDate().getTime();
            } else {
                return 0;
            }
        }));

        Map<Member, Float> skills = MemberStatus.getSkillMapping();

        List<MatchResult> oldSeedings = new ArrayList<>();
        List<MatchResult> newSeedings = new ArrayList<>();

        List<Member> ladder = new ArrayList<>();
        for (Object event : events) {
            if (event instanceof MatchResult) {
                MatchResult result = (MatchResult) event;

                if (result.getMatch() == null) {
                    oldSeedings.add(result);
                } else {
                    MatchResult.updateLadder(ladder, result);
                }
            } else if (event instanceof MemberStatus) {
                MemberStatus status = (MemberStatus) event;
                Member member = status.getMember();
                Timestamp signupTime = member.getSignupTime();

                MatchResult result = MatchResult.seeding(ladder, skills, member, signupTime);
                MatchResult.updateLadder(ladder, result);
                newSeedings.add(result);
            }
        }

        DB.executeTransaction(new Transaction<Void>() {
            @Override
            public void run() {
                oldSeedings.forEach(this::delete);
                newSeedings.forEach(this::update);
            }
        });
    }
}
