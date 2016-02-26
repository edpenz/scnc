package nz.ac.squash.reports;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;
import nz.ac.squash.db.beans.Match;
import nz.ac.squash.db.beans.Member;
import nz.ac.squash.db.beans.MemberStatus;
import nz.ac.squash.util.Utility;

public class ReportFairness {
    private static final DateFormat sDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd");

    public static void makeReport() throws IOException {
        File log = new File("logs/fairness.csv");
        PrintStream writer = new PrintStream(log);

        List<Match> matches = DB
                .executeTransaction(new Transaction<List<Match>>() {
                    @Override
                    public void run() {
                        setResult(query(Match.class, "m order by m.mDate asc"));
                    }
                });

        final Map<Member, Integer> gameCount = new HashMap<>();

        Date today = new Date(0), tomorrow = new Date(0);
        for (Match match : matches) {
            if (match.getDate().after(tomorrow)) {
                writeHistogram(writer, today, gameCount);
                gameCount.clear();

                today = Utility.stripTime(match.getDate());
                tomorrow = new Date(today.getTime() + 24l * 60 * 60 * 1000);

                final Date start = today, end = tomorrow;
                DB.executeTransaction(new DB.Transaction<Void>() {
                    @Override
                    public void run() {
                        for (MemberStatus status : query(MemberStatus.class,
                                "s where s.mDate > ?0 and s.mDate < ?1", start,
                                end)) {
                            gameCount.put(status.getMember(), 0);
                        }
                    }
                });
            }

            Integer p1count = gameCount.get(match.getPlayer1());
            Integer p2count = gameCount.get(match.getPlayer2());

            p1count++;
            p2count++;

            gameCount.put(match.getPlayer1(), p1count);
            gameCount.put(match.getPlayer2(), p2count);
        }

        writeHistogram(writer, today, gameCount);

        writer.close();
    }

    private static void writeHistogram(PrintStream stream, Date date,
            Map<Member, Integer> gameCount) {
        if (gameCount.size() == 0) return;

        int maxNumberOfGames = Collections.max(gameCount.values());
        int[] gameHistogram = new int[maxNumberOfGames + 1];

        for (int count : gameCount.values()) {
            gameHistogram[count]++;
        }

        stream.print(sDateFormat.format(date));
        for (int i : gameHistogram) {
            stream.print("," + String.valueOf(i));
        }
        stream.println();
    }

    public static void main(String[] args) throws IOException {
        makeReport();
        System.exit(0);
    }
}
