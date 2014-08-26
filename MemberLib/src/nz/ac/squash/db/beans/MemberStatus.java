package nz.ac.squash.db.beans;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;
import nz.ac.squash.util.Utility;

@Entity
@Table(name = "member_statuses")
public class MemberStatus {
    @Id
    @GeneratedValue
    private long mID;

    private Date mDate;

    @ManyToOne
    private Member mMember;

    private String mSkillLevel;

    private boolean mPresent;
    private boolean mWantsGames;

    // For hibernate.
    @SuppressWarnings("unused")
    private MemberStatus() {
    }

    // Creates a new status based on the last known state of the member.
    public MemberStatus(final Member member) {
        mDate = new Date();
        mMember = member;

        mSkillLevel = member.getSkillLevel();

        mPresent = false;
        mWantsGames = false;

        // Restore values from most recent status.
        DB.executeTransaction(new DB.Transaction<Void>() {
            @Override
            public void run() {
                List<MemberStatus> latestStatus = query(MemberStatus.class,
                        "s where s.mMember = ?0 order by s.mDate desc", member);
                if (!latestStatus.isEmpty()) {
                    final MemberStatus latest = latestStatus.get(0);

                    mSkillLevel = latest.getSkillLevel();

                    mPresent = latest.mPresent;
                    mWantsGames = latest.mWantsGames;
                }
            }
        });
    }

    public MemberStatus(Member member, String skillLevel, boolean present,
            boolean wantsGames) {
        mDate = new Date();
        mMember = member;

        mSkillLevel = skillLevel;

        mPresent = present;
        mWantsGames = wantsGames;
    }

    public long getID() {
        return mID;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public Member getMember() {
        return mMember;
    }

    public void setMember(Member mMember) {
        this.mMember = mMember;
    }

    public String getSkillLevel() {
        return mSkillLevel;
    }

    public void setSkillLevel(String mSkillLevel) {
        this.mSkillLevel = mSkillLevel;
    }

    public boolean wantsGames() {
        return mWantsGames;
    }

    public void setWantsGames(boolean mWantsGames) {
        this.mWantsGames = mWantsGames;
    }

    public boolean isPresent() {
        return mPresent;
    }

    public void setPresent(boolean mPresent) {
        this.mPresent = mPresent;
    }

    @Override
    public int hashCode() {
        return (int) (mID ^ (mID >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MemberStatus)) return false;

        return ((MemberStatus) obj).mID == mID;
    }

    @Override
    public String toString() {
        return mMember.toString();
    }

    public static Collection<MemberStatus> getPresentMembers() {
        return DB
                .executeTransaction(new Transaction<Collection<MemberStatus>>() {
                    @Override
                    public void run() {
                        Date today = Utility.stripTime(new Date());

                        setResult(typedQuery(
                                MemberStatus.class,
                                "select s from " +
                                        MemberStatus.class.getName() +
                                        " as s where s.mDate >= ?0 and s.mActive = true and s.mPresent = true and s.mDate = (select max(mDate) from " +
                                        MemberStatus.class.getName() +
                                        " as m where s.mMember = m.mMember)",
                                today));
                    }
                });
    }

    public static Collection<MemberStatus> getLatestCheckins() {
        return DB
                .executeTransaction(new Transaction<Collection<MemberStatus>>() {
                    @Override
                    public void run() {
                        setResult(typedQuery(
                                MemberStatus.class,
                                "select s from " +
                                        MemberStatus.class.getName() +
                                        " as s where s.mPresent = true and s.mDate = (select max(mDate) from " +
                                        MemberStatus.class.getName() +
                                        " as m where s.mMember = m.mMember)"));
                    }
                });
    }

    public static Collection<MemberStatus> getLatestStatus() {
        return DB
                .executeTransaction(new Transaction<Collection<MemberStatus>>() {
                    @Override
                    public void run() {
                        setResult(typedQuery(
                                MemberStatus.class,
                                "select s from " +
                                        MemberStatus.class.getName() +
                                        " as s where s.mDate = (select max(mDate) from " +
                                        MemberStatus.class.getName() +
                                        " as m where s.mMember = m.mMember)"));
                    }
                });
    }
}
