package nz.ac.squash.db.beans;

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
		if (!(obj instanceof Match))
			return false;

		return ((Match) obj).mID == mID;
	}

	public void cancel() {
		DB.executeTransaction(new DB.Transaction() {
			@Override
			public void run() {
				List<MatchHint> satisfiedByThis = query(MatchHint.class,
						"h where h.mSatisfiedBy = ?0", Match.this);
				for (MatchHint hint : satisfiedByThis) {
					hint.setSatisfiedBy(null);
					update(hint);
				}

				// Can only delete object from same session.
				delete(get(Match.class, mID));
			}
		});
	}

	public static Match createMatch(int court, int slot) {
		return createMatch(null, court, slot, null);
	}

	public static Match createMatch(int court, int slot,
			Collection<MatchHint> extraHints) {
		return createMatch(null, court, slot, extraHints);
	}

	private static class MatchProperties {
		public float SkillDifference;
		public int MatchesPlayedToday;
		public int PairedMatchesToday;
		public int PairedMatches;
		public boolean IsRequested;
	}

	public static final Comparator<MatchProperties> WEIGHTED_COMPARATOR = new Comparator<MatchProperties>() {
		private float fairness(MatchProperties o) {
			return o.MatchesPlayedToday;
		}

		private float funness(MatchProperties o) {
			final float skillDifference = Math.abs(o.SkillDifference);
			final int pairedMatchesToday = o.PairedMatchesToday;
			final int allPairedMatches = o.PairedMatches;
			final int matchesToday = o.MatchesPlayedToday;
			final boolean isRequest = o.IsRequested;

			float funness = 0.f; // The lower the better.

			// Playing the same person on the same night isn't fun.
			funness += pairedMatchesToday;

			// Playing someone much better/worse isn't fun (unless you want to).
			if (!isRequest)
				funness += skillDifference * .75f;

			// Playing people from the past is slightly not fun.
			funness += allPairedMatches * .25f;

			// Not getting games is not fun.
			funness += matchesToday;

			return funness;
		}

		@Override
		public int compare(MatchProperties o1, MatchProperties o2) {
			if (o1 == null)
				return 1;
			else if (o2 == null)
				return -1;

			float deltaFairness = fairness(o1) - fairness(o2);
			float deltaFunness = funness(o1) - funness(o2);

			if (Math.abs(deltaFairness) > 1 || deltaFunness == 0.f)
				return (int) Math.signum(deltaFairness);

			return (int) Math.signum(deltaFunness);
		}
	};

	public static final Comparator<MatchProperties> BASIC_COMPARATOR = new Comparator<MatchProperties>() {
		@Override
		public int compare(MatchProperties o1, MatchProperties o2) {
			if (o1 == null)
				return 1;
			else if (o2 == null)
				return -1;

			float deltaSkillDifference = o1.SkillDifference
					- o2.SkillDifference;
			int deltaTotalMatches = o1.MatchesPlayedToday
					- o2.MatchesPlayedToday;
			int deltaPairedMatches = o1.PairedMatchesToday
					- o2.PairedMatchesToday;
			int deltaIsRequest = (o1.IsRequested ? 1 : 0)
					- (o2.IsRequested ? 1 : 0);

			// Prefer matches for players who have played less frequently.
			if (Math.abs(deltaTotalMatches) > 1)
				return deltaTotalMatches;

			// Prefer pairs who have not played each other yet.
			if (deltaPairedMatches != 0)
				return deltaPairedMatches;

			// Prefer matches with similar skills.
			if (deltaSkillDifference != 0 && deltaIsRequest == 0)
				return (int) Math.signum(deltaSkillDifference);

			return deltaTotalMatches;
		}
	};

	private static <T> void incCount(Map<T, Integer> map, T key) {
		Integer count = map.get(key);
		count = Utility.defaultIfNull(count, 0);
		map.put(key, count + 1);
	}

	public static Match createMatch(final Member playerHint, final int court,
			final int slot, final Collection<MatchHint> extraHints) {
		final Match[] returnValue = new Match[] { null };
		DB.executeTransaction(new DB.Transaction() {
			@Override
			public void run() {
				// Load reference materials before n^2 loop.
				Date today = Utility.stripTime(new Date());

				// Get a list of present members, and their latest sign-in
				// status.
				Map<Member, MemberStatus> memberStatuses = new HashMap<>();
				{
					for (MemberStatus latestStatus : MemberStatus
							.getPresentMembers()) {
						memberStatuses.put(latestStatus.getMember(),
								latestStatus);
					}

					// Must have at least 2 players for a match.
					if (memberStatuses.size() < 2) {
						return;
					}
				}

				// Count how many matches each player has had.
				Map<Member, Integer> todaysMatchCounts = new HashMap<>();
				Map<Tuple<Member, Member>, Integer> allPairedMatchCounts = new HashMap<>();
				Map<Tuple<Member, Member>, Integer> todaysPairedMatchCounts = new HashMap<>();
				for (Match match : listAll(Match.class)) {
					final Tuple<Member, Member> pairing = Member.orderedPair(
							match.getPlayer1(), match.getPlayer2());

					incCount(allPairedMatchCounts, pairing);

					if (match.getDate().after(today)) {
						incCount(todaysMatchCounts, match.mPlayer1);
						incCount(todaysMatchCounts, match.mPlayer2);
						incCount(todaysPairedMatchCounts, pairing);
					}
				}

				// Get match hints.
				List<MatchHint> matchHints = query(MatchHint.class,
						"h where h.mDate > ?0 and h.mSatisfiedBy = null", today);
				if (extraHints != null) {
					matchHints.addAll(extraHints);
				}

				// Iterate over each player pairing and compute score.
				Match bestMatch = null;
				MatchProperties bestMatchScore = null;
				for (Member player1 : memberStatuses.keySet()) {
					for (Member player2 : memberStatuses.keySet()) {
						// Skip bad and duplicate pairings.
						if (player1.equals(player2))
							break;

						// Veto match according to hints.
						int vetoedOrSatisfied = 0;
						for (MatchHint hint : matchHints) {
							if (hint.isSatisfiedBy(player1, player2))
								vetoedOrSatisfied = 1;

							if (hint.vetosMatch(player1, player2))
								vetoedOrSatisfied = -1;
						}
						if (vetoedOrSatisfied < 0)
							continue;

						// Veto match if a player does not want to play.
						if ((!memberStatuses.get(player1).wantsGames() || !memberStatuses
								.get(player2).wantsGames())
								&& !(vetoedOrSatisfied > 0))
							continue;

						final Tuple<Member, Member> pairing = Member
								.orderedPair(player1, player2);

						// Compute properties of potential match.
						MatchProperties score = new MatchProperties();
						{
							score.SkillDifference = Utility
									.deltaSkill(memberStatuses.get(player1)
											.getSkillLevel(), memberStatuses
											.get(player2).getSkillLevel());

							Integer player1MatchCount = Utility.defaultIfNull(
									todaysMatchCounts.get(player1), 0);
							Integer player2MatchCount = Utility.defaultIfNull(
									todaysMatchCounts.get(player2), 0);
							score.MatchesPlayedToday = player1MatchCount
									+ player2MatchCount;

							score.PairedMatchesToday = Utility.defaultIfNull(
									todaysPairedMatchCounts.get(pairing), 0);

							score.PairedMatches = Utility.defaultIfNull(
									allPairedMatchCounts.get(pairing), 0);

							score.IsRequested = vetoedOrSatisfied > 0;
						}

						// Pick best so far.
						if (BASIC_COMPARATOR.compare(bestMatchScore, score) > 0) {
							bestMatch = new Match(player1, player2, court, slot);
							bestMatchScore = score;
						}
					}
				}

				// Might not have found any valid matches.
				if (bestMatch != null) {
					returnValue[0] = bestMatch;

					// Save found match with the best score.
					update(bestMatch);

					// Clear satisfied hints.
					for (MatchHint hint : matchHints) {
						if (hint.isSatisfiedBy(bestMatch.mPlayer1,
								bestMatch.mPlayer2)) {
							hint.setSatisfiedBy(bestMatch);
							update(hint);

							// Only satisfy one hint per match.
							// TODO Maybe not depending on hint types.
							break;
						}
					}
				}
			}
		});

		return returnValue[0];
	}
}
