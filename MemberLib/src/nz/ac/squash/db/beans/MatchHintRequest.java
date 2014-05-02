package nz.ac.squash.db.beans;

import javax.persistence.Entity;

import nz.ac.squash.db.DB;
import nz.ac.squash.util.Tuple;

import org.apache.log4j.Logger;

@Entity
public class MatchHintRequest extends MatchHint {
    private static final Logger sLogger = Logger
            .getLogger(MatchHintRequest.class);

    @Override
    public String toString() {
        return "Request " + getPlayer1().getNameFormatted() + " vs. " +
               getPlayer2().getNameFormatted();
    }

    @Override
    public boolean vetosMatch(Member player1, Member player2) {
        // Vetoes the match if only 1 player is present.
        int playerCount = 0;

        if (getPlayer1().equals(player1)) playerCount++;
        if (getPlayer1().equals(player2)) playerCount++;
        if (getPlayer2().equals(player1)) playerCount++;
        if (getPlayer2().equals(player2)) playerCount++;

        return playerCount == 1;
    }

    @Override
    public boolean isSatisfiedBy(Member player1, Member player2) {
        // Satisfied if both players are present.
        int playerCount = 0;

        if (getPlayer1().equals(player1)) playerCount++;
        if (getPlayer1().equals(player2)) playerCount++;
        if (getPlayer2().equals(player1)) playerCount++;
        if (getPlayer2().equals(player2)) playerCount++;

        return playerCount == 2;
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public boolean isInEffect(MemberStatus player1Status,
            MemberStatus player2Status) {
        boolean player1Present = player1Status != null &&
                                 player1Status.isPresent();
        boolean player2Present = player2Status != null &&
                                 player2Status.isPresent();

        return player1Present && player2Present;
    }

    @Override
    public boolean overrules(MatchHint otherHint, Tuple<Member, Member> match) {
        if (otherHint instanceof MatchHintRequest) {
            final MatchHintRequest other = (MatchHintRequest) otherHint;

            boolean weSatisfy = isSatisfiedBy(match.getA(), match.getB());
            boolean theyVeto = other.vetosMatch(match.getA(), match.getB());

            return weSatisfy && theyVeto;
        }

        return false;
    }

    public static MatchHintRequest createRequest(final Member player1,
            final Member player2) {
        return DB.executeTransaction(new DB.Transaction<MatchHintRequest>() {
            @Override
            public void run() {
                MatchHintRequest request = new MatchHintRequest();
                request.setPlayer1(player1);
                request.setPlayer2(player2);
                update(request);

                sLogger.info("Match requested between " +
                             player1.getNameFormatted() + " and " +
                             player2.getNameFormatted());

                setResult(request);
            }
        });
    }
}
