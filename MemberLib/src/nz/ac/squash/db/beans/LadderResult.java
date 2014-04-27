package nz.ac.squash.db.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;

@Entity
@Table(name = "ladder_result")
public class LadderResult {
	@Id
	@GeneratedValue
	protected long mID;

	private Date mDate;

	@ManyToOne
	private Member mWinner;

	private int mPlace;

	@ManyToOne(cascade = CascadeType.ALL)
	private Match mSatisfiedBy = null;

	public LadderResult() {
		mDate = new Date();
	}

	public LadderResult(Match match, Member winner) {
		mDate = new Date();

		mWinner = winner;

		mPlace = Math.min(getPlaceOf(match.getPlayer1()),
				getPlaceOf(match.getPlayer2()));

		mSatisfiedBy = null;
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

	public int getPlace() {
		return mPlace;
	}

	public Match getSatisfiedBy() {
		return mSatisfiedBy;
	}

	public static Collection<Member> getLadder() {
		final List<Member> ladder = new ArrayList<Member>();

		DB.executeTransaction(new Transaction() {
			@Override
			public void run() {
				for (LadderResult result : listAll(LadderResult.class)) {
					ladder.remove(result.getWinner());
					ladder.add(result.getPlace(), result.getWinner());
				}
			}
		});

		return ladder;
	}

	public static int getPlaceOf(final Member member) {
		Collection<Member> ladder = getLadder();

		int i = 0;
		for (Member member2 : ladder) {
			if (member2.equals(member))
				return i;
			else
				i++;
		}

		return i;
	}
}
