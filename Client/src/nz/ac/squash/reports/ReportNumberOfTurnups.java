package nz.ac.squash.reports;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;
import nz.ac.squash.db.beans.Member;
import nz.ac.squash.db.beans.MemberStatus;
import nz.ac.squash.util.Utility;

public class ReportNumberOfTurnups {
    private static class MemberStats {
        final public Member Member;
        final public Set<Date> NightsAttended;
        final public boolean HasPaid;

        public MemberStats(Member member, Set<Date> nightsAttended,
                boolean hasPaid) {
            Member = member;
            NightsAttended = nightsAttended;
            HasPaid = hasPaid;
        }

    }

    public static void main(String[] args) throws IOException {
        File turnupsFile = new File("logs/turnups.csv");
        FileWriter fw = new FileWriter(turnupsFile);

        Collection<MemberStatus> statuses = DB
                .executeTransaction(new Transaction<Collection<MemberStatus>>() {
                    @Override
                    public void run() {
                        setResult(listAll(MemberStatus.class));
                    }
                });

        HashMap<Member, Set<Date>> counts = new HashMap<Member, Set<Date>>();
        for (MemberStatus status : statuses) {
            if (!counts.containsKey(status.getMember())) counts.put(
                    status.getMember(), new HashSet<Date>());

            counts.get(status.getMember()).add(
                    Utility.stripTime(status.getDate()));
        }

        List<MemberStats> stats = new ArrayList<MemberStats>();
        for (Entry<Member, Set<Date>> count : counts.entrySet()) {
            final Member member = count.getKey();
            final Set<Date> presentDates = count.getValue();
            final boolean hasPaid = count.getKey().getPaymentStatus() != null;

            stats.add(new MemberStats(member, presentDates, hasPaid));
        }

        Collections.sort(stats, new Comparator<MemberStats>() {
            @Override
            public int compare(MemberStats a, MemberStats b) {
                if (a.HasPaid != b.HasPaid) {
                    return !a.HasPaid ? -1 : 1;
                }

                return b.NightsAttended.size() - a.NightsAttended.size();
            }
        });

        for (MemberStats stat : stats) {
            final Member member = stat.Member;
            final Set<Date> presentDates = stat.NightsAttended;
            final boolean hasPaid = stat.HasPaid;

            Date latest = Collections.max(presentDates);

            fw.write(member.getID() + "," + member.getNameFormatted() + "," +
                     member.getEmail() + "," + presentDates.size() + "," +
                     Utility.DATE_ONLY_FORMATTER.format(latest) +
                     (hasPaid ? ",paid" : ",") + "\r\n");
        }

        fw.close();

        System.exit(0);
    }
}
