package nz.ac.squash.reports;

import java.io.IOException;

import org.apache.log4j.Logger;

public class Reports {
    public static void runAllReports() {
        try {
            MakeLadder.makeReport();
            ReportAttendance.makeReport();
            ReportFairness.makeReport();
            ReportNumberOfTurnups.makeReport();
        } catch (IOException ex) {
            Logger.getLogger(Reports.class).error("Failed to make reports", ex);
        }
    }
}
