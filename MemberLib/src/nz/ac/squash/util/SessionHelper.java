package nz.ac.squash.util;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.beans.Match;
import nz.ac.squash.db.beans.MatchResult;
import nz.ac.squash.db.beans.Member;
import nz.ac.squash.db.beans.MemberStatus;

public class SessionHelper {
    private static SessionHelper sCurrent = null;

    public synchronized static SessionHelper current() {
        if (sCurrent == null) {
            sCurrent = new SessionHelper();
        }
        return sCurrent;
    }

    // Number of games biasing.
    private static final int sMaximumAbsoluteBias = 1;
    private Map<Member, Float> mAverageGameDeviation = null;

    private SessionHelper() {
    }

    public int getMemberBias(Member member) {
        if (mAverageGameDeviation == null) {
            mAverageGameDeviation = new HashMap<>();

            DB.executeTransaction(new DB.Transaction<Void>() {
                @Override
                public void run() {

                }
            });
        }

        Float averageDeviation = mAverageGameDeviation.get(member);
        if (averageDeviation == null) averageDeviation = 0.f;

        return Math.max(-sMaximumAbsoluteBias,
                Math.min(Math.round(averageDeviation), sMaximumAbsoluteBias));
    }

    public static Map<Member, Float> getGameDeviation(Date date) {
        final Date start = Utility.stripTime(date);
        final Date end = new Date(start.getTime() + 24 * 60 * 60 * 1000);

        final List<Match> matches = DB
                .executeTransaction(new DB.Transaction<List<Match>>() {
                    @Override
                    public void run() {
                        setResult(query(
                                Match.class,
                                "m where m.mDate >= ?0 and m.mDate < ?1 order by m.mDate asc",
                                start, end));
                    }
                });
        if (matches.size() < 2) return new HashMap<>();

        final long firstGameTime = matches.get(0).getDate().getTime();
        final long lastGameTime = matches.get(matches.size() - 1).getDate()
                .getTime();
        final long sessionDuration = lastGameTime - firstGameTime;

        final Map<Member, Float> weightedPresence = new HashMap<>();
        DB.executeTransaction(new DB.Transaction<Void>() {
            @Override
            public void run() {
                Set<Member> presentForChange = new HashSet<>();
                List<MemberStatus> statusChanges = query(
                        MemberStatus.class,
                        "s where s.mDate >= ?0 and s.mDate < ?1 order by s.mDate asc",
                        start, end);

                for (MemberStatus status : statusChanges) {
                    final long changeTime = Math.max(firstGameTime, status
                            .getDate().getTime());
                    final boolean wasPresent = presentForChange.contains(status
                            .getMember());

                    final float changeWeight = (lastGameTime - changeTime) /
                                               (float) sessionDuration;
                    Float runningPresence = weightedPresence.get(status
                            .getMember());
                    if (runningPresence == null) runningPresence = 0.f;

                    if (status.isPresent() && !wasPresent) {
                        // Sign in.
                        runningPresence += changeWeight;
                        presentForChange.add(status.getMember());
                    } else if (!status.isPresent() && wasPresent) {
                        // Sign out.
                        runningPresence -= changeWeight;
                        presentForChange.remove(status.getMember());
                    }
                    weightedPresence.put(status.getMember(), runningPresence);
                }
            }
        });

        final Map<Member, Float> weightedMatchCount = new HashMap<>();
        {
            // Populate so members with 0 matches are considered.
            for (Member member : weightedPresence.keySet()) {
                weightedMatchCount.put(member, 0.f);
            }

            // Fill with unweighted match counts.
            for (Match match : matches) {
                Float p1c = weightedMatchCount.get(match.getPlayer1());
                if (p1c == null) p1c = 0.f;
                p1c += 1.f;
                weightedMatchCount.put(match.getPlayer1(), p1c);

                Float p2c = weightedMatchCount.get(match.getPlayer2());
                if (p2c == null) p2c = 0.f;
                p2c += 1.f;
                weightedMatchCount.put(match.getPlayer2(), p2c);
            }

            // Figure out the mean number of games.
            float meanGameCount = 0.f;
            for (Float count : weightedMatchCount.values()) {
                meanGameCount += count;
            }
            meanGameCount /= weightedMatchCount.size();

            // Adjust to compensate for late members.
            for (Entry<Member, Float> entry : weightedMatchCount.entrySet()) {
                final Member member = entry.getKey();
                final float count = entry.getValue();
                if (count < meanGameCount && count > 0) {
                    entry.setValue(Math.min(meanGameCount,
                            count / weightedPresence.get(member)));
                }
            }
        }
        return weightedMatchCount;
    }

    // Ladder.
    private List<Member> mLadder = null;

    public synchronized List<Member> getLadder() {
        if (mLadder == null) mLadder = MatchResult.getLadder();
        return mLadder;
    }

    public synchronized void invalidateLadder() {
        mLadder = null;
    }
}
