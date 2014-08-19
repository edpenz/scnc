package nz.ac.squash.db.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;
import nz.ac.squash.util.SessionHelper;
import nz.ac.squash.util.Tuple;
import nz.ac.squash.util.Utility;

import org.apache.log4j.Logger;

@Entity
@Table(name = "matches")
public class Match {
    static Logger sLogger = Logger.getLogger(Match.class);

    @Id
    @GeneratedValue
    private long mID;

    private Date mDate;

    private int mCourt;
    private int mTimeSlot;

    @ManyToOne
    private Member mPlayer1;
    @ManyToOne
    private Member mPlayer2;

    // For Hibernate.
    private Match() {
    }

    private Match(Member player1, Member player2, int court, int slot) {
        mDate = new Date();

        mCourt = court;
        mTimeSlot = slot;

        mPlayer1 = player1;
        mPlayer2 = player2;
    }

    public long getID() {
        return mID;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date mDate) {
        this.mDate = mDate;
    }

    public int getCourt() {
        return mCourt;
    }

    public void setCourt(int mCourt) {
        this.mCourt = mCourt;
    }

    public int getTimeSlot() {
        return mTimeSlot;
    }

    public void setTimeSlot(int mTimeSlot) {
        this.mTimeSlot = mTimeSlot;
    }

    public Member getPlayer1() {
        return mPlayer1;
    }

    public void setPlayer1(Member mPlayer1) {
        this.mPlayer1 = mPlayer1;
    }

    public Member getPlayer2() {
        return mPlayer2;
    }

    public void setPlayer2(Member mPlayer2) {
        this.mPlayer2 = mPlayer2;
    }

    @Override
    public int hashCode() {
        return (int) (mID ^ (mID >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Match)) return false;

        return ((Match) obj).mID == mID;
    }

    @Override
    public String toString() {
        return mPlayer1.getNameFormatted() + " vs. " +
               mPlayer2.getNameFormatted() + " (#" + mID + ")";
    }

    public void cancel() {
        DB.executeTransaction(new DB.Transaction<Void>() {
            @Override
            public void run() {
                sLogger.info("Cancelling match: " + Match.this);
                attach(Match.this);

                // Re-enable hints satisfied by this match.
                for (MatchHint hint : query(MatchHint.class,
                        "h where h.mSatisfiedBy = ?0", Match.this)) {
                    hint.setSatisfiedBy(null);
                    update(hint);

                    sLogger.info("  which satisfied: " + hint);
                }

                // Remove result for match.
                for (MatchResult result : query(MatchResult.class,
                        "r where r.mMatch = ?0", Match.this)) {
                    delete(result);
                    SessionHelper.current().invalidateLadder();

                    sLogger.info("  which had result: " + result);
                }

                delete(Match.this);
            }
        });
    }

    public static Match createMatch(final Member player1, final Member player2,
            final int court, final int slot) {
        return DB.executeTransaction(new Transaction<Match>() {
            @Override
            public void run() {
                // Create fixed match.
                Match proto = new Match(player1, player2, court, slot);
                update(proto);

                sLogger.info("Created match: " + proto);

                // Satisfy any relevant hints.
                List<MatchHint> matchHints = MatchHint.getHintsInEffect();

                for (MatchHint hint : matchHints) {
                    if (hint.isSatisfiedBy(player1, player2)) {
                        hint.setSatisfiedBy(proto);
                        update(hint);

                        sLogger.info("  which satisfies: " + hint);
                    }
                }

                setResult(proto);
            }
        });
    }

    public static Match createMatch(int court, int slot) {
        return createMatch(court, slot, null);
    }

    private static class MatchProperties {
        public float SkillDifference;
        public int TimesAlreadyPaired;
        public boolean IsRequested;

        public int CombinedMatchCount;
        public int MinimumMatchCount;
    }

    public static final Comparator<MatchProperties> FAIR_COMPARATOR = new Comparator<MatchProperties>() {
        @Override
        public int compare(MatchProperties o1, MatchProperties o2) {
            if (o1 == null) return 1;
            else if (o2 == null) return -1;

            float deltaSkillDifference = o1.SkillDifference -
                                         o2.SkillDifference;
            int deltaTotalMatches = o1.CombinedMatchCount -
                                    o2.CombinedMatchCount;
            int deltaMinMatches = o1.MinimumMatchCount - o2.MinimumMatchCount;
            int deltaPairedMatches = o1.TimesAlreadyPaired -
                                     o2.TimesAlreadyPaired;
            int deltaIsRequest = (o1.IsRequested ? 0 : 1) -
                                 (o2.IsRequested ? 0 : 1);

            // Prefer matches for players who have played less frequently.
            if (Math.abs(deltaTotalMatches) > 1) return deltaTotalMatches;
            if (Math.abs(deltaMinMatches) > 1) return deltaMinMatches;

            // Prefer requests.
            if (deltaIsRequest != 0) return deltaIsRequest;

            // Prefer pairs who have not played each other yet.
            if (deltaPairedMatches != 0) return deltaPairedMatches;

            // Prefer matches with similar skills.
            if (deltaSkillDifference != 0) return (int) Math
                    .signum(deltaSkillDifference);

            return deltaTotalMatches;
        }
    };

    private static <T> void incCount(Map<T, Integer> map, T key) {
        Integer count = map.get(key);
        count = Utility.firstNonNull(count, 0);
        map.put(key, count + 1);
    }

