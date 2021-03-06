package nz.ac.squash.reports;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nz.ac.squash.db.beans.Member;
import nz.ac.squash.db.beans.MemberStatus;
import nz.ac.squash.util.SessionHelper;
import nz.ac.squash.util.Utility;

public class MakeLadder {
    public static void makeReport() throws IOException {
        List<Member> ladder = SessionHelper.current().getLadder();

        Date fourWeeksAgo = new Date(
                Utility.today().getTime() - 4l * 7 * 24 * 60 * 60 * 1000);
        Set<Member> recentMembers = new HashSet<>();
        for (MemberStatus status : MemberStatus.getLatestStatus()) {
            if (status.getDate().after(fourWeeksAgo)) {
                recentMembers.add(status.getMember());
            }
        }

        File ladderFile = new File("logs/ladder_recent.csv");
        FileWriter fw = new FileWriter(ladderFile);
        int i = 1;
        for (Member member : ladder) {
            if (!recentMembers.contains(member)) continue;

            fw.write(i + ", " + member.getNameFormatted() + "\r\n");
            ++i;
        }
        fw.close();

        File bigLadderFile = new File("logs/ladder_full.csv");
        fw = new FileWriter(bigLadderFile);
        i = 1;
        for (Member member : ladder) {
            fw.write(i + ", " + member.getNameFormatted() + "\r\n");
            ++i;
        }
        fw.close();
    }

    public static void main(String[] args) throws IOException {
        makeReport();
        System.exit(0);
    }
}
