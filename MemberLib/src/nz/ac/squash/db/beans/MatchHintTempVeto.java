package nz.ac.squash.db.beans;

import nz.ac.squash.util.Tuple;

public class MatchHintTempVeto extends MatchHint {
	public MatchHintTempVeto(Member player1, Member player2) {
		setPlayer1(player1);
		setPlayer2(player2);
	}

	@Override
	public boolean vetosMatch(Member player1, Member player2) {
		// Vetoes the match if both players are present.
		int playerCount = 0;

		if (getPlayer1().equals(player1))
			playerCount++;
		if (getPlayer1().equals(player2))
			playerCount++;
		if (getPlayer2().equals(player1))
			playerCount++;
		if (getPlayer2().equals(player2))
			playerCount++;

		return playerCount == 2;
	}

	@Override
	public boolean isSatisfiedBy(Member player1, Member player2) {
		// Never satisfied, so never commit to DB.
		return false;
	}

	@Override
	public int hashCode() {
		return getPlayer1().hashCode() ^ getPlayer2().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MatchHintTempVeto))
			return false;
		final MatchHintTempVeto other = (MatchHintTempVeto) obj;

		return getPlayer1().equals(other.getPlayer1())
				&& getPlayer2().equals(other.getPlayer2());
	}

	@Override
	public boolean overrules(MatchHint otherHint, Tuple<Member, Member> forMatch) {
		return otherHint instanceof MatchHintRequest;
	}
}
