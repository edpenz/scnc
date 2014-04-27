package nz.ac.squash.db.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import nz.ac.squash.db.DB;
import nz.ac.squash.util.Tuple;

import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name = "members")
public class Member {
	@Id
	@GeneratedValue
	private long mID;

	private boolean mActive = true;

	// Identifying fields.
	private String mName;
	private String mStudentId;
	private String mUPI;
	private String mEmail;

	// Info fields.
	private String mSkillLevel;

	// Additional fields.
	private Date mSignupTime;
	private String mSignupMethod;

	private String mStudentStatus;
	private String mPaymentMethod;

	public long getID() {
		return mID;
	}

	public boolean isActive() {
		return mActive;
	}

	public void setActive(boolean mActive) {
		this.mActive = mActive;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getStudentId() {
		return mStudentId;
	}

	public void setStudentId(String studentId) {
		this.mStudentId = studentId;
	}

	public String getUPI() {
		return mUPI;
	}

	public void setUPI(String upi) {
		this.mUPI = upi;
	}

	public void setStudentIdAndUpi(String idAndUpi) {
		String[] parts = idAndUpi.split("[^a-zA-Z0-9]");
		for (String part : parts) {
			if (part.matches("^[a-zA-Z]+[0-9]+$"))
				setUPI(part);
			if (part.matches("^[0-9]+$"))
				setStudentId(part);
		}
	}

	public String getEmail() {
		return mEmail;
	}

	public void setEmail(String email) {
		this.mEmail = email;
	}

	public String getSkillLevel() {
		return mSkillLevel;
	}

	public void setSkillLevel(String skillLevel) {
		this.mSkillLevel = skillLevel;
	}

	public String getStudentStatus() {
		return mStudentStatus;
	}

	public void setStudentStatus(String mStudentStatus) {
		this.mStudentStatus = mStudentStatus;
	}

	public String getPaymentMethod() {
		return mPaymentMethod;
	}

	public void setPaymentMethod(String mPaymentMethod) {
		this.mPaymentMethod = mPaymentMethod;
	}

	public String getSignupMethod() {
		return mSignupMethod;
	}

	public void setSignupMethod(String mSignupMethod) {
		this.mSignupMethod = mSignupMethod;
	}

	public Date getSignupTime() {
		return mSignupTime;
	}

	public void setSignupTime(Date mSignupTime) {
		this.mSignupTime = mSignupTime;
	}

	@Override
	public int hashCode() {
		return (int) (mID ^ (mID >>> 32));
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Member))
			return false;

