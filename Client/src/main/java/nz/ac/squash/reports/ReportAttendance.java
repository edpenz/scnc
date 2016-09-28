package nz.ac.squash.reports;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.beans.Member;
import nz.ac.squash.db.beans.MemberStatus;
import nz.ac.squash.util.Utility;

public class ReportAttendance {
    public static void makeReport() throws IOException {
        File log = new File("logs/attendance.csv");
        PrintStream writer = new PrintStream(log);

        // Get all sign-ins.
        List<MemberStatus> statuses = DB
                .executeTransaction(new DB.Transaction<List<MemberStatus>>() {
                    @Override
                    public void run() {
                        setResult(listAll(MemberStatus.class));
                    }
                });

        // Build table of attendance for each date.
        SortedMap<Date, Set<Member>> attendance = new TreeMap<>();
        for (MemberStatus status : statuses) {
            Date date = Utility.stripTime(status.getDate());

            Set<Member> presentMembers = attendance.get(date);
            if (presentMembers == null) {
                presentMembers = new HashSet<>();
                attendance.put(date, presentMembers);
            }

            presentMembers.add(status.getMember());
        }

        // Write table to file.
        writer.println("Date,Attendance");
        for (Entry<Date, Set<Member>> dayAttendances : attendance.entrySet()) {
            Date date = dayAttendances.getKey();
            int attendanceCount = dayAttendances.getValue().size();

            writer.print(Utility.DATE_ONLY_FORMATTER.format(date));
            writer.print(",");
            writer.print(attendanceCount);
            writer.println();
        }

        writer.close();
    }

    public static void main(String[] args) throws IOException {
        makeReport();
        System.exit(0);
    }
}
