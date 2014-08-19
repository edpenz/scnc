package nz.ac.squash.reports;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import nz.ac.squash.db.beans.Member;
import nz.ac.squash.util.SessionHelper;

public class MakeLadder {

    public static void main(String[] args) throws IOException {
        File ladderFile = new File("logs/ladder.csv");
        FileWriter fw = new FileWriter(ladderFile);

        List<Member> ladder = SessionHelper.current().getLadder();
        int i = 1;
        for (Member member : ladder) {
            fw.write(i + ", " + member.getNameFormatted() + "\r\n");
            ++i;
        }

        fw.close();

        System.exit(0);
    }
}
