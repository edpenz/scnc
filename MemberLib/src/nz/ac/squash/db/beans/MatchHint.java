package nz.ac.squash.db.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import nz.ac.squash.db.DB;
import nz.ac.squash.util.Tuple;
import nz.ac.squash.util.Utility;

@Entity
@Table(name = "match_hints")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class MatchHint {
    @Id
    @GeneratedValue
    protected long mID;

    private Date mDate;

    @ManyToOne
    private Member mPlayer1;
    @ManyToOne
    private Member mPlayer2;

    @ManyToOne
    private Match mSatisfiedBy = null;

    public MatchHint() {
        mDate = new Date();
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public Member getPlayer1() {
        return mPlayer1;
    }

    public void setPlayer1(Member player1) {
        this.mPlayer1 = player1;
    }

    public Member getPlayer2() {
        return mPlayer2;
    }

    public void setPlayer2(Member player2) {
        this.mPlayer2 = player2;
    }

    public Match getSatisfiedBy() {
        return mSatisfiedBy;
    }

    public void setSatisfiedBy(Match match) {
        this.mSatisfiedBy = match;
    }

    @Override
    public int hashCode() {
        return (int) (mID ^ (mID >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (!getClass().isInstance(obj)) return false;

        return ((MatchHint) obj).mID == mID;
    }

    public abstract boolean isTransient();

    public boolean isInEffect(MemberStatus player1Status,
            MemberStatus player2Status) {
        return true;
    }

    public boolean overrules(MatchHint otherHint, Tuple<Member, Member> forMatch) {
        return false;
    }

    public abstract boolean vetosMatch(Member player1, Member player2);

    public abstract boolean isSatisfiedBy(Member player1, Member player2);

    public boolean isOverruledByAny(Collection<MatchHint> hints,
            Tuple<Member, Member> forMatch) {
        for (MatchHint hint : hints) {
            if (hint.overrules(this, forMatch)) return true;
        }
        return false;
    }

    public Collection<MatchHint> overrules(Collection<MatchHint> hints,
            Tuple<Member, Member> forMatch) {
        List<MatchHint> ret = new ArrayList<>();
        for (MatchHint hint : hints) {
            if (overrules(hint, forMatch)) ret.add(hint);
        }
        return ret;
    }

    public static int checkForOverrule(MatchHint a, MatchHint b,
            Tuple<Member, Member> forMatch) {
        if (a.overrules(b, forMatch)) return -1;
        else if (b.overrules(a, forMatch)) return 1;
        else return 0;
    }

    public static List<MatchHint> getHintsInEffect() {
        final Date today = Utility.stripTime(new Date());

        final List<MatchHint> hints = new ArrayList<>();
        DB.executeTransaction(new DB.Transaction<Void>() {
            @Override
            public void run() {
                hints.addAll(query(MatchHint.class,
                        "h where h.mDate > ?0 and h.mSatisfiedBy = null", today));
            }
        });

        return hints;
    }
}