		return ((Member) obj).mID == mID;
	}

	@Override
	public String toString() {
		return getNameFormatted();
	}

	public String getNameFormatted() {
		String[] names = StringUtils.split(mName);
		StringBuilder prettyName = new StringBuilder(mName.length());

		for (String name : names) {
			// Make first character upper case.
			prettyName.append(Character.toUpperCase(name.charAt(0)));

			// Make remainder lower case if it was all upper case.
			if (name.length() > 1) {
				String remainder = name.substring(1);
				if (remainder.equals(remainder.toUpperCase()))
					prettyName.append(remainder.toLowerCase());
				else
					prettyName.append(remainder);
			}

			// Add spacer.
			prettyName.append(' ');
		}

		// Remove final spacer.
		prettyName.deleteCharAt(prettyName.length() - 1);

		return prettyName.toString();
	}

	private static String getNameStripped(String input) {
		StringBuilder b = new StringBuilder(input.length());

		for (int i = 0; i < input.length(); ++i) {
			final char c = input.charAt(i);

			if (Character.isLetter(c)) {
				b.append(Character.toLowerCase(c));
			} else if (Character.isWhitespace(c)) {
				b.append(' ');
			}
		}

		return b.toString();
	}

	public static Tuple<Member, Member> orderedPair(Member a, Member b) {
		if (a.mID < b.mID)
			return new Tuple<>(a, b);
		else if (a.mID > b.mID)
			return new Tuple<>(b, a);
		else
			throw new IllegalArgumentException("Members must be different");

	}

	public static MemberResults searchMembers(String query, int topN,
			int threshold, boolean presentOnly) {
		// Tokenise query.
		List<String> queryTokens = Arrays.asList(StringUtils
				.split(getNameStripped(query)));

		final int[] baseMatch = new int[queryTokens.size()];
		for (int i = 0; i < baseMatch.length; ++i) {
			baseMatch[i] = Integer.MAX_VALUE;
		}

		// Build table for member matches.
		final HashMap<Member, int[]> members = new HashMap<Member, int[]>();
		{
			if (!presentOnly) {
				DB.executeTransaction(new DB.Transaction() {
					@Override
					public void run() {
						for (Member member : listAll(Member.class)) {
							members.put(member,
									Arrays.copyOf(baseMatch, baseMatch.length));
						}
					}
				});
			} else {
				DB.executeTransaction(new DB.Transaction() {
					@Override
					public void run() {
						for (MemberStatus member : MemberStatus
								.getPresentMembers()) {
							members.put(member.getMember(),
									Arrays.copyOf(baseMatch, baseMatch.length));
						}
					}
				});
			}
		}

		// Compare query tokens to member ID tokens to fill match array.
		List<String> memberTokens = new ArrayList<String>();
		for (Entry<Member, int[]> memberMatch : members.entrySet()) {
			Member member = memberMatch.getKey();
			int[] match = memberMatch.getValue();

			// Build list of member ID tokens.
			memberTokens.clear();
			Collections.addAll(memberTokens,
					StringUtils.split(getNameStripped(member.mName)));
			if (!StringUtils.isBlank(member.mStudentId))
				memberTokens.add(member.mStudentId.toLowerCase());
			if (!StringUtils.isBlank(member.mUPI))
				memberTokens.add(member.mUPI.toLowerCase());
			if (!StringUtils.isBlank(member.mEmail))
				memberTokens.add(member.mEmail.toLowerCase());

			// Compare tokens.
			int i = 0;
			for (String queryToken : queryTokens) {
				for (String memberToken : memberTokens) {
					int dist = StringUtils.getLevenshteinDistance(
							queryToken,
							memberToken.substring(
									0,
									Math.min(queryToken.length(),
											memberToken.length())));

					match[i] = Math.min(match[i], dist);
					if (dist == 0)
						break;
				}
				++i;
			}

			Arrays.sort(match);
		}

		// Sort the members the computed match array.
		List<Entry<Member, int[]>> sortedMembers = new ArrayList<>(
				members.entrySet());
		Collections.sort(sortedMembers, new Comparator<Entry<Member, int[]>>() {
			@Override
			public int compare(Entry<Member, int[]> o1, Entry<Member, int[]> o2) {
				for (int i = 0; i < o1.getValue().length; ++i) {
					int delta = o1.getValue()[i] - o2.getValue()[i];

					if (delta < 0)
						return -1;
					else if (delta > 0)
						return 1;
					else
						continue;
				}

				return o1.getKey().getName()
						.compareToIgnoreCase(o2.getKey().getName());
			}
		});

		MemberResults toReturn = new MemberResults();

		// Shortcut if only one exact match.
		if (sortedMembers.size() == 1) {
			toReturn.mHasUniqueMatch = true;
		} else if (sortedMembers.size() >= 2) {
			int firstZeros = 0, secondZeros = 0;
			for (int i = 0; i < sortedMembers.get(0).getValue().length
					&& sortedMembers.get(0).getValue()[i] == 0; ++i)
				++firstZeros;
			for (int i = 0; i < sortedMembers.get(1).getValue().length
					&& sortedMembers.get(1).getValue()[i] == 0; ++i)
				++secondZeros;

			if (firstZeros == baseMatch.length
					&& secondZeros < baseMatch.length) {
				toReturn.mHasUniqueMatch = true;
			}
		}

		// Otherwise select top N results to return.
		outer: for (int i = 0; toReturn.size() < topN
				&& i < sortedMembers.size(); i++) {
			for (int match : sortedMembers.get(i).getValue()) {
				if (match > threshold)
					continue outer;
			}
			toReturn.add(sortedMembers.get(i).getKey());
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