    private static float deltaSkill(Map<Member, Integer> ladder,
            Member memberA, Member memberB) {
        final int aPos = ladder.get(memberA);
        final int bPos = ladder.get(memberB);

        return (Math.abs(aPos - bPos) + 1.f) / ladder.size();
    }

    public static Match createMatch(final int court, final int slot,
            final Collection<MatchHint> extraHints) {
        return DB.executeTransaction(new DB.Transaction<Match>() {
            @Override
            public void run() {
                // Load reference materials before n^2 loop.
                final Date today = Utility.stripTime(new Date());

                // Get a list of present members, and their latest sign-in
                // status.
                final Map<Member, MemberStatus> memberStatuses = new HashMap<>();
                {
                    for (MemberStatus status : MemberStatus.getPresentMembers()) {
                        memberStatuses.put(status.getMember(), status);
                    }

                    // Must have at least 2 players for a match.
                    if (memberStatuses.size() < 2) {
                        return;
                    }
                }

                // Count how many matches each player has had.
                final Map<Member, Integer> todaysMatchCounts = new HashMap<>();
                final Map<Tuple<Member, Member>, Integer> todaysPairedMatchCounts = new HashMap<>();
                for (Match match : listAll(Match.class)) {
                    final Tuple<Member, Member> pairing = Member.orderedPair(
                            match.getPlayer1(), match.getPlayer2());

                    if (match.getDate().after(today)) {
                        incCount(todaysMatchCounts, match.mPlayer1);
                        incCount(todaysMatchCounts, match.mPlayer2);
                        incCount(todaysPairedMatchCounts, pairing);
                    }
                }

                // Get match hints.
                final List<MatchHint> matchHints = new ArrayList<MatchHint>();
                {
                    matchHints.addAll(MatchHint.getHintsInEffect());
                    if (extraHints != null) {
                        matchHints.addAll(extraHints);
                    }
                }

                // Get ladder of present members.
                final Map<Member, Integer> ladder = new HashMap<>();
                for (Member member : SessionHelper.current().getLadder()) {
                    ladder.put(member, ladder.size());
                }

                // Iterate over each player pairing and compute score.
                Tuple<Member, Member> bestMatch = null;
                MatchProperties bestMatchScore = null;
                for (Member player1 : memberStatuses.keySet()) {
                    for (Member player2 : memberStatuses.keySet()) {
                        // Skip bad and duplicate pairings.
                        if (player1.equals(player2)) break;

                        final Tuple<Member, Member> pairing = Member
                                .orderedPair(player1, player2);

                        // Apply hints to veto or prefer match.
                        final List<MatchHint> satisfyHints = new ArrayList<MatchHint>();
                        final List<MatchHint> vetoHints = new ArrayList<MatchHint>();
                        for (MatchHint hint : matchHints) {
                            // Skip if hint is no longer in effect.
                            final boolean inEffect = hint.isInEffect(
                                    memberStatuses.get(hint.getPlayer1()),
                                    memberStatuses.get(hint.getPlayer2()));
                            if (!inEffect) continue;

                            // Skip if hint is overruled by a previous ones.
                            final boolean overruled = hint.isOverruledByAny(
                                    satisfyHints, pairing) ||
                                                      hint.isOverruledByAny(
                                                              vetoHints,
                                                              pairing);
                            if (overruled) continue;

                            // Discard hints that this one overrules.
                            satisfyHints.removeAll(hint.overrules(satisfyHints,
                                    pairing));
                            vetoHints.removeAll(hint.overrules(vetoHints,
                                    pairing));

                            // Check if and how hint applies to this match.
                            if (hint.isSatisfiedBy(player1, player2)) {
                                satisfyHints.add(hint);
                            } else if (hint.vetosMatch(player1, player2)) {
                                vetoHints.add(hint);
                            }
                        }

                        boolean satisfies = !satisfyHints.isEmpty();
                        boolean isVetoed = !vetoHints.isEmpty();

                        if (isVetoed) continue;

                        // Veto if match is random, but player only wants
                        // specific matches.
                        if ((!memberStatuses.get(player1).wantsGames() || !memberStatuses
                                .get(player2).wantsGames()) && !satisfies) continue;

                        // Compute properties of potential match.
                        final MatchProperties score = new MatchProperties();
                        {
                            score.SkillDifference = deltaSkill(ladder, player1,
                                    player2);

                            Integer player1MatchCount = Utility.firstNonNull(
                                    todaysMatchCounts.get(player1), 0);
                            Integer player2MatchCount = Utility.firstNonNull(
                                    todaysMatchCounts.get(player2), 0);
                            score.CombinedMatchCount = player1MatchCount +
                                                       player2MatchCount;
                            score.MinimumMatchCount = Math.min(
                                    player1MatchCount, player2MatchCount);

                            score.TimesAlreadyPaired = Utility.firstNonNull(
                                    todaysPairedMatchCounts.get(pairing), 0);

                            score.IsRequested = satisfies;
                        }

                        // Pick best so far.
                        if (FAIR_COMPARATOR.compare(bestMatchScore, score) > 0) {
                            bestMatch = pairing;
                            bestMatchScore = score;
                        }
                    }
                }

                // Might not have found any valid matches.
                if (bestMatch != null) {
                    setResult(createMatch(bestMatch.getA(), bestMatch.getB(),
                            court, slot));
                }
            }
        });
    }
}
