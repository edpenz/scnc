package nz.ac.squash.db.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import nz.ac.squash.db.DB;
import nz.ac.squash.util.Utility;

@Entity
@Table(name="match_hints")
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
		if (!getClass().isInstance(obj))
			return false;

		return ((MatchHint) obj).mID == mID;
	}

	public abstract boolean vetosMatch(Member player1, Member player2);

	public abstract boolean isSatisfiedBy(Member player1, Member player2);

	public static List<MatchHint> getHintsInEffect() {
		final Date today = Utility.stripTime(new Date());

		final List<MatchHint> hints = new ArrayList<>();
		DB.executeTransaction(new DB.Transaction() {
			@Override
			public void run() {
				hints.addAll(query(MatchHint.class,
						"h where h.mDate > ?0 and h.mSatisfiedBy = null", today));
			}
		});

		return hints;
	}
}
