package nz.ac.squash.db.beans;

import nz.ac.squash.db.DB;
import nz.ac.squash.util.Utility;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "member_statuses")
public class MemberStatus {
    @Id
    @GeneratedValue
    private long mID;

    private Date mDate;

    @ManyToOne
    private Member mMember;

    private float mSkillLevel;

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
        DB.executeTransaction(() -> {
            Optional<MemberStatus> latestStatus = DB.query(MemberStatus.class,
                    "s where s.mMember = ?0 order by s.mDate desc", member)
                    .stream()
                    .findFirst();

            latestStatus.ifPresent(latest -> {
                mSkillLevel = latest.getSkillLevel();

                mPresent = latest.mPresent;
                mWantsGames = latest.mWantsGames;
            });
        });
    }

    public MemberStatus(Member member, float skillLevel, boolean present,
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

    public float getSkillLevel() {
        return mSkillLevel;
    }

    public void setSkillLevel(float skillLevel) {
        mSkillLevel = skillLevel;
    }

    public boolean wantsGames() {
        return mWantsGames;
    }

    public void setWantsGames(boolean wantsGames) {
        mWantsGames = wantsGames;
    }

    public boolean isPresent() {
        return mPresent;
    }

    public void setPresent(boolean present) {
        mPresent = present;
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
        return DB.executeTransaction(() -> {
            String query = "select s from MemberStatus as s "
                    + "where s.mDate >= ?0 and s.mPresent = true and s.mDate = "
                    + "(select max(mDate) from MemberStatus as m where s.mMember = m.mMember)";
            Date today = Utility.today();

            return DB.typedQuery(MemberStatus.class, query, today);
        });
    }

    public static Collection<MemberStatus> getLatestCheckins() {
        return DB.executeTransaction(() -> {
            String query = "select s from MemberStatus as s "
                    + "where s.mPresent = true and s.mDate = "
                    + "(select max(mDate) from MemberStatus as m where s.mMember = m.mMember)";

            return DB.typedQuery(MemberStatus.class, query);
        });
    }

    public static List<MemberStatus> getLatestStatus() {
        return DB.executeTransaction(() -> {
            String query = "select s from MemberStatus as s "
                    + "where s.mDate = "
                    + "(select max(mDate) from MemberStatus as m where s.mMember = m.mMember)";

            return DB.typedQuery(MemberStatus.class, query);
        });
    }

    public static MemberStatus getPreviousStatus(Member member) {
        return DB.executeTransaction(() -> {
            String query = "s where s.mMember = ?0 order by s.mDate desc";
            List<MemberStatus> statuses = DB.query(MemberStatus.class, query, member);
            return Utility.first(statuses);
        });
    }

    public static Map<Member, Float> getSkillMapping() {
        List<MemberStatus> statuses = MemberStatus.getLatestStatus();
        return statuses.stream().collect(Collectors.toMap(MemberStatus::getMember, MemberStatus::getSkillLevel));
    }
}
