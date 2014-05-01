package nz.ac.squash.db.beans;

import nz.ac.squash.util.Tuple;

public class MatchHintTempIncludePlayer extends MatchHint {
    @Override
    public String toString() {
        return "Include " + getPlayer1().getNameFormatted();
    }

    public MatchHintTempIncludePlayer(Member player1) {
        setPlayer1(player1);
    }

    @Override
    public boolean vetosMatch(Member player1, Member player2) {
        // Vetoes the match unless desired player is present.
        return !getPlayer1().equals(player1) && !getPlayer1().equals(player2);
    }

    @Override
    public boolean isSatisfiedBy(Member player1, Member player2) {
        // This is a weak hint, so don't indicate preference.
        return false;
    }

    @Override
    public int hashCode() {
        return getPlayer1().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MatchHintTempIncludePlayer)) return false;
        final MatchHintTempIncludePlayer other = (MatchHintTempIncludePlayer) obj;

        return getPlayer1().equals(other.getPlayer1());
    }

    @Override
    public boolean isTransient() {
        return true;
    }

    @Override
    public boolean overrules(MatchHint otherHint, Tuple<Member, Member> match) {
        if (otherHint instanceof MatchHintRequest) {
            final MatchHintRequest other = (MatchHintRequest) otherHint;

            boolean weSatisfy = getPlayer1().equals(match.getA()) ||
                                getPlayer1().equals(match.getB());
            boolean theyVeto = other.vetosMatch(match.getA(), match.getB());

            return weSatisfy && theyVeto;
        }

        return false;
    }
}
