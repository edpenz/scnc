package nz.ac.squash.db.beans;

import javax.persistence.Entity;

@Entity
public class MatchHintRequest extends MatchHint {
	@Override
	public boolean vetosMatch(Member player1, Member player2) {
		// Vetoes the match if only 1 player is present.
		int playerCount = 0;

		if (getPlayer1().equals(player1))
			playerCount++;
		if (getPlayer1().equals(player2))
			playerCount++;
		if (getPlayer2().equals(player1))
			playerCount++;
		if (getPlayer2().equals(player2))
			playerCount++;

		return playerCount == 1;
	}

	@Override
	public boolean isSatisfiedBy(Member player1, Member player2) {
		// Satisfied if both players are present.
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
}
