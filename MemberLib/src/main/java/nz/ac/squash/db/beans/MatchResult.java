package nz.ac.squash.db.beans;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;

import javax.persistence.*;
import java.util.*;
import java.util.function.Function;

@Entity
@Table(name = "match_result")
public class MatchResult {
    @Id
    @GeneratedValue
    protected long mID;

    private Date mDate;

    @ManyToOne
    private Member mWinner;

    @ManyToOne
    private Member mLoser;

    @ManyToOne
    private Match mMatch = null;

    public MatchResult() {
        mDate = new Date();
    }

    public MatchResult(Member winner, Match match) {
        this(winner, match.getPlayer1().equals(winner) ? match.getPlayer2() : match.getPlayer1(), match, new Date());
    }

    public MatchResult(Member winner, Member loser) {
        this(winner, loser, new Date());
    }

    public MatchResult(Member winner, Member loser, Date atTime) {
        this(winner, loser, null, atTime);
    }

    public MatchResult(Member winner, Member loser, Match match, Date atTime) {
        mDate = atTime;

        mWinner = winner;
        mLoser = loser;

        mMatch = match;
    }

    public long getID() {
        return mID;
    }

    public Date getDate() {
        return mDate;
    }

    public Member getWinner() {
        return mWinner;
    }

    public Member getLoser() {
        return mLoser;
    }

    public Match getMatch() {
        return mMatch;
    }

    @Override
    public int hashCode() {
        return (int) (mID ^ (mID >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MatchResult)) return false;

        return ((MatchResult) obj).mID == mID;
    }

    @Override
    public String toString() {
        return mWinner.getNameFormatted() + " beat " + (mLoser != null ? mLoser.getNameFormatted() : "nobody");
    }

    public static List<MatchResult> listResults() {
        return DB.executeTransaction(new Transaction<List<MatchResult>>() {
            @Override
            public void run() {
                setResult(query(MatchResult.class, "r order by r.mDate asc"));
            }
        });
    }

    // Creates a pseudo-result for seeding a member on the ladder according to their skill level.
    public static MatchResult seeding(List<Member> ladder, Map<Member, Float> skills, Member member, Date atTime) {
        if (ladder.contains(member)) throw new IllegalArgumentException("Member is already on ladder");

        Function<Member, Float> getSkill = (Member key) -> skills.getOrDefault(key, Float.MAX_VALUE);

        Optional<Member> noHigherThan = ladder.stream()
                .filter(otherMember -> getSkill.apply(otherMember) <= getSkill.apply(member))
                .reduce((a, b) -> b);

        // Insert just below all higher/equal-skilled members.
        int insertIndex = noHigherThan.map(otherMember -> ladder.indexOf(otherMember) + 1).orElse(0);
        Member insertAbove = insertIndex < ladder.size() ? ladder.get(insertIndex) : null;

        return new MatchResult(member, insertAbove, atTime);
    }

    public static List<Member> getLadder() {
        return getLadder(new Date());
    }

    public static List<Member> getLadder(Date upToTime) {
        List<MatchResult> results = DB.executeTransaction(new Transaction<List<MatchResult>>() {
            @Override
            public void run() {
                setResult(query(MatchResult.class, "r where r.mDate < ?0 order by r.mDate asc", upToTime));
            }
        });

        List<Member> ladder = new ArrayList<>();
        results.forEach(result -> updateLadder(ladder, result));

        return ladder;
    }

    public static void updateLadder(List<Member> ladder, MatchResult result) {
        int winnerPos = ladder.size();
        int loserPos = ladder.size();

        for (int i = 0; i < ladder.size(); ++i) {
            if (ladder.get(i).equals(result.getWinner())) {
                winnerPos = i;
                break;
            } else if (ladder.get(i).equals(result.getLoser())) {
                loserPos = i;
                break;
            }
        }

        ladder.remove(result.getWinner());
        ladder.add(Math.min(winnerPos, loserPos), result.getWinner());
    }
}
