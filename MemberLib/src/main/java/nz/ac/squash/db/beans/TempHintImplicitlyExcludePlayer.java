package nz.ac.squash.db.beans;


public class TempHintImplicitlyExcludePlayer extends MatchHint {
    @Override
    public String toString() {
        return "Exclude " + getPlayer1().getNameFormatted();
    }

    public TempHintImplicitlyExcludePlayer(Member player1) {
        setPlayer1(player1);
    }

    @Override
    public boolean vetosMatch(Member player1, Member player2) {
        // Vetoes the match if the specified player is present.
        return getPlayer1().equals(player1) || getPlayer1().equals(player2);
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
        if (!(obj instanceof TempHintImplicitlyExcludePlayer)) return false;
        final TempHintImplicitlyExcludePlayer other = (TempHintImplicitlyExcludePlayer) obj;

        return getPlayer1().equals(other.getPlayer1());
    }

    @Override
    public boolean isTransient() {
        return true;
    }
}
