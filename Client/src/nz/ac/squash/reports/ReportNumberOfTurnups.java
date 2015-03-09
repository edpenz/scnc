package nz.ac.squash.reports;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;
import nz.ac.squash.db.beans.Member;
import nz.ac.squash.db.beans.MemberStatus;
import nz.ac.squash.util.Utility;

public class ReportNumberOfTurnups {

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

        for (Entry<Member, Set<Date>> count : counts.entrySet()) {
            final Member member = count.getKey();
            final Set<Date> presentDates = count.getValue();

            boolean hasPaid = count.getKey().getPaymentStatus() != null;
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
