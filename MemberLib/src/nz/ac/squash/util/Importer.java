package nz.ac.squash.util;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.beans.Member;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class Importer {
    private static final Logger sLogger = Logger.getLogger(Importer.class);

    public static void importFromCsv(final String dir) {
        for (File file : new File(dir).listFiles()) {
            if (file.isDirectory() || !file.getName().endsWith(".csv")) continue;

            Scanner reader = null;
            try {
                reader = new Scanner(file);

                // Parse each line.
                while (true) {
                    String[] lineParts = reader.nextLine().split(",");
                    final Member imported = new Member();
                    try {
                        imported.setSignupTime(Utility.SPREADSHEET_FORMATTER
                                .parse(lineParts[0]));
                    } catch (ParseException e) {
                        sLogger.warn("Invalid date/time: \"" + lineParts[0] +
                                     "\", probably the CSV header");
                        continue;
                    }
                    imported.setName(lineParts[1]);
                    // newMember.setSignupCause(lineParts[2]);
                    imported.setStudentStatus(lineParts[3]);
                    imported.setStudentIdAndUpi(lineParts[4]);
                    imported.setEmail(lineParts[5]);
                    imported.setSkillLevel(lineParts[6]);
                    imported.setPaymentMethod(lineParts[7]);

                    // Optional fields.
                    if (lineParts.length > 8) {
                        imported.setActive(StringUtils.isEmpty(lineParts[8]));
                        imported.setSignupMethod(lineParts[9]);
                    }

                    if (imported.isActive()) {
                        DB.queueTransaction(new DB.Transaction<Void>() {
                            @Override
                            public void run() {
                                Member existing = Utility.first(query(
                                        Member.class,
                                        "m where m.mSignupTime = ?0",
                                        imported.getSignupTime()));

                                if (existing == null) {
                                    update(imported);
                                    sLogger.info("Imported new member " +
                                                 imported.getNameFormatted());
                                } else {
                                    // TODO Update DB from CSV.
                                }
                            }
                        });
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (NoSuchElementException e) {
                IOUtils.closeQuietly(reader);
                file.delete();
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }
    }
}
