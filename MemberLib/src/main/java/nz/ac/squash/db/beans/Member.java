package nz.ac.squash.db.beans;

import nz.ac.squash.db.DB;
import nz.ac.squash.util.NameUtils;
import nz.ac.squash.util.Tuple;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;

@Entity
@Table(name = "members")
public class Member {
    @Id
    @GeneratedValue
    private long mID;

    private Timestamp mSignupTime;

    private String mFirstName;
    private String mLastName;
    private String mNickname;

    private String mStudentId;
    private String mEmail;

    private float mSkillLevel;

    private Float mAmountPaid;

    public long getID() {
        return mID;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        mLastName = lastName;
    }

    public String getNickname() {
        return mNickname;
    }

    public void setNickname(String nickname) {
        mNickname = StringUtils.isNotBlank(nickname) ? nickname : null;
    }

    public String getStudentId() {
        return mStudentId;
    }

    public void setStudentId(String studentId) {
        mStudentId = studentId;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }

    public float getSkillLevel() {
        return mSkillLevel;
    }

    public void setSkillLevel(Float skillLevel) {
        mSkillLevel = skillLevel != null ? skillLevel : 0f;
    }

    public Float getAmountPaid() {
        return mAmountPaid;
    }

    public void setAmountPaid(Float amountPaid) {
        mAmountPaid = amountPaid;
    }

    public boolean hasPaid() {
        return mAmountPaid != null;
    }

    public Timestamp getSignupTime() {
        return mSignupTime;
    }

    public void setSignupTime(Timestamp mSignupTime) {
        this.mSignupTime = mSignupTime;
    }

    @Override
    public int hashCode() {
        return (int) (mID ^ (mID >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Member)) return false;

        return ((Member) obj).mID == mID;
    }

    @Override
    public String toString() {
        return getNameFormatted();
    }

    public String getNameRaw() {
        return String.format("%s %s", mFirstName, mLastName).toLowerCase();
    }

    public String getNameFormatted() {
        return NameUtils.formatName(getNameRaw(), mNickname);
    }

    public String getNameFormattedLong() {
        return NameUtils.formatNameLong(getNameRaw(), mNickname);
    }

    private static String stripName(String input) {
        StringBuilder b = new StringBuilder(input.length());

        for (int i = 0; i < input.length(); ++i) {
            final char c = input.charAt(i);

            if (Character.isLetterOrDigit(c)) {
                b.append(Character.toLowerCase(c));
            } else if (Character.isWhitespace(c)) {
                b.append(' ');
            }
        }

        return b.toString();
    }

    public static Tuple<Member, Member> orderedPair(Member a, Member b) {
        if (a.mID < b.mID) return new Tuple<>(a, b);
        else if (a.mID > b.mID) return new Tuple<>(b, a);
        else throw new IllegalArgumentException("Members must be different");

    }

    public static MemberResults searchMembers(String query, int topN, boolean presentOnly) {
        // Tokenise query.
        List<String> queryTokens = Arrays.asList(StringUtils.split(stripName(query)));
        if (queryTokens.isEmpty()) return new MemberResults();

        int threshold = 0;
        for (String token : queryTokens) {
            int tokenThreshold = token.length() - 3;
            tokenThreshold = Math.max(tokenThreshold, 0);
            tokenThreshold = Math.min(tokenThreshold, 2);
            threshold = Math.max(threshold, tokenThreshold);
        }

        final int[] baseMatch = new int[queryTokens.size()];
        for (int i = 0; i < baseMatch.length; ++i) {
            baseMatch[i] = Integer.MAX_VALUE;
        }

        // Build table for member matches.
        final HashMap<Member, int[]> members = new HashMap<>();
        {
            if (presentOnly) {
                for (MemberStatus member : MemberStatus.getPresentMembers()) {
                    members.put(member.getMember(), Arrays.copyOf(baseMatch, baseMatch.length));
                }
            } else {
                DB.executeTransaction(new DB.Transaction<Void>() {
                    @Override
                    public void run() {
                        for (Member member : listAll(Member.class)) {
                            members.put(member, Arrays.copyOf(baseMatch, baseMatch.length));
                        }
                    }
                });
            }
        }

        // Compare query tokens to member ID tokens to fill match array.
        List<String> memberTokens = new ArrayList<>();
        for (Entry<Member, int[]> memberMatch : members.entrySet()) {
            Member member = memberMatch.getKey();
            int[] match = memberMatch.getValue();

            // Build list of member ID tokens.
            memberTokens.clear();
            Collections.addAll(memberTokens, StringUtils.split(stripName(member.getNameRaw())));
            if (!StringUtils.isBlank(member.mNickname)) {
                Collections.addAll(memberTokens, StringUtils.split(stripName(member.mNickname)));
            }
            if (!StringUtils.isBlank(member.mStudentId)) {
                memberTokens.add(member.mStudentId.toLowerCase());
            }
            if (!StringUtils.isBlank(member.mEmail)) {
                memberTokens.add(member.mEmail.toLowerCase());
            }

            // Compare tokens.
            int i = 0;
            for (String queryToken : queryTokens) {
                for (String memberToken : memberTokens) {
                    String clippedMemberToken = memberToken.substring(0, Math.min(queryToken.length(), memberToken.length()));
                    int dist = StringUtils.getLevenshteinDistance(queryToken, clippedMemberToken);

                    match[i] = Math.min(match[i], dist);
                    if (dist == 0) break;
                }
                ++i;
            }

            Arrays.sort(match);
        }

        // Sort the members the computed match array.
        List<Entry<Member, int[]>> sortedMembers = new ArrayList<>(members.entrySet());
        Collections.sort(sortedMembers, (o1, o2) -> {
            for (int i = 0; i < o1.getValue().length; ++i) {
                int delta = o1.getValue()[i] - o2.getValue()[i];

                if (delta < 0) return -1;
                else if (delta > 0) return 1;
            }

            return o1.getKey().getNameRaw().compareToIgnoreCase(o2.getKey().getNameRaw());
        });

        MemberResults toReturn = new MemberResults();

        // Shortcut if only one exact match.
        if (sortedMembers.size() == 1) {
            toReturn.mHasUniqueMatch = true;
        } else if (sortedMembers.size() >= 2) {
            int firstZeros = 0, secondZeros = 0;
            for (int i = 0; i < sortedMembers.get(0).getValue().length && sortedMembers.get(0).getValue()[i] == 0; ++i) {
                ++firstZeros;
            }
            for (int i = 0; i < sortedMembers.get(1).getValue().length && sortedMembers.get(1).getValue()[i] == 0; ++i) {
                ++secondZeros;
            }

            if (firstZeros == baseMatch.length && secondZeros < baseMatch.length) {
                toReturn.mHasUniqueMatch = true;
            }
        }

        // Otherwise select top N results to return.
        for (int i = 0; toReturn.size() < topN && i < topN && i < sortedMembers.size(); i++) {
            final int[] score = sortedMembers.get(i).getValue();
            final Member member = sortedMembers.get(i).getKey();

            if (score[score.length - 1] <= threshold) {
                toReturn.add(member);
            }
        }

        return toReturn;
    }

    public static class MemberResults extends ArrayList<Member> {
        private static final long serialVersionUID = 1L;

        private boolean mHasUniqueMatch = false;

        public boolean hasUniqueMatch() {
            return mHasUniqueMatch;
        }
    }
}
